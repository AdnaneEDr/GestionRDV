package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.models.Patient;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientProfileActivity extends AppCompatActivity {

    private static final String TAG = "PatientProfileActivity";

    // Views
    private MaterialToolbar toolbar;
    private ImageView profileImage;
    private TextView patientName, patientEmail;
    private TextView phoneText, birthdateText, addressText;
    private TextView totalAppointments, completedAppointments, cancelledAppointments;
    private MaterialButton editInfoButton, logoutButton;
    private LinearLayout notificationsOption, changePasswordOption;

    // Data
    private SessionManager sessionManager;
    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private Patient currentPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        // Initialize repositories and session
        sessionManager = new SessionManager(this);
        patientRepository = new PatientRepository(this);
        appointmentRepository = new AppointmentRepository(this);

        initViews();
        loadPatientData();
        loadAppointmentStatistics();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profileImage);
        patientName = findViewById(R.id.patientName);
        patientEmail = findViewById(R.id.patientEmail);
        phoneText = findViewById(R.id.phoneText);
        birthdateText = findViewById(R.id.birthdateText);
        addressText = findViewById(R.id.addressText);
        totalAppointments = findViewById(R.id.totalAppointments);
        completedAppointments = findViewById(R.id.completedAppointments);
        cancelledAppointments = findViewById(R.id.cancelledAppointments);
        editInfoButton = findViewById(R.id.editInfoButton);
        logoutButton = findViewById(R.id.logoutButton);
        notificationsOption = findViewById(R.id.notificationsOption);
        changePasswordOption = findViewById(R.id.changePasswordOption);
    }

    private void loadPatientData() {
        long patientId = sessionManager.getProfileId();

        if (patientId == -1) {
            Log.e(TAG, "Invalid patient ID from session");
            showDefaultData();
            return;
        }

        // Fetch patient from database
        currentPatient = patientRepository.getPatientById(patientId);

        if (currentPatient != null) {
            displayPatientData(currentPatient);
        } else {
            Log.e(TAG, "Patient not found in database: ID=" + patientId);
            showDefaultData();
        }
    }

    private void displayPatientData(Patient patient) {
        // Name
        patientName.setText(patient.getFullName());

        // Email from session
        String email = sessionManager.getEmail();
        patientEmail.setText(email != null ? email : "Email non disponible");

        // Phone
        String phone = patient.getPhone();
        phoneText.setText(phone != null && !phone.isEmpty() ? phone : "Non renseigné");

        // Birthdate - format for display
        String birthDate = patient.getBirthDate();
        birthdateText.setText(formatDateForDisplay(birthDate));

        // Address
        String address = patient.getAddress();
        addressText.setText(address != null && !address.isEmpty() ? address : "Non renseigné");

        Log.d(TAG, "Displayed patient data: " + patient.getFullName());
    }

    private void showDefaultData() {
        // Fallback to session data if available
        String fullName = sessionManager.getFullName();
        String email = sessionManager.getEmail();

        patientName.setText(fullName != null ? fullName : "Patient");
        patientEmail.setText(email != null ? email : "Email non disponible");
        phoneText.setText("Non renseigné");
        birthdateText.setText("Non renseigné");
        addressText.setText("Non renseigné");
    }

    private void loadAppointmentStatistics() {
        long patientId = sessionManager.getProfileId();

        if (patientId == -1) {
            setDefaultStatistics();
            return;
        }

        // Get all appointments for this patient
        List<Appointment> allAppointments = appointmentRepository.getPatientAppointments(patientId);

        if (allAppointments == null || allAppointments.isEmpty()) {
            setDefaultStatistics();
            return;
        }

        int total = allAppointments.size();
        int completed = 0;
        int cancelled = 0;

        for (Appointment apt : allAppointments) {
            String status = apt.getStatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "completed":
                        completed++;
                        break;
                    case "cancelled":
                        cancelled++;
                        break;
                }
            }
        }

        totalAppointments.setText(String.valueOf(total));
        completedAppointments.setText(String.valueOf(completed));
        cancelledAppointments.setText(String.valueOf(cancelled));

        Log.d(TAG, "Loaded statistics: total=" + total + ", completed=" + completed + ", cancelled=" + cancelled);
    }

    private void setDefaultStatistics() {
        totalAppointments.setText("0");
        completedAppointments.setText("0");
        cancelledAppointments.setText("0");
    }

    private String formatDateForDisplay(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) {
            return "Non renseigné";
        }

        try {
            // Try parsing as database format (yyyy-MM-dd)
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
            Date date = dbFormat.parse(dbDate);
            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            // Try parsing as dd/MM/yyyy format
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
                Date date = altFormat.parse(dbDate);
                if (date != null) {
                    return displayFormat.format(date);
                }
            } catch (ParseException e2) {
                Log.e(TAG, "Error parsing date: " + dbDate, e2);
            }
        }

        // Return original if parsing fails
        return dbDate;
    }

    private void setupClickListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Edit info button
        editInfoButton.setOnClickListener(v -> {
            showEditProfileDialog();
        });

        // Notifications option
        notificationsOption.setOnClickListener(v -> {
            Toast.makeText(this, "Paramètres de notifications", Toast.LENGTH_SHORT).show();
            // TODO: Open notifications settings activity
        });

        // Change password option
        changePasswordOption.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
    }

    private void showEditProfileDialog() {
        // For now, show a simple dialog. In a full implementation, this would open an edit activity.
        new AlertDialog.Builder(this)
                .setTitle("Modifier le profil")
                .setMessage("Cette fonctionnalité sera bientôt disponible.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showChangePasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Changer le mot de passe")
                .setMessage("Cette fonctionnalité sera bientôt disponible.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void performLogout() {
        // Clear session data
        sessionManager.logout();

        Log.d(TAG, "User logged out successfully");

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to profile
        loadPatientData();
        loadAppointmentStatistics();
    }
}
