package com.example.gestionrdv.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseContract.AppointmentEntry;
import com.example.gestionrdv.database.DatabaseContract.DoctorEntry;
import com.example.gestionrdv.database.DatabaseContract.PatientEntry;
import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.models.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * AppointmentRepository - Handles all appointment-related database operations
 */
public class AppointmentRepository {

    private static final String TAG = "AppointmentRepository";
    private DatabaseHelper dbHelper;

    public AppointmentRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Create new appointment
     */
    public long createAppointment(Appointment appointment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppointmentEntry.COLUMN_PATIENT_ID, appointment.getPatientId());
        values.put(AppointmentEntry.COLUMN_DOCTOR_ID, appointment.getDoctorId());
        values.put(AppointmentEntry.COLUMN_APPOINTMENT_DATE, appointment.getAppointmentDate());
        values.put(AppointmentEntry.COLUMN_APPOINTMENT_TIME, appointment.getAppointmentTime());
        values.put(AppointmentEntry.COLUMN_END_TIME, appointment.getEndTime());
        values.put(AppointmentEntry.COLUMN_REASON, appointment.getReason());
        values.put(AppointmentEntry.COLUMN_NOTES, appointment.getNotes());
        values.put(AppointmentEntry.COLUMN_STATUS, appointment.getStatus());

        long appointmentId = db.insert(AppointmentEntry.TABLE_NAME, null, values);

        if (appointmentId != -1) {
            Log.d(TAG, "Appointment created successfully with ID: " + appointmentId);
        } else {
            Log.e(TAG, "Failed to create appointment");
        }

        return appointmentId;
    }

    /**
     * Get appointment by ID with full details
     */
    public Appointment getAppointmentById(long appointmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "p." + PatientEntry.COLUMN_FIRST_NAME + " || ' ' || p." + PatientEntry.COLUMN_LAST_NAME + " as patient_name, " +
                "d." + DoctorEntry.COLUMN_FIRST_NAME + " || ' ' || d." + DoctorEntry.COLUMN_LAST_NAME + " as doctor_name, " +
                "d." + DoctorEntry.COLUMN_SPECIALIZATION + " as doctor_specialization, " +
                "d." + DoctorEntry.COLUMN_LOCATION + " as doctor_location, " +
                "d." + DoctorEntry.COLUMN_RATING + " as doctor_rating " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + PatientEntry.TABLE_NAME + " p ON a." + AppointmentEntry.COLUMN_PATIENT_ID + " = p." + PatientEntry._ID + " " +
                "INNER JOIN " + DoctorEntry.TABLE_NAME + " d ON a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = d." + DoctorEntry._ID + " " +
                "WHERE a." + AppointmentEntry._ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(appointmentId)});

        Appointment appointment = null;
        if (cursor != null && cursor.moveToFirst()) {
            appointment = cursorToAppointment(cursor);
            cursor.close();
        }

        return appointment;
    }

    /**
     * Get all appointments for a patient
     */
    public List<Appointment> getPatientAppointments(long patientId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "d." + DoctorEntry.COLUMN_FIRST_NAME + " || ' ' || d." + DoctorEntry.COLUMN_LAST_NAME + " as doctor_name, " +
                "d." + DoctorEntry.COLUMN_SPECIALIZATION + " as doctor_specialization, " +
                "d." + DoctorEntry.COLUMN_LOCATION + " as doctor_location, " +
                "d." + DoctorEntry.COLUMN_RATING + " as doctor_rating " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + DoctorEntry.TABLE_NAME + " d ON a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = d." + DoctorEntry._ID + " " +
                "WHERE a." + AppointmentEntry.COLUMN_PATIENT_ID + " = ? " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " DESC, a." + AppointmentEntry.COLUMN_APPOINTMENT_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }

    /**
     * Get all appointments for a doctor
     */
    public List<Appointment> getDoctorAppointments(long doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "p." + PatientEntry.COLUMN_FIRST_NAME + " || ' ' || p." + PatientEntry.COLUMN_LAST_NAME + " as patient_name " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + PatientEntry.TABLE_NAME + " p ON a." + AppointmentEntry.COLUMN_PATIENT_ID + " = p." + PatientEntry._ID + " " +
                "WHERE a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = ? " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " DESC, a." + AppointmentEntry.COLUMN_APPOINTMENT_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(doctorId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }

    /**
     * Get appointments by status for a patient
     */
    public List<Appointment> getPatientAppointmentsByStatus(long patientId, String status) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "d." + DoctorEntry.COLUMN_FIRST_NAME + " || ' ' || d." + DoctorEntry.COLUMN_LAST_NAME + " as doctor_name, " +
                "d." + DoctorEntry.COLUMN_SPECIALIZATION + " as doctor_specialization, " +
                "d." + DoctorEntry.COLUMN_LOCATION + " as doctor_location, " +
                "d." + DoctorEntry.COLUMN_RATING + " as doctor_rating " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + DoctorEntry.TABLE_NAME + " d ON a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = d." + DoctorEntry._ID + " " +
                "WHERE a." + AppointmentEntry.COLUMN_PATIENT_ID + " = ? AND a." + AppointmentEntry.COLUMN_STATUS + " = ? " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId), status});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }

    /**
     * Get upcoming appointments for a patient
     */
    public List<Appointment> getUpcomingPatientAppointments(long patientId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "d." + DoctorEntry.COLUMN_FIRST_NAME + " || ' ' || d." + DoctorEntry.COLUMN_LAST_NAME + " as doctor_name, " +
                "d." + DoctorEntry.COLUMN_SPECIALIZATION + " as doctor_specialization, " +
                "d." + DoctorEntry.COLUMN_LOCATION + " as doctor_location, " +
                "d." + DoctorEntry.COLUMN_RATING + " as doctor_rating " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + DoctorEntry.TABLE_NAME + " d ON a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = d." + DoctorEntry._ID + " " +
                "WHERE a." + AppointmentEntry.COLUMN_PATIENT_ID + " = ? " +
                "AND a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " >= date('now') " +
                "AND a." + AppointmentEntry.COLUMN_STATUS + " IN ('pending', 'confirmed') " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " ASC, a." + AppointmentEntry.COLUMN_APPOINTMENT_TIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }

    /**
     * Get doctor's appointments for a specific date
     */
    public List<Appointment> getDoctorAppointmentsByDate(long doctorId, String date) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "p." + PatientEntry.COLUMN_FIRST_NAME + " || ' ' || p." + PatientEntry.COLUMN_LAST_NAME + " as patient_name " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + PatientEntry.TABLE_NAME + " p ON a." + AppointmentEntry.COLUMN_PATIENT_ID + " = p." + PatientEntry._ID + " " +
                "WHERE a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = ? " +
                "AND a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " = ? " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_TIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(doctorId), date});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }

    /**
     * Update appointment
     */
    public boolean updateAppointment(Appointment appointment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppointmentEntry.COLUMN_APPOINTMENT_DATE, appointment.getAppointmentDate());
        values.put(AppointmentEntry.COLUMN_APPOINTMENT_TIME, appointment.getAppointmentTime());
        values.put(AppointmentEntry.COLUMN_END_TIME, appointment.getEndTime());
        values.put(AppointmentEntry.COLUMN_REASON, appointment.getReason());
        values.put(AppointmentEntry.COLUMN_NOTES, appointment.getNotes());
        values.put(AppointmentEntry.COLUMN_STATUS, appointment.getStatus());

        String selection = AppointmentEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(appointment.getId())};

        int rowsAffected = db.update(
                AppointmentEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return rowsAffected > 0;
    }

    /**
     * Update appointment status
     */
    public boolean updateAppointmentStatus(long appointmentId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppointmentEntry.COLUMN_STATUS, status);

        String selection = AppointmentEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(appointmentId)};

        int rowsAffected = db.update(
                AppointmentEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return rowsAffected > 0;
    }

    /**
     * Cancel appointment
     */
    public boolean cancelAppointment(long appointmentId) {
        return updateAppointmentStatus(appointmentId, "cancelled");
    }

    /**
     * Delete appointment
     */
    public boolean deleteAppointment(long appointmentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = AppointmentEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(appointmentId)};

        int rowsDeleted = db.delete(
                AppointmentEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }

    /**
     * Check if time slot is available
     */
    public boolean isTimeSlotAvailable(long doctorId, String date, String time) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = AppointmentEntry.COLUMN_DOCTOR_ID + " = ? AND " +
                AppointmentEntry.COLUMN_APPOINTMENT_DATE + " = ? AND " +
                AppointmentEntry.COLUMN_APPOINTMENT_TIME + " = ? AND " +
                AppointmentEntry.COLUMN_STATUS + " != 'cancelled'";
        String[] selectionArgs = {String.valueOf(doctorId), date, time};

        Cursor cursor = db.query(
                AppointmentEntry.TABLE_NAME,
                new String[]{AppointmentEntry._ID},
                selection,
                selectionArgs,
                null, null, null
        );

        boolean isAvailable = cursor == null || cursor.getCount() == 0;
        if (cursor != null) {
            cursor.close();
        }

        return isAvailable;
    }

    /**
     * Helper method to convert cursor to Appointment object
     */
    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment appointment = new Appointment();
        appointment.setId(cursor.getLong(cursor.getColumnIndexOrThrow(AppointmentEntry._ID)));
        appointment.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_PATIENT_ID)));
        appointment.setDoctorId(cursor.getLong(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_DOCTOR_ID)));
        appointment.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_APPOINTMENT_DATE)));
        appointment.setAppointmentTime(cursor.getString(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_APPOINTMENT_TIME)));
        appointment.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_END_TIME)));
        appointment.setReason(cursor.getString(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_REASON)));
        appointment.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_NOTES)));
        appointment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_STATUS)));
        appointment.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(AppointmentEntry.COLUMN_CREATED_AT)));

        // Extended fields (if available in the cursor)
        try {
            appointment.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
        } catch (Exception e) { /* Column might not exist */ }

        try {
            appointment.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
            appointment.setDoctorSpecialization(cursor.getString(cursor.getColumnIndexOrThrow("doctor_specialization")));
            appointment.setDoctorLocation(cursor.getString(cursor.getColumnIndexOrThrow("doctor_location")));
            appointment.setDoctorRating(cursor.getDouble(cursor.getColumnIndexOrThrow("doctor_rating")));
        } catch (Exception e) { /* Columns might not exist */ }

        return appointment;
    }
    // MÉTHODES À AJOUTER À AppointmentRepository.java

    /**
     * Get all appointments (for admin)
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "p." + PatientEntry.COLUMN_FIRST_NAME + " || ' ' || p." + PatientEntry.COLUMN_LAST_NAME + " as patient_name, " +
                "d." + DoctorEntry.COLUMN_FIRST_NAME + " || ' ' || d." + DoctorEntry.COLUMN_LAST_NAME + " as doctor_name, " +
                "d." + DoctorEntry.COLUMN_SPECIALIZATION + " as doctor_specialization " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + PatientEntry.TABLE_NAME + " p ON a." + AppointmentEntry.COLUMN_PATIENT_ID + " = p." + PatientEntry._ID + " " +
                "INNER JOIN " + DoctorEntry.TABLE_NAME + " d ON a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = d." + DoctorEntry._ID + " " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " DESC, a." + AppointmentEntry.COLUMN_APPOINTMENT_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }

    /**
     * Get appointments by date (for admin calendar)
     */
    public List<Appointment> getAppointmentsByDate(String date) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "p." + PatientEntry.COLUMN_FIRST_NAME + " || ' ' || p." + PatientEntry.COLUMN_LAST_NAME + " as patient_name, " +
                "d." + DoctorEntry.COLUMN_FIRST_NAME + " || ' ' || d." + DoctorEntry.COLUMN_LAST_NAME + " as doctor_name, " +
                "d." + DoctorEntry.COLUMN_SPECIALIZATION + " as doctor_specialization " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + PatientEntry.TABLE_NAME + " p ON a." + AppointmentEntry.COLUMN_PATIENT_ID + " = p." + PatientEntry._ID + " " +
                "INNER JOIN " + DoctorEntry.TABLE_NAME + " d ON a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = d." + DoctorEntry._ID + " " +
                "WHERE a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " = ? " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_TIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }

    /**
     * Get appointments by date and status (for admin calendar filtering)
     */
    public List<Appointment> getAppointmentsByDateAndStatus(String date, String status) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, " +
                "p." + PatientEntry.COLUMN_FIRST_NAME + " || ' ' || p." + PatientEntry.COLUMN_LAST_NAME + " as patient_name, " +
                "d." + DoctorEntry.COLUMN_FIRST_NAME + " || ' ' || d." + DoctorEntry.COLUMN_LAST_NAME + " as doctor_name, " +
                "d." + DoctorEntry.COLUMN_SPECIALIZATION + " as doctor_specialization " +
                "FROM " + AppointmentEntry.TABLE_NAME + " a " +
                "INNER JOIN " + PatientEntry.TABLE_NAME + " p ON a." + AppointmentEntry.COLUMN_PATIENT_ID + " = p." + PatientEntry._ID + " " +
                "INNER JOIN " + DoctorEntry.TABLE_NAME + " d ON a." + AppointmentEntry.COLUMN_DOCTOR_ID + " = d." + DoctorEntry._ID + " " +
                "WHERE a." + AppointmentEntry.COLUMN_APPOINTMENT_DATE + " = ? " +
                "AND a." + AppointmentEntry.COLUMN_STATUS + " = ? " +
                "ORDER BY a." + AppointmentEntry.COLUMN_APPOINTMENT_TIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{date, status});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return appointments;
    }
}