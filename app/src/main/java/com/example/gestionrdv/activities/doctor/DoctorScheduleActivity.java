package com.example.gestionrdv.activities.doctor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DoctorScheduleActivity extends AppCompatActivity {

    private static final String TAG = "DoctorSchedule";

    // Views
    private MaterialToolbar toolbar;
    private MaterialButton editScheduleButton;
    private ChipGroup durationChipGroup;
    private Chip chip15min, chip20min, chip30min;
    private MaterialButton addAbsenceButton;
    private RecyclerView absencesRecycler;
    private LinearLayout emptyAbsencesLayout;
    private MaterialButton saveButton;

    // Data
    private SessionManager sessionManager;
    private long doctorId = -1;
    private int selectedDuration = 20;
    private List<Absence> absences;
    private AbsenceAdapter absenceAdapter;

    // Schedule data
    private String[] daySchedules = {
            "09:00 - 12:00 | 14:00 - 18:00",
            "09:00 - 12:00 | 14:00 - 18:00",
            "09:00 - 12:00",
            "09:00 - 12:00 | 14:00 - 18:00",
            "09:00 - 12:00 | 14:00 - 17:00",
            "Fermé",
            "Fermé"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        sessionManager = new SessionManager(this);
        doctorId = sessionManager.getProfileId();
        absences = new ArrayList<>();

        initViews();
        setupToolbar();
        setupDurationChips();
        setupAbsencesList();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        editScheduleButton = findViewById(R.id.editScheduleButton);
        durationChipGroup = findViewById(R.id.durationChipGroup);
        chip15min = findViewById(R.id.chip15min);
        chip20min = findViewById(R.id.chip20min);
        chip30min = findViewById(R.id.chip30min);
        addAbsenceButton = findViewById(R.id.addAbsenceButton);
        absencesRecycler = findViewById(R.id.absencesRecycler);
        emptyAbsencesLayout = findViewById(R.id.emptyAbsencesLayout);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDurationChips() {
        chip20min.setChecked(true);

        durationChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chip20min.setChecked(true);
                selectedDuration = 20;
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip15min) {
                    selectedDuration = 15;
                } else if (checkedId == R.id.chip20min) {
                    selectedDuration = 20;
                } else if (checkedId == R.id.chip30min) {
                    selectedDuration = 30;
                }
            }
        });
    }

    private void setupAbsencesList() {
        absencesRecycler.setLayoutManager(new LinearLayoutManager(this));
        absenceAdapter = new AbsenceAdapter(absences);
        absencesRecycler.setAdapter(absenceAdapter);
        updateAbsencesEmptyState();
    }

    private void setupClickListeners() {
        editScheduleButton.setOnClickListener(v -> showEditScheduleDialog());
        addAbsenceButton.setOnClickListener(v -> showAddAbsenceDialog());
        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void showEditScheduleDialog() {
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

        new AlertDialog.Builder(this)
                .setTitle("Modifier le planning")
                .setItems(days, (dialog, which) -> showEditDayScheduleDialog(days[which], which))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEditDayScheduleDialog(String day, int dayIndex) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView currentLabel = new TextView(this);
        currentLabel.setText("Horaire actuel: " + daySchedules[dayIndex]);
        currentLabel.setPadding(0, 0, 0, 20);
        layout.addView(currentLabel);

        EditText morningInput = new EditText(this);
        morningInput.setHint("Matin (ex: 09:00 - 12:00)");
        if (!daySchedules[dayIndex].equals("Fermé")) {
            String[] parts = daySchedules[dayIndex].split(" \\| ");
            if (parts.length > 0) {
                morningInput.setText(parts[0]);
            }
        }
        layout.addView(morningInput);

        EditText afternoonInput = new EditText(this);
        afternoonInput.setHint("Après-midi (ex: 14:00 - 18:00)");
        if (!daySchedules[dayIndex].equals("Fermé")) {
            String[] parts = daySchedules[dayIndex].split(" \\| ");
            if (parts.length > 1) {
                afternoonInput.setText(parts[1]);
            }
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 20;
        afternoonInput.setLayoutParams(params);
        layout.addView(afternoonInput);

        new AlertDialog.Builder(this)
                .setTitle("Horaire de " + day)
                .setView(layout)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String morning = morningInput.getText().toString().trim();
                    String afternoon = afternoonInput.getText().toString().trim();

                    if (morning.isEmpty() && afternoon.isEmpty()) {
                        daySchedules[dayIndex] = "Fermé";
                    } else if (afternoon.isEmpty()) {
                        daySchedules[dayIndex] = morning;
                    } else if (morning.isEmpty()) {
                        daySchedules[dayIndex] = afternoon;
                    } else {
                        daySchedules[dayIndex] = morning + " | " + afternoon;
                    }

                    Toast.makeText(this, "Horaire de " + day + " modifié", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Jour fermé", (dialog, which) -> {
                    daySchedules[dayIndex] = "Fermé";
                    Toast.makeText(this, day + " marqué comme fermé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAddAbsenceDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText startDateInput = new EditText(this);
        startDateInput.setHint("Date de début (cliquez pour choisir)");
        startDateInput.setFocusable(false);
        startDateInput.setClickable(true);
        layout.addView(startDateInput);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 20;

        EditText endDateInput = new EditText(this);
        endDateInput.setHint("Date de fin (cliquez pour choisir)");
        endDateInput.setFocusable(false);
        endDateInput.setClickable(true);
        endDateInput.setLayoutParams(params);
        layout.addView(endDateInput);

        EditText reasonInput = new EditText(this);
        reasonInput.setHint("Motif (ex: Congés, Formation...)");
        reasonInput.setLayoutParams(params);
        layout.addView(reasonInput);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        startDateInput.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                startDateInput.setText(sdf.format(cal.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        endDateInput.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                endDateInput.setText(sdf.format(cal.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Ajouter une absence")
                .setView(layout)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String startDate = startDateInput.getText().toString().trim();
                    String endDate = endDateInput.getText().toString().trim();
                    String reason = reasonInput.getText().toString().trim();

                    if (startDate.isEmpty() || endDate.isEmpty()) {
                        Toast.makeText(this, "Veuillez sélectionner les dates", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (reason.isEmpty()) {
                        reason = "Absence";
                    }

                    Absence absence = new Absence(startDate, endDate, reason);
                    absences.add(absence);
                    absenceAdapter.notifyDataSetChanged();
                    updateAbsencesEmptyState();

                    Toast.makeText(this, "Absence ajoutée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void updateAbsencesEmptyState() {
        if (absences.isEmpty()) {
            absencesRecycler.setVisibility(View.GONE);
            emptyAbsencesLayout.setVisibility(View.VISIBLE);
        } else {
            absencesRecycler.setVisibility(View.VISIBLE);
            emptyAbsencesLayout.setVisibility(View.GONE);
        }
    }

    private void saveSettings() {
        StringBuilder summary = new StringBuilder();
        summary.append("Paramètres enregistrés:\n\n");
        summary.append("Durée des consultations: ").append(selectedDuration).append(" minutes\n\n");
        summary.append("Absences planifiées: ").append(absences.size());

        new AlertDialog.Builder(this)
                .setTitle("Paramètres sauvegardés")
                .setMessage(summary.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Paramètres enregistrés avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    // Inner class for Absence data
    private static class Absence {
        String startDate;
        String endDate;
        String reason;

        Absence(String startDate, String endDate, String reason) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.reason = reason;
        }
    }

    // Inner adapter for absences list - creates views programmatically
    private class AbsenceAdapter extends RecyclerView.Adapter<AbsenceAdapter.ViewHolder> {

        private List<Absence> absenceList;

        AbsenceAdapter(List<Absence> absenceList) {
            this.absenceList = absenceList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout itemLayout = new LinearLayout(parent.getContext());
            itemLayout.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(32, 24, 32, 24);
            itemLayout.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.background_secondary));

            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemLayout.getLayoutParams();
            layoutParams.bottomMargin = 8;
            itemLayout.setLayoutParams(layoutParams);

            LinearLayout infoLayout = new LinearLayout(parent.getContext());
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView datesText = new TextView(parent.getContext());
            datesText.setTextSize(14);
            datesText.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.text_primary));
            infoLayout.addView(datesText);

            TextView reasonText = new TextView(parent.getContext());
            reasonText.setTextSize(12);
            reasonText.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.text_secondary));
            infoLayout.addView(reasonText);

            itemLayout.addView(infoLayout);

            TextView deleteBtn = new TextView(parent.getContext());
            deleteBtn.setText("Supprimer");
            deleteBtn.setTextSize(12);
            deleteBtn.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.error));
            deleteBtn.setPadding(24, 12, 24, 12);
            itemLayout.addView(deleteBtn);

            return new ViewHolder(itemLayout, datesText, reasonText, deleteBtn);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Absence absence = absenceList.get(position);

            String dateRange = absence.startDate;
            if (!absence.startDate.equals(absence.endDate)) {
                dateRange += " → " + absence.endDate;
            }
            holder.datesText.setText(dateRange);
            holder.reasonText.setText(absence.reason);

            holder.deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(DoctorScheduleActivity.this)
                        .setTitle("Supprimer l'absence")
                        .setMessage("Voulez-vous vraiment supprimer cette absence ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                absenceList.remove(pos);
                                notifyItemRemoved(pos);
                                updateAbsencesEmptyState();
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return absenceList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView datesText;
            TextView reasonText;
            TextView deleteBtn;

            ViewHolder(View itemView, TextView datesText, TextView reasonText, TextView deleteBtn) {
                super(itemView);
                this.datesText = datesText;
                this.reasonText = reasonText;
                this.deleteBtn = deleteBtn;
            }
        }
    }
}
