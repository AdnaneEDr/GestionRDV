package com.example.gestionrdv.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseContract.DoctorEntry;
import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.models.Doctor;

import java.util.ArrayList;
import java.util.List;

/**
 * DoctorRepository - Handles all doctor-related database operations
 */
public class DoctorRepository {

    private static final String TAG = "DoctorRepository";
    private DatabaseHelper dbHelper;

    public DoctorRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Add new doctor
     */
    public long addDoctor(Doctor doctor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DoctorEntry.COLUMN_USER_ID, doctor.getUserId());
        values.put(DoctorEntry.COLUMN_FIRST_NAME, doctor.getFirstName());
        values.put(DoctorEntry.COLUMN_LAST_NAME, doctor.getLastName());
        values.put(DoctorEntry.COLUMN_PHONE, doctor.getPhone());
        values.put(DoctorEntry.COLUMN_SPECIALIZATION, doctor.getSpecialization());
        values.put(DoctorEntry.COLUMN_QUALIFICATION, doctor.getQualification());
        values.put(DoctorEntry.COLUMN_EXPERIENCE, doctor.getExperience());
        values.put(DoctorEntry.COLUMN_CONSULTATION_FEE, doctor.getConsultationFee());
        values.put(DoctorEntry.COLUMN_LOCATION, doctor.getLocation());
        values.put(DoctorEntry.COLUMN_RATING, doctor.getRating());
        values.put(DoctorEntry.COLUMN_PROFILE_IMAGE, doctor.getProfileImage());

        long doctorId = db.insert(DoctorEntry.TABLE_NAME, null, values);

        if (doctorId != -1) {
            Log.d(TAG, "Doctor added successfully with ID: " + doctorId);
        } else {
            Log.e(TAG, "Failed to add doctor");
        }

        return doctorId;
    }

    /**
     * Get doctor by ID
     */
    public Doctor getDoctorById(long doctorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DoctorEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(doctorId)};

        Cursor cursor = db.query(
                DoctorEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Doctor doctor = null;
        if (cursor != null && cursor.moveToFirst()) {
            doctor = cursorToDoctor(cursor);
            cursor.close();
        }

        return doctor;
    }

    /**
     * Get doctor by user ID
     */
    public Doctor getDoctorByUserId(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DoctorEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
                DoctorEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Doctor doctor = null;
        if (cursor != null && cursor.moveToFirst()) {
            doctor = cursorToDoctor(cursor);
            cursor.close();
        }

        return doctor;
    }

    /**
     * Get all doctors
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DoctorEntry.TABLE_NAME,
                null, null, null, null, null,
                DoctorEntry.COLUMN_RATING + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                doctors.add(cursorToDoctor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return doctors;
    }

    /**
     * Get doctors by specialization
     */
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        List<Doctor> doctors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DoctorEntry.COLUMN_SPECIALIZATION + " = ?";
        String[] selectionArgs = {specialization};

        Cursor cursor = db.query(
                DoctorEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                DoctorEntry.COLUMN_RATING + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                doctors.add(cursorToDoctor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return doctors;
    }

    /**
     * Search doctors by name or specialization
     */
    public List<Doctor> searchDoctors(String searchQuery) {
        List<Doctor> doctors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DoctorEntry.COLUMN_FIRST_NAME + " LIKE ? OR " +
                DoctorEntry.COLUMN_LAST_NAME + " LIKE ? OR " +
                DoctorEntry.COLUMN_SPECIALIZATION + " LIKE ?";
        String[] selectionArgs = {
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%"
        };

        Cursor cursor = db.query(
                DoctorEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                DoctorEntry.COLUMN_RATING + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                doctors.add(cursorToDoctor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return doctors;
    }

    /**
     * Update doctor information
     */
    public boolean updateDoctor(Doctor doctor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DoctorEntry.COLUMN_FIRST_NAME, doctor.getFirstName());
        values.put(DoctorEntry.COLUMN_LAST_NAME, doctor.getLastName());
        values.put(DoctorEntry.COLUMN_PHONE, doctor.getPhone());
        values.put(DoctorEntry.COLUMN_SPECIALIZATION, doctor.getSpecialization());
        values.put(DoctorEntry.COLUMN_QUALIFICATION, doctor.getQualification());
        values.put(DoctorEntry.COLUMN_EXPERIENCE, doctor.getExperience());
        values.put(DoctorEntry.COLUMN_CONSULTATION_FEE, doctor.getConsultationFee());
        values.put(DoctorEntry.COLUMN_LOCATION, doctor.getLocation());
        values.put(DoctorEntry.COLUMN_RATING, doctor.getRating());
        values.put(DoctorEntry.COLUMN_PROFILE_IMAGE, doctor.getProfileImage());

        String selection = DoctorEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(doctor.getId())};

        int rowsAffected = db.update(
                DoctorEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return rowsAffected > 0;
    }

    /**
     * Delete doctor
     */
    public boolean deleteDoctor(long doctorId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = DoctorEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(doctorId)};

        int rowsDeleted = db.delete(
                DoctorEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }

    /**
     * Get doctor statistics
     */
    public DoctorStats getDoctorStats(long doctorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to count appointments by status and date
        String query = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed, " +
                "SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as pending, " +
                "SUM(CASE WHEN appointment_date = date('now') THEN 1 ELSE 0 END) as today, " +
                "SUM(CASE WHEN appointment_date >= date('now') AND " +
                "appointment_date < date('now', '+7 days') THEN 1 ELSE 0 END) as thisWeek " +
                "FROM appointments WHERE doctor_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(doctorId)});

        DoctorStats stats = new DoctorStats();
        if (cursor != null && cursor.moveToFirst()) {
            stats.totalAppointments = cursor.getInt(0);
            stats.completedAppointments = cursor.getInt(1);
            stats.pendingAppointments = cursor.getInt(2);
            stats.todayAppointments = cursor.getInt(3);
            stats.weekAppointments = cursor.getInt(4);
            cursor.close();
        }

        return stats;
    }

    /**
     * Get all unique specializations
     */
    public List<String> getAllSpecializations() {
        List<String> specializations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                true, // distinct
                DoctorEntry.TABLE_NAME,
                new String[]{DoctorEntry.COLUMN_SPECIALIZATION},
                null, null, null, null,
                DoctorEntry.COLUMN_SPECIALIZATION + " ASC",
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                specializations.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return specializations;
    }

    /**
     * Helper method to convert cursor to Doctor object
     */
    private Doctor cursorToDoctor(Cursor cursor) {
        Doctor doctor = new Doctor();
        doctor.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DoctorEntry._ID)));
        doctor.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_USER_ID)));
        doctor.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_FIRST_NAME)));
        doctor.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_LAST_NAME)));
        doctor.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_PHONE)));
        doctor.setSpecialization(cursor.getString(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_SPECIALIZATION)));
        doctor.setQualification(cursor.getString(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_QUALIFICATION)));
        doctor.setExperience(cursor.getInt(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_EXPERIENCE)));
        doctor.setConsultationFee(cursor.getDouble(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_CONSULTATION_FEE)));
        doctor.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_LOCATION)));
        doctor.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_RATING)));
        doctor.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow(DoctorEntry.COLUMN_PROFILE_IMAGE)));
        return doctor;
    }

    /**
     * Inner class for doctor statistics
     */
    public static class DoctorStats {
        public int totalAppointments;
        public int completedAppointments;
        public int pendingAppointments;
        public int todayAppointments;
        public int weekAppointments;
    }
}