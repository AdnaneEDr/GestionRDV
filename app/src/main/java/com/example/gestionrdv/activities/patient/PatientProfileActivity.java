package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PatientProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView profileImage;
    private TextView patientName, patientEmail;
    private TextView phoneText, birthdateText, addressText;
    private TextView totalAppointments, completedAppointments, cancelledAppointments;
    private MaterialButton editInfoButton, logoutButton;
    private LinearLayout notificationsOption, changePasswordOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        initViews();
        setupSampleData();
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

    private void setupSampleData() {
        // Sample patient data
        patientName.setText("Mohammed Alami");
        patientEmail.setText("mohammed.alami@email.com");
        phoneText.setText("+212 6 12 34 56 78");
        birthdateText.setText("15 Mars 1990");
        addressText.setText("Tanger, Maroc");

        // Statistics
        totalAppointments.setText("24");
        completedAppointments.setText("22");
        cancelledAppointments.setText("2");
    }

    private void setupClickListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Edit info button
        editInfoButton.setOnClickListener(v -> {
            Toast.makeText(this, "Modifier les informations", Toast.LENGTH_SHORT).show();
            // TODO: Open edit profile dialog or activity
        });

        // Notifications option
        notificationsOption.setOnClickListener(v -> {
            Toast.makeText(this, "Paramètres de notifications", Toast.LENGTH_SHORT).show();
            // TODO: Open notifications settings
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
                    // Clear session and go to login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
