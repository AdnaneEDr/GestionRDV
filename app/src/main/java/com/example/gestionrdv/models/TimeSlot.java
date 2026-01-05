package com.example.gestionrdv.models;

/**
 * TimeSlot Model
 */
public class TimeSlot {
    private long id;
    private long doctorId;
    private int dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean isAvailable;

    public TimeSlot() {}

    public TimeSlot(long doctorId, int dayOfWeek, String startTime, String endTime) {
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = true;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getDoctorId() { return doctorId; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getTimeRange() {
        return startTime + " - " + endTime;
    }
}