package com.example.gestionrdv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.models.Appointment;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private OnAppointmentClickListener listener;
    private boolean showActions = true;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
        void onCancelClick(Appointment appointment);
        void onDetailsClick(Appointment appointment);
    }

    public AppointmentAdapter(List<Appointment> appointments, OnAppointmentClickListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    public AppointmentAdapter(List<Appointment> appointments, OnAppointmentClickListener listener, boolean showActions) {
        this.appointments = appointments;
        this.listener = listener;
        this.showActions = showActions;
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

        holder.appointmentDate.setText(appointment.getAppointmentDate());
        holder.doctorName.setText(appointment.getDoctorName());
        holder.doctorSpecialty.setText(appointment.getDoctorSpecialization());
        holder.appointmentTime.setText(appointment.getTimeRange());
        holder.appointmentReason.setText(appointment.getReason());

        // Set status badge
        String status = appointment.getStatus();
        int statusColor;
        String statusText;

        switch (status) {
            case "confirmed":
                statusColor = R.color.success;
                statusText = "Confirmé";
                break;
            case "pending":
                statusColor = R.color.warning;
                statusText = "En attente";
                break;
            case "cancelled":
                statusColor = R.color.error;
                statusText = "Annulé";
                break;
            case "completed":
                statusColor = R.color.info;
                statusText = "Terminé";
                break;
            default:
                statusColor = R.color.text_hint;
                statusText = status;
        }

        holder.statusBadge.setText(statusText);
        holder.statusBadgeCard.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), statusColor));

        // Show/hide action buttons based on status
        if (showActions && (status.equals("confirmed") || status.equals("pending"))) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
        } else {
            holder.actionButtonsLayout.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appointment);
            }
        });

        holder.cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClick(appointment);
            }
        });

        holder.detailsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentDate;
        CardView statusBadgeCard;
        TextView statusBadge;
        ImageView doctorAvatar;
        TextView doctorName;
        TextView doctorSpecialty;
        TextView appointmentTime;
        TextView appointmentReason;
        LinearLayout actionButtonsLayout;
        MaterialButton cancelButton;
        MaterialButton detailsButton;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            statusBadgeCard = itemView.findViewById(R.id.statusBadgeCard);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            doctorAvatar = itemView.findViewById(R.id.doctorAvatar);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorSpecialty = itemView.findViewById(R.id.doctorSpecialty);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            appointmentReason = itemView.findViewById(R.id.appointmentReason);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            detailsButton = itemView.findViewById(R.id.detailsButton);
        }
    }
}
