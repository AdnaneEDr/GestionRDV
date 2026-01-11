package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.gestionrdv.models.Appointment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsListActivity extends AppCompatActivity {

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

        private static final String ARG_TAB_POSITION = "tab_position";
        private int tabPosition;
        private RecyclerView recyclerView;

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
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            recyclerView = new RecyclerView(requireContext());
            recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            recyclerView.setPadding(16, 16, 16, 16);
            recyclerView.setClipToPadding(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            loadAppointments();

            return recyclerView;
        }

        private void loadAppointments() {
            List<Appointment> appointments = getSampleAppointments(tabPosition);

            AppointmentAdapter adapter = new AppointmentAdapter(appointments, new AppointmentAdapter.OnAppointmentClickListener() {
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

        private List<Appointment> getSampleAppointments(int tabPosition) {
            List<Appointment> appointments = new ArrayList<>();

            switch (tabPosition) {
                case 0: // Upcoming
                    Appointment apt1 = new Appointment();
                    apt1.setId(1);
                    apt1.setAppointmentDate("25 Déc 2024");
                    apt1.setAppointmentTime("10:30");
                    apt1.setEndTime("11:00");
                    apt1.setReason("Consultation générale");
                    apt1.setDoctorName("Dr. Fatima Zahra");
                    apt1.setDoctorSpecialization("Médecine Générale");
                    apt1.setStatus("confirmed");
                    appointments.add(apt1);

                    Appointment apt2 = new Appointment();
                    apt2.setId(2);
                    apt2.setAppointmentDate("28 Déc 2024");
                    apt2.setAppointmentTime("14:00");
                    apt2.setEndTime("14:30");
                    apt2.setReason("Suivi médical");
                    apt2.setDoctorName("Dr. Ahmed Bennani");
                    apt2.setDoctorSpecialization("Cardiologie");
                    apt2.setStatus("pending");
                    appointments.add(apt2);
                    break;

                case 1: // Past
                    Appointment apt3 = new Appointment();
                    apt3.setId(3);
                    apt3.setAppointmentDate("15 Nov 2024");
                    apt3.setAppointmentTime("09:00");
                    apt3.setEndTime("09:30");
                    apt3.setReason("Grippe saisonnière");
                    apt3.setDoctorName("Dr. Fatima Zahra");
                    apt3.setDoctorSpecialization("Médecine Générale");
                    apt3.setStatus("completed");
                    appointments.add(apt3);

                    Appointment apt4 = new Appointment();
                    apt4.setId(4);
                    apt4.setAppointmentDate("20 Oct 2024");
                    apt4.setAppointmentTime("16:00");
                    apt4.setEndTime("16:30");
                    apt4.setReason("Contrôle annuel");
                    apt4.setDoctorName("Dr. Sara El Amrani");
                    apt4.setDoctorSpecialization("Dermatologie");
                    apt4.setStatus("completed");
                    appointments.add(apt4);
                    break;

                case 2: // Cancelled
                    Appointment apt5 = new Appointment();
                    apt5.setId(5);
                    apt5.setAppointmentDate("10 Nov 2024");
                    apt5.setAppointmentTime("11:00");
                    apt5.setEndTime("11:30");
                    apt5.setReason("Consultation générale");
                    apt5.setDoctorName("Dr. Karim Idrissi");
                    apt5.setDoctorSpecialization("Pédiatrie");
                    apt5.setStatus("cancelled");
                    appointments.add(apt5);
                    break;
            }

            return appointments;
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
                        Toast.makeText(requireContext(), "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                        loadAppointments(); // Refresh list
                    })
                    .setNegativeButton("Non", null)
                    .show();
        }
    }
}
