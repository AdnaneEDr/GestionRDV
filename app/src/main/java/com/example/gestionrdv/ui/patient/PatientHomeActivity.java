package com.example.gestionrdv.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gestionrdv.R;
import com.google.android.material.button.MaterialButton; // Import for MaterialButton

public class PatientHomeActivity extends AppCompatActivity {

    MaterialButton buttonBookAppointment, buttonViewAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerSimple), (v, insets) -> {
            // Using headerSimple as root for insets if 'main' doesn't exist in ConstraintLayout
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        buttonBookAppointment = findViewById(R.id.buttonBookAppointment);
        buttonViewAll = findViewById(R.id.buttonViewAll);

        // --- NAVIGATION LOGIC ---

        // 1. Book Appointment -> Go to Booking Page
        buttonBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientHomeActivity.this, BookAppointmentActivity.class);
                startActivity(intent);
            }
        });

        // 2. View All -> Toast (or you can create a list activity later)
        buttonViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PatientHomeActivity.this, "Viewing all appointments...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}