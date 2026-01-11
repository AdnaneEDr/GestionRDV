package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.LoginActivity;
import com.example.gestionrdv.activities.doctor.DoctorSearchActivity;
import com.example.gestionrdv.models.Appointment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardActivity extends AppCompatActivity {

    private TextView greetingText, patientNameText;
    private TextView doctorName, doctorSpecialty, appointmentDate, appointmentTime;
    private MaterialButton quickBookButton, cancelButton, modifyButton;
    private TextView seeAllAppointmentsLink;
    private CardView upcomingAppointmentCard;
    private ImageButton notificationsButton;
    private BottomNavigationView bottomNavigation;

    // Sample data for upcoming appointment
    private Appointment upcomingAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        initViews();
        setupSampleData();
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
        quickBookButton = findViewById(R.id.quickBookButton);
        cancelButton = findViewById(R.id.cancelButton);
        modifyButton = findViewById(R.id.modifyButton);
        seeAllAppointmentsLink = findViewById(R.id.seeAllAppointmentsLink);
        upcomingAppointmentCard = findViewById(R.id.upcomingAppointmentCard);
        notificationsButton = findViewById(R.id.notificationsButton);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupSampleData() {
        // Set greeting based on time of day
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            greetingText.setText("Bonjour,");
        } else if (hour < 18) {
            greetingText.setText("Bon après-midi,");
        } else {
            greetingText.setText("Bonsoir,");
        }

        patientNameText.setText("Mohammed Alami");

        // Sample upcoming appointment
        upcomingAppointment = new Appointment();
        upcomingAppointment.setId(1);
        upcomingAppointment.setDoctorName("Dr. Fatima Zahra");
        upcomingAppointment.setDoctorSpecialization("Médecine Générale");
        upcomingAppointment.setAppointmentDate("25 Déc 2024");
        upcomingAppointment.setAppointmentTime("10:30");
        upcomingAppointment.setEndTime("11:00");
        upcomingAppointment.setStatus("confirmed");
        upcomingAppointment.setReason("Consultation générale");

        // Display upcoming appointment
        doctorName.setText(upcomingAppointment.getDoctorName());
        doctorSpecialty.setText(upcomingAppointment.getDoctorSpecialization());
        appointmentDate.setText(upcomingAppointment.getAppointmentDate());
        appointmentTime.setText(upcomingAppointment.getAppointmentTime());
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
            Intent intent = new Intent(this, AppointmentDetailsActivity.class);
            intent.putExtra("appointment_id", upcomingAppointment.getId());
            startActivity(intent);
        });

        // Cancel appointment button
        cancelButton.setOnClickListener(v -> {
            showCancelConfirmationDialog();
        });

        // Modify appointment button
        modifyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectDateTimeActivity.class);
            intent.putExtra("appointment_id", upcomingAppointment.getId());
            intent.putExtra("is_modification", true);
            startActivity(intent);
        });

        // Notifications button
        notificationsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
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
                    // Cancel the appointment
                    Toast.makeText(this, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                    upcomingAppointmentCard.setVisibility(View.GONE);
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}
