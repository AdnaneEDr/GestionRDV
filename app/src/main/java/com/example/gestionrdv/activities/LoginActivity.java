package com.example.gestionrdv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.admin.AdminDashboardActivity;
import com.example.gestionrdv.activities.doctor.DoctorDashboardActivity;
import com.example.gestionrdv.activities.patient.PatientDashboardActivity;
import com.example.gestionrdv.models.Patient;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView signupLink, forgotPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.emailInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        btnLogin.setOnClickListener(v -> {
            performLogin();
        });

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

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("L'email est requis");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Le mot de passe est requis");
            return;
        }

        // Logique de redirection basée sur la structure du projet
        if (email.equals("admin@test.com") && password.equals("admin123")) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        }
        else if (email.contains("doctor")) {
            startActivity(new Intent(this, DoctorDashboardActivity.class));
            finish();
        }
        else if (password.length() >= 6) {
            startActivity(new Intent(this, PatientDashboardActivity.class));
            finish();
        }
        else {
            Toast.makeText(this, "Identifiants invalides", Toast.LENGTH_SHORT).show();
        }
    }
}