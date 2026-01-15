package com.example.gestionrdv.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.AdminPatientAdapter;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.models.Patient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * AdminPatientsActivity - Manage all patients
 */
public class AdminPatientsActivity extends AppCompatActivity implements AdminPatientAdapter.OnPatientActionListener {

    private MaterialToolbar toolbar;
    private TextInputEditText searchInput;
    private RecyclerView patientsRecycler;
    private TextView emptyStateText, totalPatientsText;
    
    private AdminPatientAdapter adapter;
    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private List<Patient> allPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_patients);

        patientRepository = new PatientRepository(this);
        appointmentRepository = new AppointmentRepository(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        loadPatients();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchInput = findViewById(R.id.searchInput);
        patientsRecycler = findViewById(R.id.patientsRecycler);
        emptyStateText = findViewById(R.id.emptyStateText);
        totalPatientsText = findViewById(R.id.totalPatientsText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminPatientAdapter(this, appointmentRepository);
        patientsRecycler.setLayoutManager(new LinearLayoutManager(this));
        patientsRecycler.setAdapter(adapter);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPatients() {
        allPatients = patientRepository.getAllPatients();
        adapter.setPatients(allPatients);
        updateUI();
    }

    private void filterPatients(String query) {
        if (query.isEmpty()) {
            adapter.setPatients(allPatients);
        } else {
            List<Patient> filtered = patientRepository.searchPatientsByName(query);
            adapter.setPatients(filtered);
        }
        updateUI();
    }

    private void updateUI() {
        int count = adapter.getItemCount();
        totalPatientsText.setText(count + " patient(s)");
        
        if (count == 0) {
            patientsRecycler.setVisibility(android.view.View.GONE);
            emptyStateText.setVisibility(android.view.View.VISIBLE);
        } else {
            patientsRecycler.setVisibility(android.view.View.VISIBLE);
            emptyStateText.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public void onPatientClick(Patient patient) {
        showPatientDetailsDialog(patient);
    }

    @Override
    public void onPatientMenuClick(Patient patient) {
        showPatientOptionsDialog(patient);
    }

    private void showPatientDetailsDialog(Patient patient) {
        PatientRepository.PatientStats stats = patientRepository.getPatientStats(patient.getId());
        
        String details = "Nom complet: " + patient.getFullName() + "\n" +
                "Téléphone: " + (patient.getPhone() != null ? patient.getPhone() : "N/A") + "\n" +
                "Date de naissance: " + (patient.getBirthDate() != null ? patient.getBirthDate() : "N/A") + "\n" +
                "Adresse: " + (patient.getAddress() != null ? patient.getAddress() : "N/A") + "\n" +
                "Groupe sanguin: " + (patient.getBloodGroup() != null ? patient.getBloodGroup() : "N/A") + "\n\n" +
                "=== Statistiques ===\n" +
                "Total RDV: " + stats.totalAppointments + "\n" +
                "RDV complétés: " + stats.completedAppointments + "\n" +
                "RDV annulés: " + stats.cancelledAppointments;

        new AlertDialog.Builder(this)
                .setTitle("Détails du patient")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPatientOptionsDialog(Patient patient) {
        String[] options = {"Voir l'historique", "Bloquer le compte"};
        
        new AlertDialog.Builder(this)
                .setTitle(patient.getFullName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        viewPatientHistory(patient);
                    } else {
                        confirmBlockPatient(patient);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void viewPatientHistory(Patient patient) {
        List<com.example.gestionrdv.models.Appointment> appointments = 
                appointmentRepository.getPatientAppointments(patient.getId());
        
        if (appointments.isEmpty()) {
            Toast.makeText(this, "Aucun rendez-vous trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder history = new StringBuilder();
        for (com.example.gestionrdv.models.Appointment apt : appointments) {
            history.append("📅 ")
                    .append(apt.getAppointmentDate())
                    .append(" à ")
                    .append(apt.getAppointmentTime())
                    .append("\n")
                    .append("👨‍⚕️ ")
                    .append(apt.getDoctorName())
                    .append("\n")
                    .append("📋 ")
                    .append(apt.getStatus())
                    .append("\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Historique de " + patient.getFullName())
                .setMessage(history.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmBlockPatient(Patient patient) {
        new AlertDialog.Builder(this)
                .setTitle("Bloquer le patient")
                .setMessage("Voulez-vous vraiment bloquer " + patient.getFullName() + " ?")
                .setPositiveButton("Bloquer", (dialog, which) -> {
                    // TODO: Implement block functionality
                    Toast.makeText(this, "Fonctionnalité de blocage en développement", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
