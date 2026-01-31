package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.example.gestionrdv.activities.doctor.DoctorSearchActivity;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.models.Patient;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PatientDashboard";

    // Views
    private TextView greetingText, patientNameText;
    private TextView doctorName, doctorSpecialty, appointmentDate, appointmentTime;
    private TextView statusBadge;
    private MaterialButton quickBookButton, cancelButton, modifyButton;
    private TextView seeAllAppointmentsLink;
    private CardView upcomingAppointmentCard, noAppointmentsCard;
    private CardView findDoctorCard, myAppointmentsCard;
    private ImageButton notificationsButton;
    private BottomNavigationView bottomNavigation;

    // Data
    private SessionManager sessionManager;
    private AppointmentRepository appointmentRepository;
    private PatientRepository patientRepository;
    private Appointment upcomingAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        // Initialize repositories and session manager
        sessionManager = new SessionManager(this);
        appointmentRepository = new AppointmentRepository(this);
        patientRepository = new PatientRepository(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
        loadPatientData();
        loadUpcomingAppointment();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        greetingText = findViewById(R.id.greetingText);
        patientNameText = findViewById(R.id.patientNameText);
        doctorName = findViewById(R.id.doctorName);
        doctorSpecialty = findViewById(R.id.doctorSpecialty);
        appointmentDate = findViewById(R.id.appointmentDate);
        appointmentTime = findViewById(R.id.appointmentTime);
        statusBadge = findViewById(R.id.statusBadge);
        quickBookButton = findViewById(R.id.quickBookButton);
        cancelButton = findViewById(R.id.cancelButton);
        modifyButton = findViewById(R.id.modifyButton);
        seeAllAppointmentsLink = findViewById(R.id.seeAllAppointmentsLink);
        upcomingAppointmentCard = findViewById(R.id.upcomingAppointmentCard);
        noAppointmentsCard = findViewById(R.id.noAppointmentsCard);
        findDoctorCard = findViewById(R.id.findDoctorCard);
        myAppointmentsCard = findViewById(R.id.myAppointmentsCard);
        notificationsButton = findViewById(R.id.notificationsButton);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadPatientData() {
        // Set greeting based on time of day
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            greetingText.setText("Bonjour,");
        } else if (hour < 18) {
            greetingText.setText("Bon après-midi,");
        } else {
            greetingText.setText("Bonsoir,");
        }

        // Get patient name from session or database
        String fullName = sessionManager.getFullName();

        if (fullName != null && !fullName.isEmpty()) {
            patientNameText.setText(fullName);
        } else {
            // Fallback: fetch from database
            long patientId = sessionManager.getProfileId();
            if (patientId != -1) {
                Patient patient = patientRepository.getPatientById(patientId);
                if (patient != null) {
                    patientNameText.setText(patient.getFullName());
                } else {
                    patientNameText.setText("Patient");
                }
            } else {
                patientNameText.setText("Patient");
            }
        }
    }

    private void loadUpcomingAppointment() {
        long patientId = sessionManager.getProfileId();

        if (patientId == -1) {
            Log.e(TAG, "Invalid patient ID from session");
            showNoAppointments();
            return;
        }

        // Get upcoming appointments for this patient
        List<Appointment> appointments = appointmentRepository.getUpcomingPatientAppointments(patientId);

        if (appointments != null && !appointments.isEmpty()) {
            // Get the first (next) upcoming appointment
            upcomingAppointment = appointments.get(0);
            displayUpcomingAppointment(upcomingAppointment);
        } else {
            showNoAppointments();
        }
    }

    private void displayUpcomingAppointment(Appointment appointment) {
        // Show appointment card, hide empty state
        upcomingAppointmentCard.setVisibility(View.VISIBLE);
        noAppointmentsCard.setVisibility(View.GONE);

        // Set doctor info
        String docName = appointment.getDoctorName();
        doctorName.setText(docName != null ? docName : "Médecin");

        String specialty = appointment.getDoctorSpecialization();
        doctorSpecialty.setText(specialty != null ? specialty : "Spécialité");

        // Format and set date
        String dateStr = appointment.getAppointmentDate();
        appointmentDate.setText(formatDateForDisplay(dateStr));

        // Set time
        appointmentTime.setText(appointment.getAppointmentTime());

        // Set status badge
        updateStatusBadge(appointment.getStatus());
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

        // Return original if parsing fails
        return dbDate;
    }

    private void updateStatusBadge(String status) {
        if (status == null) {
            status = "pending";
        }

        int backgroundColor;
        String displayText;

        switch (status.toLowerCase()) {
            case "confirmed":
                backgroundColor = R.color.success;
                displayText = getString(R.string.status_confirmed);
                break;
            case "completed":
                backgroundColor = R.color.info;
                displayText = getString(R.string.status_completed);
                break;
            case "cancelled":
                backgroundColor = R.color.error;
                displayText = getString(R.string.status_cancelled);
                break;
            case "pending":
            default:
                backgroundColor = R.color.warning;
                displayText = getString(R.string.status_pending);
                break;
        }

        statusBadge.setText(displayText);

        // Update the CardView background color (statusBadge's parent)
        View parent = (View) statusBadge.getParent();
        if (parent instanceof CardView) {
            ((CardView) parent).setCardBackgroundColor(
                ContextCompat.getColor(this, backgroundColor)
            );
        }
    }

    private void showNoAppointments() {
        upcomingAppointmentCard.setVisibility(View.GONE);
        noAppointmentsCard.setVisibility(View.VISIBLE);
        upcomingAppointment = null;
    }

    private void setupClickListeners() {
        // Quick book button
        quickBookButton.setOnClickListener(v -> {
            startActivity(new Intent(this, BookAppointmentActivity.class));
        });

        // See all appointments
        seeAllAppointmentsLink.setOnClickListener(v -> {
            startActivity(new Intent(this, AppointmentsListActivity.class));
        });

        // Upcoming appointment card click
        upcomingAppointmentCard.setOnClickListener(v -> {
            if (upcomingAppointment != null) {
                Intent intent = new Intent(this, AppointmentDetailsActivity.class);
                intent.putExtra("appointment_id", upcomingAppointment.getId());
                startActivity(intent);
            }
        });

        // Cancel appointment button
        cancelButton.setOnClickListener(v -> {
            if (upcomingAppointment != null) {
                showCancelConfirmationDialog();
            }
        });

        // Modify appointment button
        modifyButton.setOnClickListener(v -> {
            if (upcomingAppointment != null) {
                Intent intent = new Intent(this, SelectDateTimeActivity.class);
                intent.putExtra("appointment_id", upcomingAppointment.getId());
                intent.putExtra("doctor_id", upcomingAppointment.getDoctorId());
                intent.putExtra("doctor_name", upcomingAppointment.getDoctorName());
                intent.putExtra("doctor_specialty", upcomingAppointment.getDoctorSpecialization());
                intent.putExtra("reason", upcomingAppointment.getReason());
                intent.putExtra("is_modification", true);
                startActivity(intent);
            }
        });

        // Notifications button
        notificationsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
        });

        // Quick action cards
        findDoctorCard.setOnClickListener(v -> {
            startActivity(new Intent(this, DoctorSearchActivity.class));
        });

        myAppointmentsCard.setOnClickListener(v -> {
            startActivity(new Intent(this, AppointmentsListActivity.class));
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_appointments) {
                startActivity(new Intent(this, AppointmentsListActivity.class));
                return true;
            } else if (itemId == R.id.nav_doctors) {
                startActivity(new Intent(this, DoctorSearchActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, PatientProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Annuler le rendez-vous")
                .setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous ?")
                .setPositiveButton("Oui, annuler", (dialog, which) -> {
                    cancelAppointment();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void cancelAppointment() {
        if (upcomingAppointment == null) {
            return;
        }

        // Update appointment status in database
        boolean success = appointmentRepository.updateAppointmentStatus(
                upcomingAppointment.getId(),
                "cancelled"
        );

        if (success) {
            Log.d(TAG, "Appointment cancelled successfully: ID=" + upcomingAppointment.getId());
            Toast.makeText(this, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();

            // Reload to show next appointment or empty state
            loadUpcomingAppointment();
        } else {
            Log.e(TAG, "Failed to cancel appointment: ID=" + upcomingAppointment.getId());
            Toast.makeText(this, "Erreur lors de l'annulation", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Reload data when returning to dashboard
        loadPatientData();
        loadUpcomingAppointment();
    }
}
