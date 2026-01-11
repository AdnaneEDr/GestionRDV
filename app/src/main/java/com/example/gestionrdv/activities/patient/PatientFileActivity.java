package com.example.gestionrdv.activities.patient;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.AppointmentAdapter;
import com.example.gestionrdv.models.Appointment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class PatientFileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView patientAvatar;
    private TextView patientName, patientAge, patientPhone;
    private TextView appointmentDate, appointmentReason;
    private TextView allergiesText, treatmentsText;
    private RecyclerView historyRecycler;
    private TextInputEditText notesInput;
    private MaterialButton saveNotesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_file);

        initViews();
        setupSampleData();
        setupClickListeners();
        setupHistoryRecycler();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        patientAvatar = findViewById(R.id.patientAvatar);
        patientName = findViewById(R.id.patientName);
        patientAge = findViewById(R.id.patientAge);
        patientPhone = findViewById(R.id.patientPhone);
        appointmentDate = findViewById(R.id.appointmentDate);
        appointmentReason = findViewById(R.id.appointmentReason);
        allergiesText = findViewById(R.id.allergiesText);
        treatmentsText = findViewById(R.id.treatmentsText);
        historyRecycler = findViewById(R.id.historyRecycler);
        notesInput = findViewById(R.id.notesInput);
        saveNotesButton = findViewById(R.id.saveNotesButton);
    }

    private void setupSampleData() {
        // Sample patient data
        patientName.setText("Mohammed Alami");
        patientAge.setText("34 ans");
        patientPhone.setText("+212 6 12 34 56 78");
        appointmentDate.setText("Aujourd'hui, 10:30");
        appointmentReason.setText("Consultation générale");
        allergiesText.setText("Aucune allergie connue");
        treatmentsText.setText("Aucun traitement");
    }

    private void setupClickListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Save notes button
        saveNotesButton.setOnClickListener(v -> {
            String notes = notesInput.getText().toString().trim();
            if (!notes.isEmpty()) {
                Toast.makeText(this, "Notes enregistrées", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Veuillez entrer des notes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHistoryRecycler() {
        // Create sample consultation history
        List<Appointment> history = new ArrayList<>();

        Appointment apt1 = new Appointment();
        apt1.setId(1);
        apt1.setAppointmentDate("15 Nov 2024");
        apt1.setAppointmentTime("09:00");
        apt1.setEndTime("09:30");
        apt1.setReason("Grippe saisonnière");
        apt1.setDoctorName("Dr. Fatima Zahra");
        apt1.setDoctorSpecialization("Médecine Générale");
        apt1.setStatus("completed");
        history.add(apt1);

        Appointment apt2 = new Appointment();
        apt2.setId(2);
        apt2.setAppointmentDate("20 Oct 2024");
        apt2.setAppointmentTime("14:30");
        apt2.setEndTime("15:00");
        apt2.setReason("Contrôle annuel");
        apt2.setDoctorName("Dr. Fatima Zahra");
        apt2.setDoctorSpecialization("Médecine Générale");
        apt2.setStatus("completed");
        history.add(apt2);

        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(new AppointmentAdapter(history, new AppointmentAdapter.OnAppointmentClickListener() {
            @Override
            public void onAppointmentClick(Appointment appointment) {
                Toast.makeText(PatientFileActivity.this, "Consultation: " + appointment.getReason(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelClick(Appointment appointment) {
                // Not applicable for history
            }

            @Override
            public void onDetailsClick(Appointment appointment) {
                Toast.makeText(PatientFileActivity.this, "Détails: " + appointment.getReason(), Toast.LENGTH_SHORT).show();
            }
        }, false));
    }
}
