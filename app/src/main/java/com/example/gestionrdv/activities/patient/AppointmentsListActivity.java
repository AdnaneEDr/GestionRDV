package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.AppointmentAdapter;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentsListActivity extends AppCompatActivity {

    private static final String TAG = "AppointmentsListActivity";

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments_list);

        initViews();
        setupViewPager();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupViewPager() {
        AppointmentsPagerAdapter adapter = new AppointmentsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("À venir");
                    break;
                case 1:
                    tab.setText("Passés");
                    break;
                case 2:
                    tab.setText("Annulés");
                    break;
            }
        }).attach();
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ViewPager Adapter
    private static class AppointmentsPagerAdapter extends FragmentStateAdapter {

        public AppointmentsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return AppointmentsFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    // Fragment for appointments list
    public static class AppointmentsFragment extends Fragment {

        private static final String TAG = "AppointmentsFragment";
        private static final String ARG_TAB_POSITION = "tab_position";

        private int tabPosition;
        private RecyclerView recyclerView;
        private LinearLayout emptyStateLayout;
        private AppointmentRepository appointmentRepository;
        private SessionManager sessionManager;
        private AppointmentAdapter adapter;
        private List<Appointment> appointments;

        public static AppointmentsFragment newInstance(int position) {
            AppointmentsFragment fragment = new AppointmentsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_TAB_POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                tabPosition = getArguments().getInt(ARG_TAB_POSITION);
            }

            // Initialize repository and session manager
            appointmentRepository = new AppointmentRepository(requireContext());
            sessionManager = new SessionManager(requireContext());
            appointments = new ArrayList<>();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Create main container
            LinearLayout mainLayout = new LinearLayout(requireContext());
            mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            mainLayout.setOrientation(LinearLayout.VERTICAL);

            // Create RecyclerView
            recyclerView = new RecyclerView(requireContext());
            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            int padding = dpToPx(16);
            recyclerView.setPadding(padding, padding, padding, padding);
            recyclerView.setClipToPadding(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            // Create empty state layout
            emptyStateLayout = createEmptyStateLayout();

            mainLayout.addView(recyclerView);
            mainLayout.addView(emptyStateLayout);

            setupAdapter();
            loadAppointments();

            return mainLayout;
        }

        private LinearLayout createEmptyStateLayout() {
            LinearLayout layout = new LinearLayout(requireContext());
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            int padding = dpToPx(32);
            layout.setPadding(padding, padding, padding, padding);
            layout.setVisibility(View.GONE);

            // Icon
            ImageView icon = new ImageView(requireContext());
            int iconSize = dpToPx(80);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            iconParams.bottomMargin = dpToPx(16);
            icon.setLayoutParams(iconParams);
            icon.setImageResource(R.drawable.ic_calendar);
            icon.setColorFilter(requireContext().getResources().getColor(R.color.text_hint, null));
            icon.setAlpha(0.5f);

            // Message
            TextView message = new TextView(requireContext());
            message.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            message.setTextSize(16);
            message.setTextColor(requireContext().getResources().getColor(R.color.text_secondary, null));
            message.setGravity(Gravity.CENTER);

            // Set message based on tab
            switch (tabPosition) {
                case 0:
                    message.setText("Aucun rendez-vous à venir");
                    break;
                case 1:
                    message.setText("Aucun rendez-vous passé");
                    break;
                case 2:
                    message.setText("Aucun rendez-vous annulé");
                    break;
            }

            layout.addView(icon);
            layout.addView(message);

            return layout;
        }

        private void setupAdapter() {
            adapter = new AppointmentAdapter(appointments, new AppointmentAdapter.OnAppointmentClickListener() {
                @Override
                public void onAppointmentClick(Appointment appointment) {
                    openAppointmentDetails(appointment);
                }

                @Override
                public void onCancelClick(Appointment appointment) {
                    showCancelDialog(appointment);
                }

                @Override
                public void onDetailsClick(Appointment appointment) {
                    openAppointmentDetails(appointment);
                }
            });
            recyclerView.setAdapter(adapter);
        }

        private void loadAppointments() {
            long patientId = sessionManager.getProfileId();

            if (patientId == -1) {
                Log.e(TAG, "Invalid patient ID from session");
                updateEmptyState();
                return;
            }

            List<Appointment> loadedAppointments = fetchAppointmentsByTab(patientId, tabPosition);

            appointments.clear();
            if (loadedAppointments != null) {
                // Format dates for display
                for (Appointment apt : loadedAppointments) {
                    apt.setAppointmentDate(formatDateForDisplay(apt.getAppointmentDate()));
                    appointments.add(apt);
                }
            }

            Log.d(TAG, "Tab " + tabPosition + ": Loaded " + appointments.size() + " appointments");

            adapter.notifyDataSetChanged();
            updateEmptyState();
        }

        private List<Appointment> fetchAppointmentsByTab(long patientId, int tab) {
            switch (tab) {
                case 0: // Upcoming - pending and confirmed with future dates
                    return appointmentRepository.getUpcomingPatientAppointments(patientId);

                case 1: // Past - completed appointments
                    return appointmentRepository.getPatientAppointmentsByStatus(patientId, "completed");

                case 2: // Cancelled
                    return appointmentRepository.getPatientAppointmentsByStatus(patientId, "cancelled");

                default:
                    return new ArrayList<>();
            }
        }

        private String formatDateForDisplay(String dbDate) {
            if (dbDate == null || dbDate.isEmpty()) {
                return "Date non définie";
            }

            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("d MMM yyyy", Locale.FRENCH);
                Date date = dbFormat.parse(dbDate);
                if (date != null) {
                    return displayFormat.format(date);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + dbDate, e);
            }

            // Return original if parsing fails (might already be formatted)
            return dbDate;
        }

        private void updateEmptyState() {
            if (appointments.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
            }
        }

        private void openAppointmentDetails(Appointment appointment) {
            Intent intent = new Intent(requireContext(), AppointmentDetailsActivity.class);
            intent.putExtra("appointment_id", appointment.getId());
            startActivity(intent);
        }

        private void showCancelDialog(Appointment appointment) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Annuler le rendez-vous")
                    .setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous ?")
                    .setPositiveButton("Oui, annuler", (dialog, which) -> {
                        cancelAppointment(appointment);
                    })
                    .setNegativeButton("Non", null)
                    .show();
        }

        private void cancelAppointment(Appointment appointment) {
            boolean success = appointmentRepository.updateAppointmentStatus(
                    appointment.getId(),
                    "cancelled"
            );

            if (success) {
                Log.d(TAG, "Appointment cancelled: ID=" + appointment.getId());
                Toast.makeText(requireContext(), "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                loadAppointments(); // Refresh list
            } else {
                Log.e(TAG, "Failed to cancel appointment: ID=" + appointment.getId());
                Toast.makeText(requireContext(), "Erreur lors de l'annulation", Toast.LENGTH_SHORT).show();
            }
        }

        private int dpToPx(int dp) {
            float density = requireContext().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }

        @Override
        public void onResume() {
            super.onResume();
            // Reload appointments when fragment becomes visible again
            if (appointmentRepository != null && sessionManager != null) {
                loadAppointments();
            }
        }
    }
}
