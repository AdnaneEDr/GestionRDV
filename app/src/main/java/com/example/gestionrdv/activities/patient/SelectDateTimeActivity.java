package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.TimeSlotAdapter;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.models.Appointment;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SelectDateTimeActivity extends AppCompatActivity {

    private static final String TAG = "SelectDateTimeActivity";

    private MaterialToolbar toolbar;
    private CalendarView calendarView;
    private TextView selectedDateText;
    private RecyclerView morningTimeSlotsRecycler, afternoonTimeSlotsRecycler;
    private MaterialButton confirmButton;

    private TimeSlotAdapter morningAdapter, afternoonAdapter;

    // Repository and Session
    private AppointmentRepository appointmentRepository;
    private SessionManager sessionManager;

    // Intent data
    private long doctorId;
    private String doctorName, doctorSpecialty, reason, notes;
    private boolean isModification = false;
    private long appointmentId = -1; // For modification mode

    // Selected data
    private String selectedDate = "";        // Display format: "EEEE d MMMM yyyy"
    private String selectedDateDb = "";      // Database format: "yyyy-MM-dd"
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_datetime);

        // Initialize repository and session manager
        appointmentRepository = new AppointmentRepository(this);
        sessionManager = new SessionManager(this);

        getIntentData();
        initViews();
        setupCalendar();
        setupTimeSlots();
        setupClickListeners();
    }

    private void getIntentData() {
        doctorId = getIntent().getLongExtra("doctor_id", -1);
        doctorName = getIntent().getStringExtra("doctor_name");
        doctorSpecialty = getIntent().getStringExtra("doctor_specialty");
        reason = getIntent().getStringExtra("reason");
        notes = getIntent().getStringExtra("notes");
        isModification = getIntent().getBooleanExtra("is_modification", false);
        appointmentId = getIntent().getLongExtra("appointment_id", -1);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        morningTimeSlotsRecycler = findViewById(R.id.morningTimeSlotsRecycler);
        afternoonTimeSlotsRecycler = findViewById(R.id.afternoonTimeSlotsRecycler);
        confirmButton = findViewById(R.id.confirmButton);
    }

    private void setupCalendar() {
        // Set minimum date to today
        calendarView.setMinDate(System.currentTimeMillis() - 1000);

        // Date formats
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH);
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Set initial selected date
        Date today = new Date();
        selectedDate = displayFormat.format(today);
        selectedDateDb = dbFormat.format(today);
        selectedDateText.setText(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date selectedDateObj = calendar.getTime();

            selectedDate = displayFormat.format(selectedDateObj);
            selectedDateDb = dbFormat.format(selectedDateObj);
            selectedDateText.setText(selectedDate);

            // Reset time selection when date changes
            selectedTime = "";
            if (morningAdapter != null) morningAdapter.setSelectedPosition(-1);
            if (afternoonAdapter != null) afternoonAdapter.setSelectedPosition(-1);
            confirmButton.setEnabled(false);

            // Refresh available time slots for the new date
            refreshTimeSlots();
        });
    }

    private void setupTimeSlots() {
        // Morning time slots
        List<String> morningSlots = Arrays.asList("08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30");

        // Afternoon time slots
        List<String> afternoonSlots = Arrays.asList("14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30");

        // Setup morning recycler
        morningTimeSlotsRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        morningAdapter = new TimeSlotAdapter(morningSlots, (time, position) -> {
            selectedTime = time;
            if (afternoonAdapter != null) afternoonAdapter.setSelectedPosition(-1);
            updateConfirmButton();
        });
        morningTimeSlotsRecycler.setAdapter(morningAdapter);

        // Setup afternoon recycler
        afternoonTimeSlotsRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        afternoonAdapter = new TimeSlotAdapter(afternoonSlots, (time, position) -> {
            selectedTime = time;
            if (morningAdapter != null) morningAdapter.setSelectedPosition(-1);
            updateConfirmButton();
        });
        afternoonTimeSlotsRecycler.setAdapter(afternoonAdapter);
    }

    private void refreshTimeSlots() {
        // In a real app, this would fetch available slots from the server
        // For now, we just reset the adapters
        setupTimeSlots();
    }

    private void updateConfirmButton() {
        confirmButton.setEnabled(!selectedDate.isEmpty() && !selectedTime.isEmpty());
    }

    private void setupClickListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Confirm button
        confirmButton.setOnClickListener(v -> {
            if (validateSelection()) {
                showConfirmationDialog();
            }
        });
    }

    private boolean validateSelection() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner un créneau horaire", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showConfirmationDialog() {
        String message = String.format(
                "Médecin: %s\nDate: %s\nHeure: %s\nMotif: %s",
                doctorName != null ? doctorName : "Non spécifié",
                selectedDate,
                selectedTime,
                reason != null ? reason : "Consultation générale"
        );

        new AlertDialog.Builder(this)
                .setTitle("Confirmer le rendez-vous")
                .setMessage(message)
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    confirmAppointment();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void confirmAppointment() {
        // Get patient ID from session
        long patientId = sessionManager.getProfileId();

        if (patientId == -1) {
            Toast.makeText(this, "Erreur: Session invalide. Veuillez vous reconnecter.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid patient ID from session");
            return;
        }

        if (doctorId == -1) {
            Toast.makeText(this, "Erreur: Médecin non sélectionné.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid doctor ID");
            return;
        }

        // Calculate end time (30 minutes after start time)
        String endTime = calculateEndTime(selectedTime);

        // Create appointment object
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentDate(selectedDateDb);
        appointment.setAppointmentTime(selectedTime);
        appointment.setEndTime(endTime);
        appointment.setReason(reason != null ? reason : "Consultation générale");
        appointment.setNotes(notes);
        appointment.setStatus("pending");

        boolean success;
        if (isModification && appointmentId != -1) {
            // Update existing appointment
            appointment.setId(appointmentId);
            success = appointmentRepository.updateAppointment(appointment);
            if (success) {
                Log.d(TAG, "Appointment updated successfully: ID=" + appointmentId);
                Toast.makeText(this, "Rendez-vous modifié avec succès !", Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "Failed to update appointment: ID=" + appointmentId);
                Toast.makeText(this, "Erreur lors de la modification du rendez-vous.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            // Create new appointment
            long newAppointmentId = appointmentRepository.createAppointment(appointment);
            success = newAppointmentId != -1;
            if (success) {
                Log.d(TAG, "Appointment created successfully: ID=" + newAppointmentId);
                Toast.makeText(this, "Rendez-vous confirmé avec succès !", Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "Failed to create appointment");
                Toast.makeText(this, "Erreur lors de la création du rendez-vous.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Navigate back to dashboard on success
        Intent intent = new Intent(this, PatientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Calculate end time by adding 30 minutes to start time
     * @param startTime Time in HH:mm format
     * @return End time in HH:mm format
     */
    private String calculateEndTime(String startTime) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date start = timeFormat.parse(startTime);
            if (start != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start);
                calendar.add(Calendar.MINUTE, 30);
                return timeFormat.format(calendar.getTime());
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing start time: " + startTime, e);
        }
        // Fallback: return start time + ":30" logic or default
        return startTime;
    }
}
