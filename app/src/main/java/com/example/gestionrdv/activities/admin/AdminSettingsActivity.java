package com.example.gestionrdv.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.example.gestionrdv.database.repositories.AdminRepository;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * AdminSettingsActivity - Application settings and admin profile
 */
public class AdminSettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView adminNameText, adminEmailText;
    private TextView totalDoctorsText, totalPatientsText, totalAppointmentsText;
    private CardView profileCard, statsCard;
    private LinearLayout changePasswordLayout, notificationsLayout, aboutLayout, logoutLayout;
    private MaterialButton deleteAllDataButton;

    private SessionManager sessionManager;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings_new);

        sessionManager = new SessionManager(this);
        doctorRepository = new DoctorRepository(this);
        patientRepository = new PatientRepository(this);
        appointmentRepository = new AppointmentRepository(this);

        initViews();
        setupToolbar();
        loadAdminInfo();
        loadStats();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        adminNameText = findViewById(R.id.adminNameText);
        adminEmailText = findViewById(R.id.adminEmailText);
        totalDoctorsText = findViewById(R.id.totalDoctorsText);
        totalPatientsText = findViewById(R.id.totalPatientsText);
        totalAppointmentsText = findViewById(R.id.totalAppointmentsText);
        profileCard = findViewById(R.id.profileCard);
        statsCard = findViewById(R.id.statsCard);
        changePasswordLayout = findViewById(R.id.changePasswordLayout);
        notificationsLayout = findViewById(R.id.notificationsLayout);
        aboutLayout = findViewById(R.id.aboutLayout);
        logoutLayout = findViewById(R.id.logoutLayout);
        deleteAllDataButton = findViewById(R.id.deleteAllDataButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadAdminInfo() {
        String fullName = sessionManager.getFullName();
        String email = sessionManager.getEmail();

        adminNameText.setText(fullName != null ? fullName : "Administrateur");
        adminEmailText.setText(email != null ? email : "admin@cabinet.ma");
    }

    private void loadStats() {
        int doctorsCount = doctorRepository.getAllDoctors().size();
        int patientsCount = patientRepository.getAllPatients().size();
        int appointmentsCount = appointmentRepository.getAllAppointments().size();

        totalDoctorsText.setText(String.valueOf(doctorsCount));
        totalPatientsText.setText(String.valueOf(patientsCount));
        totalAppointmentsText.setText(String.valueOf(appointmentsCount));
    }

    private void setupClickListeners() {
        changePasswordLayout.setOnClickListener(v -> changePassword());
        notificationsLayout.setOnClickListener(v -> manageNotifications());
        aboutLayout.setOnClickListener(v -> showAboutDialog());
        logoutLayout.setOnClickListener(v -> confirmLogout());
        deleteAllDataButton.setOnClickListener(v -> confirmDeleteAllData());
    }

    private void changePassword() {
        // Create custom dialog layout
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(android.R.layout.simple_list_item_1, null);

        // Create EditText fields programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.EditText currentPasswordInput = new android.widget.EditText(this);
        currentPasswordInput.setHint("Mot de passe actuel");
        currentPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        final android.widget.EditText newPasswordInput = new android.widget.EditText(this);
        newPasswordInput.setHint("Nouveau mot de passe");
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 20;
        newPasswordInput.setLayoutParams(params);
        layout.addView(newPasswordInput);

        final android.widget.EditText confirmPasswordInput = new android.widget.EditText(this);
        confirmPasswordInput.setHint("Confirmer le nouveau mot de passe");
        confirmPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordInput.setLayoutParams(params);
        layout.addView(confirmPasswordInput);

        new AlertDialog.Builder(this)
                .setTitle("Changer le mot de passe")
                .setView(layout)
                .setPositiveButton("Changer", (dialog, which) -> {
                    String currentPassword = currentPasswordInput.getText().toString().trim();
                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();

                    // Validation
                    if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get admin ID from session
                    int adminId = (int) sessionManager.getUserId();

                    // Verify current password and update
                    AdminRepository adminRepository = new AdminRepository(this);
                    if (adminRepository.verifyAdminPassword(adminId, currentPassword)) {
                        if (adminRepository.updateAdminPassword(adminId, newPassword)) {
                            Toast.makeText(this, "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erreur lors du changement de mot de passe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Mot de passe actuel incorrect", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void manageNotifications() {
        String[] options = {"Notifications RDV", "Notifications système", "Emails"};
        boolean[] checked = {true, true, false};

        new AlertDialog.Builder(this)
                .setTitle("Paramètres de notification")
                .setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> {
                    // TODO: Save notification preferences
                })
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    Toast.makeText(this, "Préférences enregistrées", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAboutDialog() {
        String aboutText = "GestionRDV v1.0\n\n" +
                "Application de gestion de rendez-vous médicaux\n\n" +
                "Développé par Anthropic Claude\n" +
                "© 2026 Tous droits réservés\n\n" +
                "Fonctionnalités:\n" +
                "• Gestion des médecins\n" +
                "• Gestion des patients\n" +
                "• Calendrier des rendez-vous\n" +
                "• Statistiques en temps réel\n" +
                "• Interface intuitive";

        new AlertDialog.Builder(this)
                .setTitle("À propos")
                .setMessage(aboutText)
                .setPositiveButton("OK", null)
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

        Intent intent = new Intent(AdminSettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmDeleteAllData() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ DANGER")
                .setMessage("Cette action supprimera TOUTES les données de l'application " +
                        "(médecins, patients, rendez-vous).\n\n" +
                        "Cette action est IRRÉVERSIBLE !\n\n" +
                        "Voulez-vous vraiment continuer ?")
                .setPositiveButton("SUPPRIMER TOUT", (dialog, which) -> {
                    confirmDeleteAgain();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void confirmDeleteAgain() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ DERNIÈRE CONFIRMATION")
                .setMessage("Êtes-vous ABSOLUMENT SÛR ?\n\n" +
                        "Toutes les données seront perdues définitivement.")
                .setPositiveButton("OUI, SUPPRIMER", (dialog, which) -> {
                    deleteAllData();
                })
                .setNegativeButton("Non, annuler", null)
                .show();
    }

    private void deleteAllData() {
        // TODO: Implement complete data deletion
        Toast.makeText(this, "Fonctionnalité de suppression - En développement", Toast.LENGTH_LONG).show();

        // This would require:
        // 1. Delete all appointments
        // 2. Delete all patients
        // 3. Delete all doctors
        // 4. Reset database
        // 5. Logout and redirect to init screen
    }
}