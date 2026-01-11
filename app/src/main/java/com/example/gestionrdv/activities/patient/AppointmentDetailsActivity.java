package com.example.gestionrdv.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
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
import com.example.gestionrdv.models.Appointment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class AppointmentDetailsActivity extends AppCompatActivity {

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

    private Appointment appointment;
    private long appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

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
        // In a real app, load from database using appointmentId
        // For now, use sample data
        appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctorName("Dr. Fatima Zahra");
        appointment.setDoctorSpecialization("Médecine Générale");
        appointment.setDoctorRating(4.8);
        appointment.setDoctorLocation("Cabinet Médical, Tanger");
        appointment.setAppointmentDate("Mercredi, 25 Décembre 2024");
        appointment.setAppointmentTime("10:30");
        appointment.setEndTime("11:00");
        appointment.setReason("Consultation générale");
        appointment.setStatus("confirmed");
        appointment.setNotes("");

        // Display data
        doctorName.setText(appointment.getDoctorName());
        doctorSpecialty.setText(appointment.getDoctorSpecialization());
        doctorRating.setText(String.valueOf(appointment.getDoctorRating()));
        appointmentDate.setText(appointment.getAppointmentDate());
        appointmentTime.setText(appointment.getTimeRange());
        appointmentReason.setText(appointment.getReason());
        appointmentLocation.setText(appointment.getDoctorLocation());

        // Set status
        updateStatusBanner(appointment.getStatus());

        // Show/hide notes
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            notesCard.setVisibility(View.VISIBLE);
            appointmentNotes.setText(appointment.getNotes());
        } else {
            notesCard.setVisibility(View.GONE);
        }

        // Show/hide action buttons based on status
        if (appointment.getStatus().equals("cancelled") || appointment.getStatus().equals("completed")) {
            actionButtonsLayout.setVisibility(View.GONE);
        }
    }

    private void updateStatusBanner(String status) {
        int backgroundColor;
        String text;

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
            intent.putExtra("doctor_name", appointment.getDoctorName());
            intent.putExtra("doctor_specialty", appointment.getDoctorSpecialization());
            intent.putExtra("reason", appointment.getReason());
            startActivity(intent);
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> showCancelConfirmationDialog());
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, "RDV - " + appointment.getDoctorName());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Motif: " + appointment.getReason());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.getDoctorLocation());

        // Set appointment time (simplified - in real app, parse the actual date)
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2024, Calendar.DECEMBER, 25, 10, 30);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());

        Calendar endTime = Calendar.getInstance();
        endTime.set(2024, Calendar.DECEMBER, 25, 11, 0);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());

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
        // In a real app, update the appointment status in database
        appointment.setStatus("cancelled");
        updateStatusBanner("cancelled");
        actionButtonsLayout.setVisibility(View.GONE);
        Toast.makeText(this, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
    }
}
