package com.example.gestionrdv.activities.admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.AdminAppointmentAdapter;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.models.Doctor;
import com.example.gestionrdv.models.Patient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminAppointmentsActivity extends AppCompatActivity implements AdminAppointmentAdapter.OnAppointmentActionListener {

    private RecyclerView appointmentsRecycler;
    private TextView noAppointmentsText, totalAppointmentsText;
    private ChipGroup statusFilterChipGroup;
    private FloatingActionButton fabAddAppointment;
    private EditText searchEditText;

    private AdminAppointmentAdapter adapter;
    private AppointmentRepository appointmentRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;

    private List<Appointment> allAppointments;
    private String currentStatusFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointments);

        // Initialize repositories
        appointmentRepository = new AppointmentRepository(this);
        doctorRepository = new DoctorRepository(this);
        patientRepository = new PatientRepository(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        loadAppointments();
    }

    private void initViews() {
        appointmentsRecycler = findViewById(R.id.appointmentsRecycler);
        noAppointmentsText = findViewById(R.id.noAppointmentsText);
        totalAppointmentsText = findViewById(R.id.totalAppointmentsText);
        statusFilterChipGroup = findViewById(R.id.statusFilterChipGroup);
        fabAddAppointment = findViewById(R.id.fabAddAppointment);
        searchEditText = findViewById(R.id.searchEditText);

        fabAddAppointment.setOnClickListener(v -> showCreateAppointmentDialog());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Gestion des rendez-vous");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAppointmentAdapter(new ArrayList<>(), this);
        appointmentsRecycler.setAdapter(adapter);
    }

    private void setupFilters() {
        statusFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentStatusFilter = "all";
            } else if (checkedId == R.id.chipPending) {
                currentStatusFilter = "pending";
            } else if (checkedId == R.id.chipConfirmed) {
                currentStatusFilter = "confirmed";
            } else if (checkedId == R.id.chipCancelled) {
                currentStatusFilter = "cancelled";
            } else if (checkedId == R.id.chipCompleted) {
                currentStatusFilter = "completed";
            }
            filterAppointments();
        });

        // Search functionality
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAppointments();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void loadAppointments() {
        allAppointments = appointmentRepository.getAllAppointments();
        filterAppointments();
    }

    private void filterAppointments() {
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
        List<Appointment> filteredList = new ArrayList<>();

        for (Appointment apt : allAppointments) {
            // Status filter
            boolean matchesStatus = currentStatusFilter.equals("all") ||
                    apt.getStatus().equals(currentStatusFilter);

            // Search filter
            boolean matchesSearch = searchQuery.isEmpty() ||
                    (apt.getPatientName() != null && apt.getPatientName().toLowerCase().contains(searchQuery)) ||
                    (apt.getDoctorName() != null && apt.getDoctorName().toLowerCase().contains(searchQuery));

            if (matchesStatus && matchesSearch) {
                filteredList.add(apt);
            }
        }

        // Update UI
        adapter.updateAppointments(filteredList);
        totalAppointmentsText.setText(filteredList.size() + " rendez-vous");

        if (filteredList.isEmpty()) {
            appointmentsRecycler.setVisibility(View.GONE);
            noAppointmentsText.setVisibility(View.VISIBLE);
        } else {
            appointmentsRecycler.setVisibility(View.VISIBLE);
            noAppointmentsText.setVisibility(View.GONE);
        }
    }

    private void showCreateAppointmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_appointment, null);
        builder.setView(dialogView);

        // Get views
        Spinner patientSpinner = dialogView.findViewById(R.id.patientSpinner);
        Spinner doctorSpinner = dialogView.findViewById(R.id.doctorSpinner);
        EditText dateEditText = dialogView.findViewById(R.id.dateEditText);
        EditText timeEditText = dialogView.findViewById(R.id.timeEditText);
        EditText reasonEditText = dialogView.findViewById(R.id.reasonEditText);
        EditText notesEditText = dialogView.findViewById(R.id.notesEditText);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        // Load patients and doctors
        List<Patient> patients = patientRepository.getAllPatients();
        List<Doctor> doctors = doctorRepository.getAllDoctors();

        // Setup patient spinner
        List<String> patientNames = new ArrayList<>();
        for (Patient p : patients) {
            patientNames.add(p.getFirstName() + " " + p.getLastName());
        }
        ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, patientNames);
        patientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        patientSpinner.setAdapter(patientAdapter);

        // Setup doctor spinner
        List<String> doctorNames = new ArrayList<>();
        for (Doctor d : doctors) {
            doctorNames.add("Dr. " + d.getFirstName() + " " + d.getLastName() + " - " + d.getSpecialization());
        }
        ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, doctorNames);
        doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        doctorSpinner.setAdapter(doctorAdapter);

        // Date picker
        dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                dateEditText.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time picker
        timeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                timeEditText.setText(time);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            // Validation
            if (patientSpinner.getSelectedItemPosition() == -1) {
                Toast.makeText(this, "Sélectionnez un patient", Toast.LENGTH_SHORT).show();
                return;
            }
            if (doctorSpinner.getSelectedItemPosition() == -1) {
                Toast.makeText(this, "Sélectionnez un médecin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dateEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Sélectionnez une date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (timeEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Sélectionnez une heure", Toast.LENGTH_SHORT).show();
                return;
            }
            if (reasonEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Entrez le motif", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create appointment
            Patient selectedPatient = patients.get(patientSpinner.getSelectedItemPosition());
            Doctor selectedDoctor = doctors.get(doctorSpinner.getSelectedItemPosition());

            Appointment appointment = new Appointment();
            appointment.setPatientId(selectedPatient.getId());
            appointment.setDoctorId(selectedDoctor.getId());
            appointment.setAppointmentDate(dateEditText.getText().toString());
            appointment.setAppointmentTime(timeEditText.getText().toString());
            appointment.setReason(reasonEditText.getText().toString().trim());
            appointment.setNotes(notesEditText.getText().toString().trim());
            appointment.setStatus("confirmed");

            // Calculate end time (30 minutes after start)
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(appointment.getAppointmentTime()));
                cal.add(Calendar.MINUTE, 30);
                appointment.setEndTime(sdf.format(cal.getTime()));
            } catch (Exception e) {
                appointment.setEndTime(appointment.getAppointmentTime());
            }

            long result = appointmentRepository.createAppointment(appointment);

            if (result > 0) {
                Toast.makeText(this, "Rendez-vous créé avec succès", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadAppointments();
            } else {
                Toast.makeText(this, "Erreur lors de la création", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onEditAppointment(Appointment appointment) {
        showEditAppointmentDialog(appointment);
    }

    private void showEditAppointmentDialog(Appointment appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_appointment, null);
        builder.setView(dialogView);

        EditText dateEditText = dialogView.findViewById(R.id.dateEditText);
        EditText timeEditText = dialogView.findViewById(R.id.timeEditText);
        EditText reasonEditText = dialogView.findViewById(R.id.reasonEditText);
        EditText notesEditText = dialogView.findViewById(R.id.notesEditText);
        Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Fill current values
        dateEditText.setText(appointment.getAppointmentDate());
        timeEditText.setText(appointment.getAppointmentTime());
        reasonEditText.setText(appointment.getReason());
        notesEditText.setText(appointment.getNotes());

        // Setup status spinner
        String[] statuses = {"pending", "confirmed", "cancelled", "completed"};
        String[] statusLabels = {"En attente", "Confirmé", "Annulé", "Terminé"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusLabels);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Set current status
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(appointment.getStatus())) {
                statusSpinner.setSelection(i);
                break;
            }
        }

        // Date picker
        dateEditText.setOnClickListener(v -> {
            String[] dateParts = appointment.getAppointmentDate().split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int day = Integer.parseInt(dateParts[2]);

            new DatePickerDialog(this, (view, y, m, d) -> {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                dateEditText.setText(date);
            }, year, month, day).show();
        });

        // Time picker
        timeEditText.setOnClickListener(v -> {
            String[] timeParts = appointment.getAppointmentTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            new TimePickerDialog(this, (view, h, m) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                timeEditText.setText(time);
            }, hour, minute, true).show();
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            appointment.setAppointmentDate(dateEditText.getText().toString());
            appointment.setAppointmentTime(timeEditText.getText().toString());
            appointment.setReason(reasonEditText.getText().toString().trim());
            appointment.setNotes(notesEditText.getText().toString().trim());
            appointment.setStatus(statuses[statusSpinner.getSelectedItemPosition()]);

            // Calculate end time
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(appointment.getAppointmentTime()));
                cal.add(Calendar.MINUTE, 30);
                appointment.setEndTime(sdf.format(cal.getTime()));
            } catch (Exception e) {
                appointment.setEndTime(appointment.getAppointmentTime());
            }

            boolean success = appointmentRepository.updateAppointment(appointment);

            if (success) {
                Toast.makeText(this, "Rendez-vous modifié avec succès", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadAppointments();
            } else {
                Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onChangeStatus(Appointment appointment, String newStatus) {
        new AlertDialog.Builder(this)
                .setTitle("Changer le statut")
                .setMessage("Voulez-vous vraiment changer le statut à \"" + getStatusLabel(newStatus) + "\" ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), newStatus);
                    if (success) {
                        Toast.makeText(this, "Statut modifié", Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    } else {
                        Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    public void onDeleteAppointment(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le rendez-vous")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce rendez-vous ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    boolean success = appointmentRepository.deleteAppointment(appointment.getId());
                    if (success) {
                        Toast.makeText(this, "Rendez-vous supprimé", Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "pending": return "En attente";
            case "confirmed": return "Confirmé";
            case "cancelled": return "Annulé";
            case "completed": return "Terminé";
            default: return status;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }
}