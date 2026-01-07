package com.example.gestionrdv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.gestionrdv.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailInput;
    private MaterialButton sendButton;
    private CardView successCard;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();

        backButton.setOnClickListener(v -> finish());

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });

        sendButton.setOnClickListener(v -> handlePasswordReset());
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        emailInput = findViewById(R.id.emailInput);
        sendButton = findViewById(R.id.sendButton);
        successCard = findViewById(R.id.successCard);
        loginLink = findViewById(R.id.loginLink);
    }

    private void handlePasswordReset() {
        String email = emailInput.getText().toString().trim();

        // Validation de l'email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Veuillez entrer votre adresse email");
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Veuillez entrer une adresse email valide");
            return;
        }

        // Effacer l'erreur si l'entrée est valide
        emailInputLayout.setError(null);

        // Simulation de l'envoi du mail (Ici vous connecterez votre API ou Firebase)
        sendResetEmail(email);
    }

    private void sendResetEmail(String email) {
        // On désactive le bouton pour éviter les clics multiples
        sendButton.setEnabled(false);
        sendButton.setText("Envoi en cours...");

        // Simulation d'un délai réseau
        new android.os.Handler().postDelayed(() -> {
            // Afficher le message de succès (successCard) défini dans votre XML
            successCard.setVisibility(View.VISIBLE);

            // Masquer le bouton d'envoi et le champ après succès
            sendButton.setVisibility(View.GONE);
            emailInputLayout.setVisibility(View.GONE);

            Toast.makeText(this, "Lien envoyé à : " + email, Toast.LENGTH_LONG).show();
        }, 2000); // Délai de 2 secondes
    }
}