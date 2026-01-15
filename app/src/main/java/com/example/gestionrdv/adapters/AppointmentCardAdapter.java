package com.example.gestionrdv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.models.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying appointment cards
 */
public class AppointmentCardAdapter extends RecyclerView.Adapter<AppointmentCardAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    public AppointmentCardAdapter(OnAppointmentClickListener listener) {
        this.appointments = new ArrayList<>();
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_card, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentDateText;
        TextView appointmentTimeText;
        TextView doctorNameText;
        TextView doctorSpecialtyText;
        TextView statusBadge;
        CardView statusBadgeCard;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentDateText = itemView.findViewById(R.id.appointmentDate);
            appointmentTimeText = itemView.findViewById(R.id.appointmentTime);
            doctorNameText = itemView.findViewById(R.id.doctorName);
            doctorSpecialtyText = itemView.findViewById(R.id.doctorSpecialty);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            statusBadgeCard = itemView.findViewById(R.id.statusBadgeCard);
        }

        public void bind(Appointment appointment) {
            // Set date
            if (appointment.getAppointmentDate() != null) {
                appointmentDateText.setText(appointment.getAppointmentDate());
            }

            // Set time
            if (appointment.getAppointmentTime() != null) {
                appointmentTimeText.setText(appointment.getAppointmentTime());
            }

            // Set doctor name
            if (appointment.getDoctorName() != null) {
                doctorNameText.setText(appointment.getDoctorName());
            }

            // Set doctor specialty
            if (appointment.getDoctorSpecialization() != null) {
                doctorSpecialtyText.setText(appointment.getDoctorSpecialization());
            }

            // Set status badge and color
            String status = appointment.getStatus();
            int color = itemView.getContext().getColor(R.color.text_hint);
            String statusText = "En attente";

            if ("pending".equals(status)) {
                color = itemView.getContext().getColor(R.color.warning);
                statusText = "En attente";
            } else if ("confirmed".equals(status)) {
                color = itemView.getContext().getColor(R.color.info);
                statusText = "Confirmé";
            } else if ("completed".equals(status)) {
                color = itemView.getContext().getColor(R.color.success);
                statusText = "Terminé";
            } else if ("cancelled".equals(status)) {
                color = itemView.getContext().getColor(R.color.error);
                statusText = "Annulé";
            }

            statusBadge.setText(statusText);
            if (statusBadgeCard != null) {
                statusBadgeCard.setCardBackgroundColor(color);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppointmentClick(appointment);
                }
            });
        }
    }
}
