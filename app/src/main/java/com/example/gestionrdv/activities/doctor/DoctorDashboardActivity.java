package com.example.gestionrdv.activities.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.example.gestionrdv.adapters.DoctorAppointmentAdapter;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.models.Doctor;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorDashboardActivity extends AppCompatActivity implements DoctorAppointmentAdapter.OnDoctorAppointmentActionListener {

    private static final String TAG = "DoctorDashboard";

    // Views
    private TextView dateText;
    private TextView todayCount, pendingCount, weekCount, completedCount;
    private TextView viewAllLink;
    private RecyclerView todayAppointmentsRecycler;
    private ImageButton notificationsButton;
    private MaterialButton manageScheduleButton, viewStatsButton, logoutButton;

    // Data
    private SessionManager sessionManager;
    private AppointmentRepository appointmentRepository;
    private DoctorRepository doctorRepository;
    private DoctorAppointmentAdapter adapter;
    private Doctor currentDoctor;
    private long doctorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Initialize session and repositories
        sessionManager = new SessionManager(this);
        appointmentRepository = new AppointmentRepository(this);
        doctorRepository = new DoctorRepository(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Get doctor ID from session
        doctorId = sessionManager.getProfileId();
        if (doctorId == -1) {
            Log.e(TAG, "Invalid doctor ID from session");
            Toast.makeText(this, "Erreur: Session invalide", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Load doctor info
        currentDoctor = doctorRepository.getDoctorById(doctorId);

        initViews();
        setupCurrentDate();
        loadStatistics();
        setupTodayAppointments();
        setupClickListeners();
    }

    private void initViews() {
        dateText = findViewById(R.id.dateText);
        todayCount = findViewById(R.id.todayCount);
        pendingCount = findViewById(R.id.pendingCount);
        weekCount = findViewById(R.id.weekCount);
        completedCount = findViewById(R.id.completedCount);
        viewAllLink = findViewById(R.id.viewAllLink);
        todayAppointmentsRecycler = findViewById(R.id.todayAppointmentsRecycler);
        notificationsButton = findViewById(R.id.notificationsButton);
        manageScheduleButton = findViewById(R.id.manageScheduleButton);
        viewStatsButton = findViewById(R.id.viewStatsButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.FRENCH);
        String currentDate = sdf.format(new Date());
        // Capitalize first letter
        currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1);
        dateText.setText(currentDate);
    }

    private void loadStatistics() {
        if (doctorId == -1) return;

        // Get statistics from repository
        DoctorRepository.DoctorStats stats = doctorRepository.getDoctorStats(doctorId);

        todayCount.setText(String.valueOf(stats.todayAppointments));
        pendingCount.setText(String.valueOf(stats.pendingAppointments));
        weekCount.setText(String.valueOf(stats.weekAppointments));
        completedCount.setText(String.valueOf(stats.completedAppointments));
    }

    private void setupTodayAppointments() {
        todayAppointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Get today's date in database format
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Load today's appointments
        List<Appointment> todayAppointments = appointmentRepository.getDoctorAppointmentsByDate(doctorId, today);

        Log.d(TAG, "Loaded " + todayAppointments.size() + " appointments for today");

        // Create adapter
        adapter = new DoctorAppointmentAdapter(todayAppointments, this);
        todayAppointmentsRecycler.setAdapter(adapter);

        // Show empty state if needed
        if (todayAppointments.isEmpty()) {
            // The RecyclerView will be empty, which is fine
            // Could add an empty state view here if needed
        }
    }

    private void setupClickListeners() {
        notificationsButton.setOnClickListener(v -> {
            // Count pending appointments for notification
            DoctorRepository.DoctorStats stats = doctorRepository.getDoctorStats(doctorId);
            Toast.makeText(this, stats.pendingAppointments + " rendez-vous en attente", Toast.LENGTH_SHORT).show();
        });

        viewAllLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAppointmentsActivity.class);
            startActivity(intent);
        });

        manageScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorScheduleActivity.class);
            startActivity(intent);
        });

        viewStatsButton.setOnClickListener(v -> {
            showStatisticsDialog();
        });

        logoutButton.setOnClickListener(v -> {
            confirmLogout();
        });
    }

    private void showStatisticsDialog() {
        DoctorRepository.DoctorStats stats = doctorRepository.getDoctorStats(doctorId);

        String message = String.format(Locale.getDefault(),
                "Statistiques de vos consultations:\n\n" +
                "Aujourd'hui: %d rendez-vous\n" +
                "Cette semaine: %d rendez-vous\n" +
                "En attente: %d rendez-vous\n" +
                "Total complétés: %d rendez-vous\n" +
                "Total: %d rendez-vous",
                stats.todayAppointments,
                stats.weekAppointments,
                stats.pendingAppointments,
                stats.completedAppointments,
                stats.totalAppointments
        );

        new AlertDialog.Builder(this)
                .setTitle("Mes statistiques")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Déconnexion", (dialog, which) -> {
                    confirmLogout();
                })
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> logout())
                .setNegativeButton("Non", null)
                .show();
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DoctorDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // DoctorAppointmentAdapter callbacks
    @Override
    public void onAppointmentClick(Appointment appointment) {
        showAppointmentDetailsDialog(appointment);
    }

    @Override
    public void onConfirmClick(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmer le rendez-vous")
                .setMessage("Confirmer le rendez-vous avec " + appointment.getPatientName() + " ?")
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), "confirmed");
                    if (success) {
                        Toast.makeText(this, "Rendez-vous confirmé", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "Erreur lors de la confirmation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onCompleteClick(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Terminer le rendez-vous")
                .setMessage("Marquer le rendez-vous avec " + appointment.getPatientName() + " comme terminé ?")
                .setPositiveButton("Terminer", (dialog, which) -> {
                    boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), "completed");
                    if (success) {
                        Toast.makeText(this, "Rendez-vous terminé", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onCancelClick(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Annuler le rendez-vous")
                .setMessage("Voulez-vous vraiment annuler ce rendez-vous ?")
                .setPositiveButton("Oui, annuler", (dialog, which) -> {
                    boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), "cancelled");
                    if (success) {
                        Toast.makeText(this, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'annulation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void showAppointmentDetailsDialog(Appointment appointment) {
        String details = String.format(
                "Patient: %s\n" +
                "Date: %s\n" +
                "Heure: %s - %s\n" +
                "Motif: %s\n" +
                "Statut: %s",
                appointment.getPatientName() != null ? appointment.getPatientName() : "N/A",
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getEndTime() != null ? appointment.getEndTime() : "",
                appointment.getReason() != null ? appointment.getReason() : "N/A",
                getStatusLabel(appointment.getStatus())
        );

        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            details += "\n\nNotes: " + appointment.getNotes();
        }

        new AlertDialog.Builder(this)
                .setTitle("Détails du rendez-vous")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Inconnu";
        switch (status) {
            case "pending": return "En attente";
            case "confirmed": return "Confirmé";
            case "completed": return "Terminé";
            case "cancelled": return "Annulé";
            default: return status;
        }
    }

    private void refreshData() {
        loadStatistics();
        setupTodayAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
}
