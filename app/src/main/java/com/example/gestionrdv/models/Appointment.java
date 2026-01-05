package com.example.gestionrdv.models;

/**
 * Appointment Model
 */
public class Appointment {
    private long id;
    private long patientId;
    private long doctorId;
    private String appointmentDate;
    private String appointmentTime;
    private String endTime;
    private String reason;
    private String notes;
    private String status; // "pending", "confirmed", "completed", "cancelled"
    private String createdAt;

    // Extended fields (not in DB, for display purposes)
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorLocation;
    private double doctorRating;

    public Appointment() {}

    public Appointment(long patientId, long doctorId, String date, String time, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = date;
        this.appointmentTime = time;
        this.reason = reason;
        this.status = "pending";
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public long getDoctorId() { return doctorId; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Extended fields
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String spec) { this.doctorSpecialization = spec; }

    public String getDoctorLocation() { return doctorLocation; }
    public void setDoctorLocation(String location) { this.doctorLocation = location; }

    public double getDoctorRating() { return doctorRating; }
    public void setDoctorRating(double rating) { this.doctorRating = rating; }

    public String getTimeRange() {
        return appointmentTime + " - " + endTime;
    }
}