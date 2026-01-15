package com.example.gestionrdv.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.patient.PatientDashboardActivity;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.database.repositories.UserRepository;
import com.example.gestionrdv.models.Patient;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etEmail, etPhone,
            etBirthDate, etPassword, etConfirmPassword;

    private TextInputLayout passwordLayout, confirmPasswordLayout;
    private TextView tvLoginLink;
    private MaterialButton btnSignup;

    // Password requirements
    private TextView reqLength, reqUppercase, reqNumber, reqSpecial;

    // Repositories
    private UserRepository userRepository;
    private PatientRepository patientRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize repositories
        userRepository = new UserRepository(this);
        patientRepository = new PatientRepository(this);

        initViews();
        setupDatePicker();
        setupPasswordValidation();

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnSignup.setOnClickListener(v -> handleSignup());
    }

    private void initViews() {
        etFirstName = findViewById(R.id.firstnameInput);
        etLastName = findViewById(R.id.lastnameInput);
        etEmail = findViewById(R.id.emailInput);
        etPhone = findViewById(R.id.phoneInput);
        etBirthDate = findViewById(R.id.birthdateInput);
        etPassword = findViewById(R.id.passwordInput);
        etConfirmPassword = findViewById(R.id.confirmPasswordInput);

        passwordLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordInputLayout);

        btnSignup = findViewById(R.id.signupButton);
        tvLoginLink = findViewById(R.id.loginLink);

        reqLength = findViewById(R.id.reqLength);
        reqUppercase = findViewById(R.id.reqUppercase);
        reqNumber = findViewById(R.id.reqNumber);
        reqSpecial = findViewById(R.id.reqSpecial);
    }

    private void setupDatePicker() {
        etBirthDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(
                                Locale.FRANCE,
                                "%02d/%02d/%d",
                                dayOfMonth,
                                month + 1,
                                year
                        );
                        etBirthDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR) - 20, // Default to 20 years ago
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupPasswordValidation() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pass = s.toString();

                updateRequirement(reqLength, pass.length() >= 8);
                updateRequirement(reqUppercase, pass.matches(".*[A-Z].*"));
                updateRequirement(reqNumber, pass.matches(".*\\d.*"));
                updateRequirement(reqSpecial, pass.matches(".*[@#$%^&+=!].*"));
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateRequirement(TextView tv, boolean isMet) {
        int color = isMet ? R.color.primary_medium : R.color.text_hint;
        int icon = isMet ? R.drawable.ic_check : R.drawable.ic_circle_outline;

        tv.setTextColor(ContextCompat.getColor(this, color));
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    private void handleSignup() {
        // Get all input values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate required fields
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("Prénom requis");
            etFirstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Nom requis");
            etLastName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            etEmail.requestFocus();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format d'email invalide");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Téléphone requis");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(birthDate)) {
            etBirthDate.setError("Date de naissance requise");
            etBirthDate.requestFocus();
            return;
        }

        // Validate password requirements
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Le mot de passe ne respecte pas les critères requis", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Les mots de passe ne correspondent pas");
            return;
        } else {
            confirmPasswordLayout.setError(null);
        }

        // Check if email already exists
        if (userRepository.emailExists(email)) {
            etEmail.setError("Cet email est déjà utilisé");
            etEmail.requestFocus();
            return;
        }

        // Register user as PATIENT (userType = "patient")
        long userId = userRepository.registerUser(email, password, "patient");

        if (userId == -1) {
            Toast.makeText(this, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create patient profile
        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setPhone(phone);
        patient.setBirthDate(birthDate);

        long patientId = patientRepository.addPatient(patient);

        if (patientId == -1) {
            Toast.makeText(this, "Erreur lors de la création du profil patient", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save session data (userId, userType, profileId, email, fullName)
        String fullName = firstName + " " + lastName;
        saveUserSession(userId, "patient", patientId, email, fullName);

        Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_LONG).show();

        // Navigate to Patient Dashboard
        Intent intent = new Intent(this, PatientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Validate password meets all requirements
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[@#$%^&+=!].*");
    }

    /**
     * Save user session using SessionManager
     */
    private void saveUserSession(long userId, String userType, long profileId, String email, String fullName) {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.createLoginSession(userId, userType, email, profileId, fullName);
    }
}