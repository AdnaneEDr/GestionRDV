package com.example.gestionrdv.ui.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gestionrdv.R;
import com.google.android.material.button.MaterialButton;

public class BookAppointmentActivity extends AppCompatActivity {

    MaterialButton buttonConfirmBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_appointment);

        // Window setup... (standard)

        buttonConfirmBooking = findViewById(R.id.buttonConfirmBooking);

        // --- NAVIGATION LOGIC ---

        buttonConfirmBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simulate saving to DB
                Toast.makeText(BookAppointmentActivity.this, "Booking Confirmed!", Toast.LENGTH_LONG).show();

                // Close this page and return to Home
                finish();
            }
        });
    }
}