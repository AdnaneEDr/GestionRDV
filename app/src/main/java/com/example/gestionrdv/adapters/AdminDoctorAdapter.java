package com.example.gestionrdv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.models.Doctor;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying doctors list in admin panel
 */
public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors;
    private OnDoctorActionListener listener;
    private DoctorRepository doctorRepository;

    public interface OnDoctorActionListener {
        void onDoctorClick(Doctor doctor);
        void onStatusChanged(Doctor doctor, boolean isActive);
    }

    public AdminDoctorAdapter(OnDoctorActionListener listener, DoctorRepository doctorRepository) {
        this.doctors = new ArrayList<>();
        this.listener = listener;
        this.doctorRepository = doctorRepository;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        TextView specialtyText;
        TextView todayAppointments;
        TextView weekAppointments;
        TextView ratingText;
        SwitchMaterial statusSwitch;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.doctorAvatar);
            nameText = itemView.findViewById(R.id.doctorName);
            specialtyText = itemView.findViewById(R.id.doctorSpecialty);
            todayAppointments = itemView.findViewById(R.id.todayAppointments);
            weekAppointments = itemView.findViewById(R.id.weekAppointments);
            ratingText = itemView.findViewById(R.id.doctorRating);
            statusSwitch = itemView.findViewById(R.id.statusSwitch);
        }

        public void bind(Doctor doctor) {
            nameText.setText(doctor.getFullName());
            specialtyText.setText(doctor.getSpecialization());
            ratingText.setText(String.format("%.1f", doctor.getRating()));

            // Get doctor statistics
            if (doctorRepository != null) {
                DoctorRepository.DoctorStats stats = doctorRepository.getDoctorStats(doctor.getId());
                todayAppointments.setText(String.valueOf(stats.todayAppointments));
                weekAppointments.setText(String.valueOf(stats.weekAppointments));
            } else {
                todayAppointments.setText("0");
                weekAppointments.setText("0");
            }

            // Status switch
            statusSwitch.setChecked(true);

            statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onStatusChanged(doctor, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDoctorClick(doctor);
                }
            });
        }
    }
}