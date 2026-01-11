package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.doctor.DoctorSearchActivity;
import com.example.gestionrdv.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class BookAppointmentActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CardView selectedDoctorCard;
    private ImageView doctorAvatar;
    private TextView doctorName, doctorSpecialty;
    private ChipGroup reasonChipGroup;
    private TextInputEditText notesInput;
    private MaterialButton nextButton;

    // Selected data
    private Doctor selectedDoctor = null;
    private String selectedReason = "Consultation générale";

    // Activity result launcher for doctor selection
    private ActivityResultLauncher<Intent> doctorSelectionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        initViews();
        setupDoctorSelectionLauncher();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        selectedDoctorCard = findViewById(R.id.selectedDoctorCard);
        doctorAvatar = findViewById(R.id.doctorAvatar);
        doctorName = findViewById(R.id.doctorName);
        doctorSpecialty = findViewById(R.id.doctorSpecialty);
        reasonChipGroup = findViewById(R.id.reasonChipGroup);
        notesInput = findViewById(R.id.notesInput);
        nextButton = findViewById(R.id.nextButton);
    }

    private void setupDoctorSelectionLauncher() {
        doctorSelectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Get selected doctor data from intent
                        long doctorId = result.getData().getLongExtra("doctor_id", -1);
                        String name = result.getData().getStringExtra("doctor_name");
                        String specialty = result.getData().getStringExtra("doctor_specialty");

                        if (doctorId != -1 && name != null) {
                            selectedDoctor = new Doctor();
                            selectedDoctor.setId(doctorId);
                            String[] nameParts = name.replace("Dr. ", "").split(" ");
                            if (nameParts.length >= 2) {
                                selectedDoctor.setFirstName(nameParts[0]);
                                selectedDoctor.setLastName(nameParts[1]);
                            } else {
                                selectedDoctor.setFirstName(nameParts[0]);
                                selectedDoctor.setLastName("");
                            }
                            selectedDoctor.setSpecialization(specialty);

                            // Update UI
                            doctorName.setText(name);
                            doctorSpecialty.setText(specialty);
                        }
                    }
                }
        );
    }

    private void setupClickListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Select doctor card
        selectedDoctorCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorSearchActivity.class);
            intent.putExtra("selection_mode", true);
            doctorSelectionLauncher.launch(intent);
        });

        // Reason chip group
        reasonChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    selectedReason = selectedChip.getText().toString();
                }
            }
        });

        // Next button
        nextButton.setOnClickListener(v -> {
            if (validateSelection()) {
                proceedToDateSelection();
            }
        });
    }

    private boolean validateSelection() {
        if (selectedDoctor == null) {
            Toast.makeText(this, "Veuillez sélectionner un médecin", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void proceedToDateSelection() {
        Intent intent = new Intent(this, SelectDateTimeActivity.class);
        intent.putExtra("doctor_id", selectedDoctor.getId());
        intent.putExtra("doctor_name", selectedDoctor.getFullName());
        intent.putExtra("doctor_specialty", selectedDoctor.getSpecialization());
        intent.putExtra("reason", selectedReason);

        String notes = notesInput.getText() != null ? notesInput.getText().toString().trim() : "";
        intent.putExtra("notes", notes);

        startActivity(intent);
    }
}
