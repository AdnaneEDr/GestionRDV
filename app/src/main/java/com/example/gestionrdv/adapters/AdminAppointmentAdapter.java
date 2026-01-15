package com.example.gestionrdv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.models.Appointment;

import java.util.List;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onEditAppointment(Appointment appointment);
        void onChangeStatus(Appointment appointment, String newStatus);
        void onDeleteAppointment(Appointment appointment);
    }

    public AdminAppointmentAdapter(List<Appointment> appointments, OnAppointmentActionListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        // Patient info
        String patientName = appointment.getPatientName() != null ?
                appointment.getPatientName() : "Patient";
        holder.patientNameText.setText(patientName);

        // Doctor info
        String doctorName = appointment.getDoctorName() != null ?
                "Dr. " + appointment.getDoctorName() : "Médecin";
        String specialization = appointment.getDoctorSpecialization() != null ?
                appointment.getDoctorSpecialization() : "";
        holder.doctorNameText.setText(doctorName);
        if (!specialization.isEmpty()) {
            holder.specializationText.setText(specialization);
            holder.specializationText.setVisibility(View.VISIBLE);
        } else {
            holder.specializationText.setVisibility(View.GONE);
        }

        // Date and time
        holder.dateText.setText("📅 " + appointment.getAppointmentDate());
        holder.timeText.setText("🕐 " + appointment.getAppointmentTime());

        // Reason
        String reason = appointment.getReason() != null && !appointment.getReason().isEmpty() ?
                appointment.getReason() : "Non spécifié";
        holder.reasonText.setText(reason);

        // Status badge
        String status = appointment.getStatus();
        int statusColor;
        String statusLabel;

        switch (status) {
            case "confirmed":
                statusColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.holo_green_dark);
                statusLabel = "Confirmé";
                break;
            case "pending":
                statusColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.holo_orange_dark);
                statusLabel = "En attente";
                break;
            case "cancelled":
                statusColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.holo_red_dark);
                statusLabel = "Annulé";
                break;
            case "completed":
                statusColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.holo_blue_dark);
                statusLabel = "Terminé";
                break;
            default:
                statusColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.darker_gray);
                statusLabel = status;
        }

        holder.statusBadge.setText(statusLabel);
        holder.statusBadge.setTextColor(statusColor);

        // Menu button
        holder.menuButton.setOnClickListener(v -> showPopupMenu(v, appointment));
    }

    private void showPopupMenu(View view, Appointment appointment) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.menu_appointment_actions);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                listener.onEditAppointment(appointment);
                return true;
            } else if (itemId == R.id.action_confirm) {
                listener.onChangeStatus(appointment, "confirmed");
                return true;
            } else if (itemId == R.id.action_complete) {
                listener.onChangeStatus(appointment, "completed");
                return true;
            } else if (itemId == R.id.action_cancel) {
                listener.onChangeStatus(appointment, "cancelled");
                return true;
            } else if (itemId == R.id.action_delete) {
                listener.onDeleteAppointment(appointment);
                return true;
            }

            return false;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patientNameText, doctorNameText, specializationText;
        TextView dateText, timeText, reasonText, statusBadge;
        ImageButton menuButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameText = itemView.findViewById(R.id.patientNameText);
            doctorNameText = itemView.findViewById(R.id.doctorNameText);
            specializationText = itemView.findViewById(R.id.specializationText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            reasonText = itemView.findViewById(R.id.reasonText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}