package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.gestionrdv.R;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.models.Appointment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppointmentDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AppointmentDetails";

    private MaterialToolbar toolbar;
    private CardView statusBanner;
    private TextView statusText;
    private ImageView doctorAvatar;
    private TextView doctorName, doctorSpecialty, doctorRating;
    private TextView appointmentDate, appointmentTime, appointmentReason, appointmentLocation;
    private CardView notesCard;
    private TextView appointmentNotes;
    private LinearLayout actionButtonsLayout;
    private MaterialButton addToCalendarButton, modifyButton, cancelButton;

    private AppointmentRepository appointmentRepository;
    private Appointment appointment;
    private long appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        // Initialize repository
        appointmentRepository = new AppointmentRepository(this);

        getIntentData();
        initViews();
        loadAppointmentData();
        setupClickListeners();
    }

    private void getIntentData() {
        appointmentId = getIntent().getLongExtra("appointment_id", -1);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        statusBanner = findViewById(R.id.statusBanner);
        statusText = findViewById(R.id.statusText);
        doctorAvatar = findViewById(R.id.doctorAvatar);
        doctorName = findViewById(R.id.doctorName);
        doctorSpecialty = findViewById(R.id.doctorSpecialty);
        doctorRating = findViewById(R.id.doctorRating);
        appointmentDate = findViewById(R.id.appointmentDate);
        appointmentTime = findViewById(R.id.appointmentTime);
        appointmentReason = findViewById(R.id.appointmentReason);
        appointmentLocation = findViewById(R.id.appointmentLocation);
        notesCard = findViewById(R.id.notesCard);
        appointmentNotes = findViewById(R.id.appointmentNotes);
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout);
        addToCalendarButton = findViewById(R.id.addToCalendarButton);
        modifyButton = findViewById(R.id.modifyButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void loadAppointmentData() {
        if (appointmentId == -1) {
            Log.e(TAG, "Invalid appointment ID");
            Toast.makeText(this, "Erreur: Rendez-vous introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load appointment from database
        appointment = appointmentRepository.getAppointmentById(appointmentId);

        if (appointment == null) {
            Log.e(TAG, "Appointment not found: ID=" + appointmentId);
            Toast.makeText(this, "Erreur: Rendez-vous introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loaded appointment: ID=" + appointment.getId() + ", Doctor=" + appointment.getDoctorName());

        // Display doctor info
        String doctorFullName = appointment.getDoctorName();
        if (doctorFullName != null && !doctorFullName.isEmpty()) {
            doctorName.setText(doctorFullName);
        } else {
            doctorName.setText("Médecin");
        }

        String specialty = appointment.getDoctorSpecialization();
        if (specialty != null && !specialty.isEmpty()) {
            doctorSpecialty.setText(specialty);
        } else {
            doctorSpecialty.setText("Spécialité non spécifiée");
        }

        double rating = appointment.getDoctorRating();
        if (rating > 0) {
            doctorRating.setText(String.format(Locale.getDefault(), "%.1f", rating));
        } else {
            doctorRating.setText("N/A");
        }

        // Display appointment details
        String dateDisplay = formatDateForDisplay(appointment.getAppointmentDate());
        appointmentDate.setText(dateDisplay);

        String timeRange = appointment.getTimeRange();
        if (timeRange != null && !timeRange.isEmpty()) {
            appointmentTime.setText(timeRange);
        } else {
            appointmentTime.setText(appointment.getAppointmentTime());
        }

        String reason = appointment.getReason();
        if (reason != null && !reason.isEmpty()) {
            appointmentReason.setText(reason);
        } else {
            appointmentReason.setText("Consultation");
        }

        String location = appointment.getDoctorLocation();
        if (location != null && !location.isEmpty()) {
            appointmentLocation.setText(location);
        } else {
            appointmentLocation.setText("Adresse non spécifiée");
        }

        // Set status
        updateStatusBanner(appointment.getStatus());

        // Show/hide notes
        String notes = appointment.getNotes();
        if (notes != null && !notes.isEmpty()) {
            notesCard.setVisibility(View.VISIBLE);
            appointmentNotes.setText(notes);
        } else {
            notesCard.setVisibility(View.GONE);
        }

        // Show/hide action buttons based on status
        String status = appointment.getStatus();
        if (status != null && (status.equals("cancelled") || status.equals("completed"))) {
            actionButtonsLayout.setVisibility(View.GONE);
        } else {
            actionButtonsLayout.setVisibility(View.VISIBLE);
        }
    }

    private String formatDateForDisplay(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) {
            return "Date non définie";
        }

        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH);
            Date date = dbFormat.parse(dbDate);
            if (date != null) {
                String formatted = displayFormat.format(date);
                // Capitalize first letter
                return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dbDate, e);
        }

        return dbDate;
    }

    private void updateStatusBanner(String status) {
        int backgroundColor;
        String text;

        if (status == null) status = "pending";

        switch (status) {
            case "confirmed":
                backgroundColor = R.color.success;
                text = "Rendez-vous confirmé";
                break;
            case "pending":
                backgroundColor = R.color.warning;
                text = "En attente de confirmation";
                break;
            case "cancelled":
                backgroundColor = R.color.error;
                text = "Rendez-vous annulé";
                break;
            case "completed":
                backgroundColor = R.color.info;
                text = "Rendez-vous terminé";
                break;
            default:
                backgroundColor = R.color.text_hint;
                text = status;
        }

        statusBanner.setCardBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        statusText.setText(text);
    }

    private void setupClickListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Add to calendar button
        addToCalendarButton.setOnClickListener(v -> addToCalendar());

        // Modify button
        modifyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectDateTimeActivity.class);
            intent.putExtra("appointment_id", appointment.getId());
            intent.putExtra("is_modification", true);
            intent.putExtra("doctor_id", appointment.getDoctorId());
            intent.putExtra("doctor_name", appointment.getDoctorName());
            intent.putExtra("doctor_specialty", appointment.getDoctorSpecialization());
            intent.putExtra("reason", appointment.getReason());
            intent.putExtra("notes", appointment.getNotes());
            startActivity(intent);
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> showCancelConfirmationDialog());
    }

    private void addToCalendar() {
        if (appointment == null) {
            Toast.makeText(this, "Erreur: Données du rendez-vous non disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, "RDV - " + appointment.getDoctorName());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Motif: " + appointment.getReason());

        String location = appointment.getDoctorLocation();
        if (location != null && !location.isEmpty()) {
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        }

        // Parse the appointment date and time
        try {
            String dateStr = appointment.getAppointmentDate(); // Format: yyyy-MM-dd
            String startTimeStr = appointment.getAppointmentTime(); // Format: HH:mm
            String endTimeStr = appointment.getEndTime(); // Format: HH:mm

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date date = dateFormat.parse(dateStr);
            Date startTime = timeFormat.parse(startTimeStr);

            if (date != null && startTime != null) {
                Calendar beginCal = Calendar.getInstance();
                Calendar dateCal = Calendar.getInstance();
                dateCal.setTime(date);

                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(startTime);

                beginCal.set(
                        dateCal.get(Calendar.YEAR),
                        dateCal.get(Calendar.MONTH),
                        dateCal.get(Calendar.DAY_OF_MONTH),
                        timeCal.get(Calendar.HOUR_OF_DAY),
                        timeCal.get(Calendar.MINUTE)
                );

                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginCal.getTimeInMillis());

                // Calculate end time
                Calendar endCal = Calendar.getInstance();
                endCal.setTimeInMillis(beginCal.getTimeInMillis());

                if (endTimeStr != null && !endTimeStr.isEmpty()) {
                    Date endTime = timeFormat.parse(endTimeStr);
                    if (endTime != null) {
                        Calendar endTimeCal = Calendar.getInstance();
                        endTimeCal.setTime(endTime);
                        endCal.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY));
                        endCal.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE));
                    }
                } else {
                    // Default: 30 minutes duration
                    endCal.add(Calendar.MINUTE, 30);
                }

                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis());
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing appointment date/time for calendar", e);
            // Fallback: just open calendar without specific time
        }

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Aucune application de calendrier trouvée", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Annuler le rendez-vous")
                .setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous ?")
                .setPositiveButton("Oui, annuler", (dialog, which) -> {
                    cancelAppointment();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void cancelAppointment() {
        if (appointment == null) {
            Toast.makeText(this, "Erreur: Données du rendez-vous non disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update appointment status in database
        boolean success = appointmentRepository.updateAppointmentStatus(appointment.getId(), "cancelled");

        if (success) {
            Log.d(TAG, "Appointment cancelled successfully: ID=" + appointment.getId());
            appointment.setStatus("cancelled");
            updateStatusBanner("cancelled");
            actionButtonsLayout.setVisibility(View.GONE);
            Toast.makeText(this, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Failed to cancel appointment: ID=" + appointment.getId());
            Toast.makeText(this, "Erreur lors de l'annulation", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data in case it was modified
        if (appointmentId != -1) {
            loadAppointmentData();
        }
    }
}
