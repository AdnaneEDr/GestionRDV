package com.example.gestionrdv.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView greetingText, adminNameText;
    private ImageButton notificationsButton;
    private CardView managePatientsCard, manageDoctorsCard, viewCalendarCard, settingsCard;
    private MaterialButton logoutButton;
    private SessionManager sessionManager;

    // Statistics views
    private TextView totalDoctorsText, totalPatientsText, totalAppointmentsText;
    private TextView activeDoctorsText, todayAppointmentsText, pendingAppointmentsText;
    private TextView weekAppointmentsText, monthAppointmentsText;

    // Alert views
    private LinearLayout alertsContainer;
    private TextView pendingAlertText, todayAlertText;

    // Recent activity
    private RecyclerView recentActivityRecycler;
    private TextView noActivityText;

    // Repositories
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private CardView manageAppointmentsCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        // Initialize repositories
        doctorRepository = new DoctorRepository(this);
        patientRepository = new PatientRepository(this);
        appointmentRepository = new AppointmentRepository(this);

        initViews();
        setupUserInfo();
        loadStatistics();
        loadAlerts();
        loadRecentActivity();
        setupClickListeners();
    }

    private void initViews() {
        greetingText = findViewById(R.id.greetingText);
        adminNameText = findViewById(R.id.adminNameText);
        notificationsButton = findViewById(R.id.notificationsButton);
        managePatientsCard = findViewById(R.id.managePatientsCard);
        manageDoctorsCard = findViewById(R.id.manageDoctorsCard);
        viewCalendarCard = findViewById(R.id.viewCalendarCard);
        settingsCard = findViewById(R.id.settingsCard);
        logoutButton = findViewById(R.id.logoutButton);
        manageAppointmentsCard = findViewById(R.id.manageAppointmentsCard);
        // Statistics
        totalDoctorsText = findViewById(R.id.totalDoctorsText);
        totalPatientsText = findViewById(R.id.totalPatientsText);
        totalAppointmentsText = findViewById(R.id.totalAppointmentsText);
        activeDoctorsText = findViewById(R.id.activeDoctorsText);
        todayAppointmentsText = findViewById(R.id.todayAppointmentsText);
        pendingAppointmentsText = findViewById(R.id.pendingAppointmentsText);
        weekAppointmentsText = findViewById(R.id.weekAppointmentsText);
        monthAppointmentsText = findViewById(R.id.monthAppointmentsText);

        // Alerts
        alertsContainer = findViewById(R.id.alertsContainer);
        pendingAlertText = findViewById(R.id.pendingAlertText);
        todayAlertText = findViewById(R.id.todayAlertText);

        // Recent activity
        recentActivityRecycler = findViewById(R.id.recentActivityRecycler);
        noActivityText = findViewById(R.id.noActivityText);

        recentActivityRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupUserInfo() {
        String fullName = sessionManager.getFullName();
        adminNameText.setText(fullName);

        // Set greeting based on time of day
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Bonjour";
        } else if (hour < 18) {
            greeting = "Bon après-midi";
        } else {
            greeting = "Bonsoir";
        }
        greetingText.setText(greeting);
    }

    private void loadStatistics() {
        // Total counts
        int totalDoctors = doctorRepository.getAllDoctors().size();
        int totalPatients = patientRepository.getAllPatients().size();
        List<Appointment> allAppointments = appointmentRepository.getAllAppointments();
        int totalAppointments = allAppointments.size();

        totalDoctorsText.setText(String.valueOf(totalDoctors));
        totalPatientsText.setText(String.valueOf(totalPatients));
        totalAppointmentsText.setText(String.valueOf(totalAppointments));

        // Active doctors
        int activeDoctors = doctorRepository.getActiveDoctorsCount();
        activeDoctorsText.setText(activeDoctors + " actifs");

        // Today's appointments
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Appointment> todayAppointments = appointmentRepository.getAppointmentsByDate(today);
        todayAppointmentsText.setText(todayAppointments.size() + " aujourd'hui");

        // Pending appointments
        int pendingCount = 0;
        for (Appointment apt : allAppointments) {
            if ("pending".equals(apt.getStatus())) {
                pendingCount++;
            }
        }
        pendingAppointmentsText.setText(pendingCount + " en attente");

        // This week's appointments
        int weekCount = getAppointmentsThisWeek(allAppointments);
        weekAppointmentsText.setText(weekCount + " cette semaine");

        // This month's appointments
        int monthCount = getAppointmentsThisMonth(allAppointments);
        monthAppointmentsText.setText(monthCount + " ce mois");
    }

    private void loadAlerts() {
        List<Appointment> allAppointments = appointmentRepository.getAllAppointments();

        // Count pending appointments
        int pendingCount = 0;
        for (Appointment apt : allAppointments) {
            if ("pending".equals(apt.getStatus())) {
                pendingCount++;
            }
        }

        // Count today's appointments
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Appointment> todayAppointments = appointmentRepository.getAppointmentsByDate(today);

        // Show/hide alerts
        if (pendingCount > 0 || todayAppointments.size() > 0) {
            alertsContainer.setVisibility(View.VISIBLE);

            if (pendingCount > 0) {
                pendingAlertText.setVisibility(View.VISIBLE);
                pendingAlertText.setText("⚠️ " + pendingCount + " rendez-vous en attente de confirmation");
            } else {
                pendingAlertText.setVisibility(View.GONE);
            }

            if (todayAppointments.size() > 0) {
                todayAlertText.setVisibility(View.VISIBLE);
                todayAlertText.setText("📅 " + todayAppointments.size() + " rendez-vous prévus aujourd'hui");
            } else {
                todayAlertText.setVisibility(View.GONE);
            }
        } else {
            alertsContainer.setVisibility(View.GONE);
        }
    }

    private void loadRecentActivity() {
        List<Appointment> allAppointments = appointmentRepository.getAllAppointments();

        // Get last 5 appointments
        List<Appointment> recentAppointments = new ArrayList<>();
        int count = Math.min(5, allAppointments.size());
        for (int i = 0; i < count; i++) {
            recentAppointments.add(allAppointments.get(i));
        }

        if (recentAppointments.isEmpty()) {
            recentActivityRecycler.setVisibility(View.GONE);
            noActivityText.setVisibility(View.VISIBLE);
        } else {
            recentActivityRecycler.setVisibility(View.VISIBLE);
            noActivityText.setVisibility(View.GONE);

            // Create adapter for recent activity
            RecentActivityAdapter adapter = new RecentActivityAdapter(recentAppointments);
            recentActivityRecycler.setAdapter(adapter);
        }
    }

    private int getAppointmentsThisWeek(List<Appointment> appointments) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        String weekStart = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 7);
        String weekEnd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        int count = 0;
        for (Appointment apt : appointments) {
            String date = apt.getAppointmentDate();
            if (date.compareTo(weekStart) >= 0 && date.compareTo(weekEnd) < 0) {
                count++;
            }
        }
        return count;
    }

    private int getAppointmentsThisMonth(List<Appointment> appointments) {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        int count = 0;
        for (Appointment apt : appointments) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(apt.getAppointmentDate());
                calendar.setTime(date);

                if (calendar.get(Calendar.MONTH) == currentMonth &&
                        calendar.get(Calendar.YEAR) == currentYear) {
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private void setupClickListeners() {
        notificationsButton.setOnClickListener(v -> {
            // Count pending appointments for notification badge
            List<Appointment> allAppointments = appointmentRepository.getAllAppointments();
            int pendingCount = 0;
            for (Appointment apt : allAppointments) {
                if ("pending".equals(apt.getStatus())) {
                    pendingCount++;
                }
            }
            Toast.makeText(this, pendingCount + " notifications", Toast.LENGTH_SHORT).show();
        });

        managePatientsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPatientsActivity.class);
            startActivity(intent);
        });

        manageDoctorsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDoctorsActivity.class);
            startActivity(intent);
        });
        manageAppointmentsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAppointmentsActivity.class);
            startActivity(intent);
        });
        viewCalendarCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCalendarActivity.class);
            startActivity(intent);
        });

        settingsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminSettingsActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());

        // Alert click listeners
        if (pendingAlertText != null) {
            pendingAlertText.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminCalendarActivity.class);
                startActivity(intent);
            });
        }

        if (todayAlertText != null) {
            todayAlertText.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminCalendarActivity.class);
                startActivity(intent);
            });
        }
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadStatistics();
        loadAlerts();
        loadRecentActivity();
    }

    // Inner class for Recent Activity Adapter
    private class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

        private List<Appointment> appointments;

        public RecentActivityAdapter(List<Appointment> appointments) {
            this.appointments = appointments;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recent_activity, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Appointment appointment = appointments.get(position);

            // Format: "RDV avec Dr. [name] - [patient]"
            String doctorName = appointment.getDoctorName() != null ? appointment.getDoctorName() : "Médecin";
            String patientName = appointment.getPatientName() != null ? appointment.getPatientName() : "Patient";

            holder.activityText.setText("RDV avec Dr. " + doctorName);
            holder.detailsText.setText(patientName + " • " + appointment.getAppointmentDate() + " à " + appointment.getAppointmentTime());

            // Status badge
            String status = appointment.getStatus();
            int color;
            String statusText;

            switch (status) {
                case "confirmed":
                    color = getResources().getColor(android.R.color.holo_green_dark);
                    statusText = "Confirmé";
                    break;
                case "pending":
                    color = getResources().getColor(android.R.color.holo_orange_dark);
                    statusText = "En attente";
                    break;
                case "cancelled":
                    color = getResources().getColor(android.R.color.holo_red_dark);
                    statusText = "Annulé";
                    break;
                case "completed":
                    color = getResources().getColor(android.R.color.holo_blue_dark);
                    statusText = "Terminé";
                    break;
                default:
                    color = getResources().getColor(android.R.color.darker_gray);
                    statusText = status;
            }

            holder.statusBadge.setText(statusText);
            holder.statusBadge.setTextColor(color);
        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView activityText, detailsText, statusBadge;

            public ViewHolder(View itemView) {
                super(itemView);
                activityText = itemView.findViewById(R.id.activityText);
                detailsText = itemView.findViewById(R.id.detailsText);
                statusBadge = itemView.findViewById(R.id.statusBadge);

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminCalendarActivity.class);
                    startActivity(intent);
                });
            }
        }
    }
}