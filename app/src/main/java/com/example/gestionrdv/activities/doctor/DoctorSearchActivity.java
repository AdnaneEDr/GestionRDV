package com.example.gestionrdv.activities.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.activities.patient.BookAppointmentActivity;
import com.example.gestionrdv.adapters.DoctorAdapter;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DoctorSearchActivity extends AppCompatActivity {

    private static final String TAG = "DoctorSearchActivity";

    private MaterialToolbar toolbar;
    private TextInputEditText searchInput;
    private ChipGroup filterChipGroup;
    private RecyclerView doctorsRecyclerView;
    private LinearLayout emptyStateLayout;

    private DoctorAdapter adapter;
    private DoctorRepository doctorRepository;
    private List<Doctor> allDoctors;
    private List<Doctor> filteredDoctors;

    private boolean isSelectionMode = false;
    private String currentFilter = "Toutes spécialités";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_search);

        // Initialize repository
        doctorRepository = new DoctorRepository(this);

        isSelectionMode = getIntent().getBooleanExtra("selection_mode", false);

        initViews();
        loadDoctorsFromDatabase();
        setupFilterChips();
        setupRecyclerView();
        setupSearch();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchInput = findViewById(R.id.searchInput);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void loadDoctorsFromDatabase() {
        // Fetch all doctors from database
        allDoctors = doctorRepository.getAllDoctors();

        if (allDoctors == null) {
            allDoctors = new ArrayList<>();
        }

        Log.d(TAG, "Loaded " + allDoctors.size() + " doctors from database");

        // Initialize filtered list with all doctors
        filteredDoctors = new ArrayList<>(allDoctors);
    }

    private void setupFilterChips() {
        // Clear existing dynamic chips (keep only the "All" chip)
        Chip allChip = findViewById(R.id.chipAllSpecialties);

        // Get unique specializations from database
        List<String> specializations = doctorRepository.getAllSpecializations();

        // Remove all chips except the first one (Toutes spécialités)
        filterChipGroup.removeAllViews();

        // Re-add the "All specialties" chip
        Chip allSpecialtiesChip = createFilterChip("Toutes spécialités", true);
        allSpecialtiesChip.setId(R.id.chipAllSpecialties);
        filterChipGroup.addView(allSpecialtiesChip);

        // Add chips for each specialization from database
        if (specializations != null) {
            for (String specialization : specializations) {
                if (specialization != null && !specialization.isEmpty()) {
                    Chip chip = createFilterChip(specialization, false);
                    filterChipGroup.addView(chip);
                }
            }
        }

        // Set up listener for chip selection
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // If nothing selected, select "All" chip
                allSpecialtiesChip.setChecked(true);
                currentFilter = "Toutes spécialités";
            } else {
                // Get the selected chip's text
                int checkedId = checkedIds.get(0);
                Chip checkedChip = group.findViewById(checkedId);
                if (checkedChip != null) {
                    currentFilter = checkedChip.getText().toString();
                }
            }
            filterDoctors();
        });
    }

    private Chip createFilterChip(String text, boolean isChecked) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(isChecked);
        chip.setChipBackgroundColorResource(R.color.background_card);
        chip.setChipStrokeColorResource(R.color.primary_medium);
        chip.setChipStrokeWidth(1f);
        chip.setTextColor(getResources().getColor(R.color.text_primary, null));
        chip.setCheckedIconVisible(true);
        chip.setCheckedIconTintResource(R.color.primary_medium);

        // Style when checked
        chip.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                chip.setChipBackgroundColorResource(R.color.primary_light);
                chip.setTextColor(getResources().getColor(R.color.text_white, null));
            } else {
                chip.setChipBackgroundColorResource(R.color.background_card);
                chip.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
        });

        // Apply initial style if checked
        if (isChecked) {
            chip.setChipBackgroundColorResource(R.color.primary_light);
            chip.setTextColor(getResources().getColor(R.color.text_white, null));
        }

        return chip;
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
                // Navigate to book appointment with this doctor
                Intent intent = new Intent(this, BookAppointmentActivity.class);
                intent.putExtra("doctor_id", doctor.getId());
                intent.putExtra("doctor_name", doctor.getFullName());
                intent.putExtra("doctor_specialty", doctor.getSpecialization());
                startActivity(intent);
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

    private void filterDoctors() {
        String searchQuery = searchInput.getText() != null ?
                searchInput.getText().toString().toLowerCase().trim() : "";

        filteredDoctors.clear();

        for (Doctor doctor : allDoctors) {
            // Check search query match
            boolean matchesSearch = searchQuery.isEmpty() ||
                    doctor.getFullName().toLowerCase().contains(searchQuery) ||
                    (doctor.getSpecialization() != null &&
                     doctor.getSpecialization().toLowerCase().contains(searchQuery)) ||
                    (doctor.getLocation() != null &&
                     doctor.getLocation().toLowerCase().contains(searchQuery));

            // Check filter match
            boolean matchesFilter = currentFilter.equals("Toutes spécialités") ||
                    (doctor.getSpecialization() != null &&
                     doctor.getSpecialization().equalsIgnoreCase(currentFilter));

            if (matchesSearch && matchesFilter) {
                filteredDoctors.add(doctor);
            }
        }

        Log.d(TAG, "Filtered to " + filteredDoctors.size() + " doctors (search: '" +
              searchQuery + "', filter: '" + currentFilter + "')");

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

    @Override
    protected void onResume() {
        super.onResume();
        // Reload doctors in case data changed
        loadDoctorsFromDatabase();
        filterDoctors();
    }
}
