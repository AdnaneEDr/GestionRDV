package com.example.gestionrdv.activities.doctor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.DoctorAppointmentAdapter;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorAppointmentsActivity extends AppCompatActivity implements DoctorAppointmentAdapter.OnDoctorAppointmentActionListener {

    private static final String TAG = "DoctorAppointments";

    // Views
    private MaterialToolbar toolbar;
    private ImageButton filterButton;
    private ChipGroup dateChipGroup;
    private Chip chipToday, chipTomorrow, chipThisWeek, chipNextWeek, chipAll;
    private RecyclerView appointmentsRecycler;
    private LinearLayout emptyStateLayout;

    // Data
    private SessionManager sessionManager;
    private AppointmentRepository appointmentRepository;
    private DoctorAppointmentAdapter adapter;
    private List<Appointment> appointments;
    private long doctorId = -1;

    // Current filter
    private String currentFilter = "today";
    private String currentStatusFilter = "all"; // all, pending, confirmed, completed, cancelled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointments);

        // Initialize
        sessionManager = new SessionManager(this);
        appointmentRepository = new AppointmentRepository(this);
        appointments = new ArrayList<>();

        // Get doctor ID from session
        doctorId = sessionManager.getProfileId();
        if (doctorId == -1) {
            Log.e(TAG, "Invalid doctor ID from session");
            Toast.makeText(this, "Erreur: Session invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupDateChips();
        setupRecyclerView();
        loadAppointments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        filterButton = findViewById(R.id.filterButton);
        dateChipGroup = findViewById(R.id.dateChipGroup);
        chipToday = findViewById(R.id.chipToday);
        chipTomorrow = findViewById(R.id.chipTomorrow);
        chipThisWeek = findViewById(R.id.chipThisWeek);
        chipNextWeek = findViewById(R.id.chipNextWeek);
        chipAll = findViewById(R.id.chipAll);
        appointmentsRecycler = findViewById(R.id.appointmentsRecycler);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());

        filterButton.setOnClickListener(v -> showStatusFilterDialog());
    }

    private void setupDateChips() {
        dateChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // If nothing selected, select "today" chip
                chipToday.setChecked(true);
                currentFilter = "today";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipToday) {
                    currentFilter = "today";
                } else if (checkedId == R.id.chipTomorrow) {
                    currentFilter = "tomorrow";
                } else if (checkedId == R.id.chipThisWeek) {
                    currentFilter = "thisWeek";
                } else if (checkedId == R.id.chipNextWeek) {
                    currentFilter = "nextWeek";
                } else if (checkedId == R.id.chipAll) {
                    currentFilter = "all";
                }
            }
            loadAppointments();
        });
    }

    private void setupRecyclerView() {
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAppointmentAdapter(appointments, this, true); // Show date
        appointmentsRecycler.setAdapter(adapter);
    }

    private void loadAppointments() {
        List<Appointment> filteredAppointments = getAppointmentsByDateFilter();

        // Apply status filter
        if (!currentStatusFilter.equals("all")) {
            List<Appointment> statusFiltered = new ArrayList<>();
            for (Appointment apt : filteredAppointments) {
                if (currentStatusFilter.equals(apt.getStatus())) {
                    statusFiltered.add(apt);
                }
            }
            filteredAppointments = statusFiltered;
        }

        appointments.clear();
        appointments.addAll(filteredAppointments);
        adapter.notifyDataSetChanged();

        updateEmptyState();

        Log.d(TAG, "Loaded " + appointments.size() + " appointments (filter: " + currentFilter + ", status: " + currentStatusFilter + ")");
    }

    private List<Appointment> getAppointmentsByDateFilter() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        switch (currentFilter) {
            case "today":
                String today = sdf.format(calendar.getTime());
                return appointmentRepository.getDoctorAppointmentsByDate(doctorId, today);

            case "tomorrow":
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                String tomorrow = sdf.format(calendar.getTime());
                return appointmentRepository.getDoctorAppointmentsByDate(doctorId, tomorrow);

            case "thisWeek":
                return getDoctorAppointmentsForWeek(doctorId, 0);

            case "nextWeek":
                return getDoctorAppointmentsForWeek(doctorId, 1);

            case "all":
            default:
                return appointmentRepository.getDoctorAppointments(doctorId);
        }
    }

    private List<Appointment> getDoctorAppointmentsForWeek(long doctorId, int weekOffset) {
        // Get all doctor appointments and filter by week
        List<Appointment> allAppointments = appointmentRepository.getDoctorAppointments(doctorId);
        List<Appointment> weekAppointments = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Calculate week start and end
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        String weekStart = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 7);
        String weekEnd = sdf.format(calendar.getTime());

        for (Appointment apt : allAppointments) {
            String date = apt.getAppointmentDate();
            if (date != null && date.compareTo(weekStart) >= 0 && date.compareTo(weekEnd) < 0) {
                weekAppointments.add(apt);
            }
        }

        return weekAppointments;
    }

    private void updateEmptyState() {
        if (appointments.isEmpty()) {
            appointmentsRecycler.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            appointmentsRecycler.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showStatusFilterDialog() {
        String[] options = {"Tous", "En attente", "Confirmés", "Terminés", "Annulés"};
        String[] statusValues = {"all", "pending", "confirmed", "completed", "cancelled"};

        int currentIndex = 0;
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equals(currentStatusFilter)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Filtrer par statut")
                .setSingleChoiceItems(options, currentIndex, (dialog, which) -> {
                    currentStatusFilter = statusValues[which];
                    dialog.dismiss();
                    loadAppointments();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // DoctorAppointmentAdapter callbacks
    @Override
    public void onAppointmentClick(Appointment appointment) {
        showAppointmentDetailsDialog(appointment);
    }

    @Override
    public void onConfirmClick(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmer le rendez-vous")
                .setMessage("Confirmer le rendez-vous avec " + appointment.getPatientName() + " ?")
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), "confirmed");
                    if (success) {
                        Toast.makeText(this, "Rendez-vous confirmé", Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    } else {
                        Toast.makeText(this, "Erreur lors de la confirmation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onCompleteClick(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Terminer le rendez-vous")
                .setMessage("Marquer le rendez-vous avec " + appointment.getPatientName() + " comme terminé ?")
                .setPositiveButton("Terminer", (dialog, which) -> {
                    boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), "completed");
                    if (success) {
                        Toast.makeText(this, "Rendez-vous terminé", Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    } else {
                        Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onCancelClick(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Annuler le rendez-vous")
                .setMessage("Voulez-vous vraiment annuler ce rendez-vous ?")
                .setPositiveButton("Oui, annuler", (dialog, which) -> {
                    boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), "cancelled");
                    if (success) {
                        Toast.makeText(this, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'annulation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void showAppointmentDetailsDialog(Appointment appointment) {
        String details = String.format(
                "Patient: %s\n" +
                "Date: %s\n" +
                "Heure: %s - %s\n" +
                "Motif: %s\n" +
                "Statut: %s",
                appointment.getPatientName() != null ? appointment.getPatientName() : "N/A",
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getEndTime() != null ? appointment.getEndTime() : "",
                appointment.getReason() != null ? appointment.getReason() : "N/A",
                getStatusLabel(appointment.getStatus())
        );

        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            details += "\n\nNotes: " + appointment.getNotes();
        }

        new AlertDialog.Builder(this)
                .setTitle("Détails du rendez-vous")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Inconnu";
        switch (status) {
            case "pending": return "En attente";
            case "confirmed": return "Confirmé";
            case "completed": return "Terminé";
            case "cancelled": return "Annulé";
            default: return status;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }
}
