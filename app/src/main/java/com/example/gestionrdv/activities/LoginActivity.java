package com.example.gestionrdv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.admin.AdminDashboardActivity;
import com.example.gestionrdv.activities.doctor.DoctorDashboardActivity;
import com.example.gestionrdv.activities.patient.PatientDashboardActivity;
import com.example.gestionrdv.database.repositories.AdminRepository;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.database.repositories.UserRepository;
import com.example.gestionrdv.models.Admin;
import com.example.gestionrdv.models.Doctor;
import com.example.gestionrdv.models.Patient;
import com.example.gestionrdv.models.User;
import com.example.gestionrdv.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView signupLink, forgotPasswordLink;

    // Repositories
    private UserRepository userRepository;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private AdminRepository adminRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize repositories
        userRepository = new UserRepository(this);
        patientRepository = new PatientRepository(this);
        doctorRepository = new DoctorRepository(this);
        adminRepository = new AdminRepository(this);

        // Check if already logged in
        if (isUserLoggedIn()) {
            redirectToAppropriateActivity();
            return;
        }

        etEmail = findViewById(R.id.emailInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        btnLogin.setOnClickListener(v -> performLogin());

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        forgotPasswordLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("L'email est requis");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Le mot de passe est requis");
            etPassword.requestFocus();
            return;
        }

        // Authenticate user with database
        User user = userRepository.login(email, password);

        if (user == null) {
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user type and redirect accordingly
        String userType = user.getUserType();
        long userId = user.getId();

        switch (userType.toLowerCase()) {
            case "patient":
                handlePatientLogin(userId);
                break;

            case "doctor":
            case "medecin":
                handleDoctorLogin(userId);
                break;

            case "admin":
            case "secretaire":
                handleAdminLogin(userId);
                break;

            default:
                Toast.makeText(this, "Type d'utilisateur invalide", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle Patient login
     */
    private void handlePatientLogin(long userId) {
        Patient patient = patientRepository.getPatientByUserId(userId);

        if (patient == null) {
            Toast.makeText(this, "Profil patient introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user email
        User user = userRepository.getUserById(userId);
        String email = user != null ? user.getEmail() : "";
        String fullName = patient.getFirstName() + " " + patient.getLastName();

        // Save session using SessionManager
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.createLoginSession(userId, "patient", email, patient.getId(), fullName);

        // Navigate to Patient Dashboard
        Intent intent = new Intent(this, PatientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Handle Doctor login
     */
    private void handleDoctorLogin(long userId) {
        Doctor doctor = doctorRepository.getDoctorByUserId(userId);

        if (doctor == null) {
            Toast.makeText(this, "Profil médecin introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user email
        User user = userRepository.getUserById(userId);
        String email = user != null ? user.getEmail() : "";
        String fullName = doctor.getFirstName() + " " + doctor.getLastName();

        // Save session using SessionManager
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.createLoginSession(userId, "doctor", email, doctor.getId(), fullName);

        // Navigate to Doctor Dashboard
        Intent intent = new Intent(this, DoctorDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Handle Admin login
     */
    private void handleAdminLogin(long userId) {
        Admin admin = adminRepository.getAdminByUserId(userId);

        if (admin == null) {
            Toast.makeText(this, "Profil administrateur introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user email
        User user = userRepository.getUserById(userId);
        String email = user != null ? user.getEmail() : "";
        String fullName = admin.getFirstName() + " " + admin.getLastName();

        // Save session using SessionManager
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.createLoginSession(userId, "admin", email, admin.getId(), fullName);

        // Navigate to Admin Dashboard
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Check if user is already logged in
     */
    private boolean isUserLoggedIn() {
        SessionManager sessionManager = new SessionManager(this);
        return sessionManager.isLoggedIn();
    }

    /**
     * Redirect to appropriate dashboard based on saved session
     */
    private void redirectToAppropriateActivity() {
        SessionManager sessionManager = new SessionManager(this);
        String userType = sessionManager.getUserType();

        if (userType == null) return;

        Intent intent;
        switch (userType.toLowerCase()) {
            case "patient":
                intent = new Intent(this, PatientDashboardActivity.class);
                break;
            case "doctor":
            case "medecin":
                intent = new Intent(this, DoctorDashboardActivity.class);
                break;
            case "admin":
            case "secretaire":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                return; // Don't redirect if invalid
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}