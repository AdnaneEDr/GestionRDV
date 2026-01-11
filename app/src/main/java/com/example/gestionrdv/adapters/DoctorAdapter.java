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
import com.example.gestionrdv.models.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors;
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_card, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);

        holder.doctorName.setText(doctor.getFullName());
        holder.doctorSpecialty.setText(doctor.getSpecialization());
        holder.doctorRating.setText(String.valueOf(doctor.getRating()));
        holder.doctorExperience.setText(doctor.getExperience() + " ans");
        holder.doctorLocation.setText(doctor.getLocation());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDoctorClick(doctor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctors != null ? doctors.size() : 0;
    }

    public void updateDoctors(List<Doctor> newDoctors) {
        this.doctors = newDoctors;
        notifyDataSetChanged();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView doctorAvatar;
        TextView doctorName;
        TextView doctorSpecialty;
        TextView doctorRating;
        TextView doctorExperience;
        TextView doctorLocation;
        CardView availabilityBadge;
        TextView availabilityText;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorAvatar = itemView.findViewById(R.id.doctorAvatar);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorSpecialty = itemView.findViewById(R.id.doctorSpecialty);
            doctorRating = itemView.findViewById(R.id.doctorRating);
            doctorExperience = itemView.findViewById(R.id.doctorExperience);
            doctorLocation = itemView.findViewById(R.id.doctorLocation);
            availabilityBadge = itemView.findViewById(R.id.availabilityBadge);
            availabilityText = itemView.findViewById(R.id.availabilityText);
        }
    }
}
