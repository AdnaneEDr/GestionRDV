package com.example.gestionrdv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.database.repositories.AppointmentRepository;
import com.example.gestionrdv.models.Patient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Adapter for displaying patients list in admin panel
 */
public class AdminPatientAdapter extends RecyclerView.Adapter<AdminPatientAdapter.PatientViewHolder> {

    private List<Patient> patients;
    private OnPatientActionListener listener;
    private AppointmentRepository appointmentRepository;

    public interface OnPatientActionListener {
        void onPatientClick(Patient patient);
        void onPatientMenuClick(Patient patient);
    }

    public AdminPatientAdapter(OnPatientActionListener listener, AppointmentRepository appointmentRepository) {
        this.patients = new ArrayList<>();
        this.listener = listener;
        this.appointmentRepository = appointmentRepository;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_admin, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.bind(patient);
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class PatientViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        TextView phoneText;
        TextView ageText;
        TextView appointmentsCountText;
        ImageButton menuButton;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.patientAvatar);
            nameText = itemView.findViewById(R.id.patientName);
            phoneText = itemView.findViewById(R.id.patientPhone);
            ageText = itemView.findViewById(R.id.patientAge);
            appointmentsCountText = itemView.findViewById(R.id.appointmentsCount);
            menuButton = itemView.findViewById(R.id.menuButton);
        }

        public void bind(Patient patient) {
            nameText.setText(patient.getFullName());
            phoneText.setText(patient.getPhone() != null ? patient.getPhone() : "N/A");
            
            // Calculate age from birth date
            if (patient.getBirthDate() != null && !patient.getBirthDate().isEmpty()) {
                int age = calculateAge(patient.getBirthDate());
                ageText.setText(age + " ans");
            } else {
                ageText.setText("- ans");
            }

            // Get appointments count
            if (appointmentRepository != null) {
                com.example.gestionrdv.database.repositories.PatientRepository.PatientStats stats = 
                        new com.example.gestionrdv.database.repositories.PatientRepository(itemView.getContext())
                        .getPatientStats(patient.getId());
                appointmentsCountText.setText(stats.totalAppointments + " RDV");
            } else {
                appointmentsCountText.setText("0 RDV");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPatientClick(patient);
                }
            });

            menuButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPatientMenuClick(patient);
                }
            });
        }

        private int calculateAge(String birthDate) {
            try {
                // Parse birth date (format: yyyy-MM-dd)
                String[] parts = birthDate.split("-");
                if (parts.length != 3) return 0;
                
                int birthYear = Integer.parseInt(parts[0]);
                int birthMonth = Integer.parseInt(parts[1]);
                int birthDay = Integer.parseInt(parts[2]);

                Calendar today = Calendar.getInstance();
                int age = today.get(Calendar.YEAR) - birthYear;

                // Adjust if birthday hasn't occurred this year
                if (today.get(Calendar.MONTH) + 1 < birthMonth ||
                    (today.get(Calendar.MONTH) + 1 == birthMonth && today.get(Calendar.DAY_OF_MONTH) < birthDay)) {
                    age--;
                }

                return age;
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
