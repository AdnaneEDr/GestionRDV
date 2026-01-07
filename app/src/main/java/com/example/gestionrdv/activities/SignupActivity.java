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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

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
                    calendar.get(Calendar.YEAR),
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

        if (TextUtils.isEmpty(etFirstName.getText())) {
            etFirstName.setError("Prénom requis");
            return;
        }

        if (TextUtils.isEmpty(etEmail.getText())) {
            etEmail.setError("Email requis");
            return;
        }

        String pass = etPassword.getText().toString();
        String confirmPass = etConfirmPassword.getText().toString();

        if (!pass.equals(confirmPass)) {
            confirmPasswordLayout.setError("Les mots de passe ne correspondent pas");
            return;
        } else {
            confirmPasswordLayout.setError(null);
        }

        Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_LONG).show();

        startActivity(new Intent(this, PatientDashboardActivity.class));
        finish();
    }
}
