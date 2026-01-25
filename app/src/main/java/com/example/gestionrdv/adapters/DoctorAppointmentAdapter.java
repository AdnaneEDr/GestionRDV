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

/**
 * DoctorAppointmentAdapter - Adapter for displaying appointments from the doctor's perspective
 * Shows patient information instead of doctor information
 */
public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private OnDoctorAppointmentActionListener listener;
    private boolean showDate = false;

    public interface OnDoctorAppointmentActionListener {
        void onAppointmentClick(Appointment appointment);
        void onConfirmClick(Appointment appointment);
        void onCompleteClick(Appointment appointment);
        void onCancelClick(Appointment appointment);
    }

    public DoctorAppointmentAdapter(List<Appointment> appointments, OnDoctorAppointmentActionListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    public DoctorAppointmentAdapter(List<Appointment> appointments, OnDoctorAppointmentActionListener listener, boolean showDate) {
        this.appointments = appointments;
        this.listener = listener;
        this.showDate = showDate;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        // Set patient info
        String patientName = appointment.getPatientName();
        holder.patientName.setText(patientName != null ? patientName : "Patient");

        // Set time
        String timeRange = appointment.getTimeRange();
        holder.appointmentTime.setText(timeRange != null ? timeRange : appointment.getAppointmentTime());

        // Set reason
        String reason = appointment.getReason();
        holder.appointmentReason.setText(reason != null ? reason : "Consultation");

        // Show/hide date row
        if (showDate) {
            holder.dateRow.setVisibility(View.VISIBLE);
            holder.appointmentDate.setText(appointment.getAppointmentDate());
        } else {
            holder.dateRow.setVisibility(View.GONE);
        }

        // Set status badge
        String status = appointment.getStatus();
        updateStatusBadge(holder, status);

        // Configure action buttons based on status
        configureActionButtons(holder, appointment, status);

        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appointment);
            }
        });
    }

    private void updateStatusBadge(AppointmentViewHolder holder, String status) {
        int statusColor;
        String statusText;

        if (status == null) status = "pending";

        switch (status.toLowerCase()) {
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
    }

    private void configureActionButtons(AppointmentViewHolder holder, Appointment appointment, String status) {
        if (status == null) status = "pending";

        // Hide all buttons first
        holder.confirmButton.setVisibility(View.GONE);
        holder.completeButton.setVisibility(View.GONE);
        holder.cancelButton.setVisibility(View.GONE);
        holder.actionButtonsLayout.setVisibility(View.VISIBLE);
        holder.divider.setVisibility(View.VISIBLE);

        switch (status.toLowerCase()) {
            case "pending":
                // Show confirm and cancel buttons
                holder.confirmButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setVisibility(View.VISIBLE);

                holder.confirmButton.setOnClickListener(v -> {
                    if (listener != null) listener.onConfirmClick(appointment);
                });
                holder.cancelButton.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelClick(appointment);
                });
                break;

            case "confirmed":
                // Show complete and cancel buttons
                holder.completeButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setVisibility(View.VISIBLE);

                holder.completeButton.setOnClickListener(v -> {
                    if (listener != null) listener.onCompleteClick(appointment);
                });
                holder.cancelButton.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelClick(appointment);
                });
                break;

            case "completed":
            case "cancelled":
                // Hide action buttons for completed/cancelled appointments
                holder.actionButtonsLayout.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
                break;

            default:
                holder.actionButtonsLayout.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
        notifyDataSetChanged();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentTime;
        CardView statusBadgeCard;
        TextView statusBadge;
        ImageView patientAvatar;
        TextView patientName;
        TextView appointmentReason;
        LinearLayout dateRow;
        TextView appointmentDate;
        View divider;
        LinearLayout actionButtonsLayout;
        MaterialButton confirmButton;
        MaterialButton completeButton;
        MaterialButton cancelButton;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            statusBadgeCard = itemView.findViewById(R.id.statusBadgeCard);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            patientAvatar = itemView.findViewById(R.id.patientAvatar);
            patientName = itemView.findViewById(R.id.patientName);
            appointmentReason = itemView.findViewById(R.id.appointmentReason);
            dateRow = itemView.findViewById(R.id.dateRow);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            divider = itemView.findViewById(R.id.divider);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            confirmButton = itemView.findViewById(R.id.confirmButton);
            completeButton = itemView.findViewById(R.id.completeButton);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
