package com.example.gestionrdv.activities.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DoctorDashboardActivity extends AppCompatActivity {

    private TextView dateText;
    private ImageButton notificationsButton;
    private MaterialButton manageScheduleButton, viewStatsButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        sessionManager = new SessionManager(this);

        initViews();
        setupCurrentDate();
        setupClickListeners();
    }

    private void initViews() {
        dateText = findViewById(R.id.dateText);
        notificationsButton = findViewById(R.id.notificationsButton);
        manageScheduleButton = findViewById(R.id.manageScheduleButton);
        viewStatsButton = findViewById(R.id.viewStatsButton);
    }

    private void setupCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.FRENCH);
        String currentDate = sdf.format(new Date());
        dateText.setText(currentDate);
    }

    private void setupClickListeners() {
        notificationsButton.setOnClickListener(v ->
                Toast.makeText(this, "Notifications - En développement", Toast.LENGTH_SHORT).show()
        );

        manageScheduleButton.setOnClickListener(v -> {
            // BOUTON TEMPORAIRE DE DÉCONNEXION
            logout();
        });

        viewStatsButton.setOnClickListener(v ->
                Toast.makeText(this, "Statistiques - En développement", Toast.LENGTH_SHORT).show()
        );
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DoctorDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}