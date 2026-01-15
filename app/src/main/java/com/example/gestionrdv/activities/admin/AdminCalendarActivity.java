package com.example.gestionrdv.activities.admin;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.AppointmentCardAdapter;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.models.Appointment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * AdminCalendarActivity - View all appointments in calendar
 */
public class AdminCalendarActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CalendarView calendarView;
    private ChipGroup filterChipGroup;
    private TextView selectedDateText, appointmentsCountText, emptyStateText;
    private CardView statsCard;
    private RecyclerView appointmentsRecycler;
    
    private AppointmentRepository appointmentRepository;
    private AppointmentCardAdapter adapter;
    private String selectedDate;
    private String selectedStatus = "all"; // all, pending, confirmed, completed, cancelled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_calendar);

        appointmentRepository = new AppointmentRepository(this);
        
        // Set today's date
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(today.getTime());

        initViews();
        setupToolbar();
        setupCalendar();
        setupFilters();
        setupRecyclerView();
        loadAppointments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendarView);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        selectedDateText = findViewById(R.id.selectedDateText);
        appointmentsCountText = findViewById(R.id.appointmentsCountText);
        statsCard = findViewById(R.id.statsCard);
        appointmentsRecycler = findViewById(R.id.appointmentsRecycler);
        emptyStateText = findViewById(R.id.emptyStateText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Format selected date
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadAppointments();
        });
    }

    private void setupFilters() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedStatus = "all";
            } else {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    String text = chip.getText().toString().toLowerCase();
                    if (text.contains("attente")) {
                        selectedStatus = "pending";
                    } else if (text.contains("confirmé")) {
                        selectedStatus = "confirmed";
                    } else if (text.contains("terminé")) {
                        selectedStatus = "completed";
                    } else if (text.contains("annulé")) {
                        selectedStatus = "cancelled";
                    } else {
                        selectedStatus = "all";
                    }
                }
            }
            loadAppointments();
        });
    }

    private void setupRecyclerView() {
        adapter = new AppointmentCardAdapter(appointment -> {
            Toast.makeText(this, "RDV: " + appointment.getPatientName(), Toast.LENGTH_SHORT).show();
        });
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        appointmentsRecycler.setAdapter(adapter);
    }

    private void loadAppointments() {
        List<Appointment> appointments;
        
        if (selectedStatus.equals("all")) {
            appointments = appointmentRepository.getAppointmentsByDate(selectedDate);
        } else {
            appointments = appointmentRepository.getAppointmentsByDateAndStatus(selectedDate, selectedStatus);
        }

        adapter.setAppointments(appointments);
        updateUI(appointments);
    }

    private void updateUI(List<Appointment> appointments) {
        // Update selected date text
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE dd MMM yyyy", Locale.FRENCH);
            String formattedDate = outputFormat.format(inputFormat.parse(selectedDate));
            selectedDateText.setText(formattedDate);
        } catch (Exception e) {
            selectedDateText.setText(selectedDate);
        }

        // Update count
        int count = appointments.size();
        appointmentsCountText.setText(count + " rendez-vous");

        // Show/hide empty state
        if (count == 0) {
            appointmentsRecycler.setVisibility(android.view.View.GONE);
            emptyStateText.setVisibility(android.view.View.VISIBLE);
        } else {
            appointmentsRecycler.setVisibility(android.view.View.VISIBLE);
            emptyStateText.setVisibility(android.view.View.GONE);
        }

        // Update stats card
        updateStatsCard(appointments);
    }

    private void updateStatsCard(List<Appointment> appointments) {
        int pending = 0, confirmed = 0, completed = 0, cancelled = 0;
        
        for (Appointment apt : appointments) {
            String status = apt.getStatus();
            if ("pending".equals(status)) pending++;
            else if ("confirmed".equals(status)) confirmed++;
            else if ("completed".equals(status)) completed++;
            else if ("cancelled".equals(status)) cancelled++;
        }

        // Update chip badges (you can add TextViews in layout to show counts)
        // For now, this is a placeholder
    }
}
