package com.example.gestionrdv.activities.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.DoctorAdapter;
import com.example.gestionrdv.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DoctorSearchActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText searchInput;
    private ChipGroup filterChipGroup;
    private RecyclerView doctorsRecyclerView;
    private LinearLayout emptyStateLayout;

    private DoctorAdapter adapter;
    private List<Doctor> allDoctors;
    private List<Doctor> filteredDoctors;

    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_search);

        isSelectionMode = getIntent().getBooleanExtra("selection_mode", false);

        initViews();
        setupSampleData();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchInput = findViewById(R.id.searchInput);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupSampleData() {
        allDoctors = new ArrayList<>();

        Doctor doc1 = new Doctor("Fatima", "Zahra", "Médecine générale");
        doc1.setId(1);
        doc1.setExperience(15);
        doc1.setRating(4.8);
        doc1.setLocation("Tanger");
        allDoctors.add(doc1);

        Doctor doc2 = new Doctor("Ahmed", "Bennani", "Cardiologie");
        doc2.setId(2);
        doc2.setExperience(20);
        doc2.setRating(4.9);
        doc2.setLocation("Casablanca");
        allDoctors.add(doc2);

        Doctor doc3 = new Doctor("Sara", "El Amrani", "Dermatologie");
        doc3.setId(3);
        doc3.setExperience(10);
        doc3.setRating(4.7);
        doc3.setLocation("Rabat");
        allDoctors.add(doc3);

        Doctor doc4 = new Doctor("Karim", "Idrissi", "Pédiatrie");
        doc4.setId(4);
        doc4.setExperience(12);
        doc4.setRating(4.6);
        doc4.setLocation("Tanger");
        allDoctors.add(doc4);

        Doctor doc5 = new Doctor("Nadia", "Chakir", "Médecine générale");
        doc5.setId(5);
        doc5.setExperience(8);
        doc5.setRating(4.5);
        doc5.setLocation("Marrakech");
        allDoctors.add(doc5);

        filteredDoctors = new ArrayList<>(allDoctors);
    }

    private void setupRecyclerView() {
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DoctorAdapter(filteredDoctors, doctor -> {
            if (isSelectionMode) {
                // Return selected doctor to the calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("doctor_id", doctor.getId());
                resultIntent.putExtra("doctor_name", doctor.getFullName());
                resultIntent.putExtra("doctor_specialty", doctor.getSpecialization());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // TODO: Open doctor profile/details
                // For now, just show a toast or navigate to booking
            }
        });

        doctorsRecyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterDoctors();
        });
    }

    private void filterDoctors() {
        String searchQuery = searchInput.getText() != null ?
                searchInput.getText().toString().toLowerCase().trim() : "";

        // Get selected filter
        String selectedSpecialty = "Toutes spécialités";
        int checkedChipId = filterChipGroup.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip checkedChip = findViewById(checkedChipId);
            if (checkedChip != null) {
                selectedSpecialty = checkedChip.getText().toString();
            }
        }

        filteredDoctors.clear();

        for (Doctor doctor : allDoctors) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    doctor.getFullName().toLowerCase().contains(searchQuery) ||
                    doctor.getSpecialization().toLowerCase().contains(searchQuery) ||
                    doctor.getLocation().toLowerCase().contains(searchQuery);

            boolean matchesFilter = selectedSpecialty.equals("Toutes spécialités") ||
                    doctor.getSpecialization().equalsIgnoreCase(selectedSpecialty);

            if (matchesSearch && matchesFilter) {
                filteredDoctors.add(doctor);
            }
        }

        adapter.updateDoctors(filteredDoctors);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredDoctors.isEmpty()) {
            doctorsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            doctorsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> {
            if (isSelectionMode) {
                setResult(RESULT_CANCELED);
            }
            finish();
        });
    }
}
