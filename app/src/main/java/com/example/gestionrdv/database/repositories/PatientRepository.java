package com.example.gestionrdv.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseContract.PatientEntry;
import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.models.Patient;

import java.util.ArrayList;
import java.util.List;

/**
 * PatientRepository - Handles all patient-related database operations
 */
public class PatientRepository {

    private static final String TAG = "PatientRepository";
    private DatabaseHelper dbHelper;

    public PatientRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Add new patient
     */
    public long addPatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PatientEntry.COLUMN_USER_ID, patient.getUserId());
        values.put(PatientEntry.COLUMN_FIRST_NAME, patient.getFirstName());
        values.put(PatientEntry.COLUMN_LAST_NAME, patient.getLastName());
        values.put(PatientEntry.COLUMN_PHONE, patient.getPhone());
        values.put(PatientEntry.COLUMN_BIRTH_DATE, patient.getBirthDate());
        values.put(PatientEntry.COLUMN_ADDRESS, patient.getAddress());
        values.put(PatientEntry.COLUMN_BLOOD_GROUP, patient.getBloodGroup());
        values.put(PatientEntry.COLUMN_PROFILE_IMAGE, patient.getProfileImage());

        long patientId = db.insert(PatientEntry.TABLE_NAME, null, values);

        if (patientId != -1) {
            Log.d(TAG, "Patient added successfully with ID: " + patientId);
        } else {
            Log.e(TAG, "Failed to add patient");
        }

        return patientId;
    }

    /**
     * Get patient by ID
     */
    public Patient getPatientById(long patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = PatientEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(patientId)};

        Cursor cursor = db.query(
                PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Patient patient = null;
        if (cursor != null && cursor.moveToFirst()) {
            patient = cursorToPatient(cursor);
            cursor.close();
        }

        return patient;
    }

    /**
     * Get patient by user ID
     */
    public Patient getPatientByUserId(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = PatientEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
                PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Patient patient = null;
        if (cursor != null && cursor.moveToFirst()) {
            patient = cursorToPatient(cursor);
            cursor.close();
        }

        return patient;
    }

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PatientEntry.TABLE_NAME,
                null, null, null, null, null,
                PatientEntry.COLUMN_FIRST_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Update patient information
     */
    public boolean updatePatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PatientEntry.COLUMN_FIRST_NAME, patient.getFirstName());
        values.put(PatientEntry.COLUMN_LAST_NAME, patient.getLastName());
        values.put(PatientEntry.COLUMN_PHONE, patient.getPhone());
        values.put(PatientEntry.COLUMN_BIRTH_DATE, patient.getBirthDate());
        values.put(PatientEntry.COLUMN_ADDRESS, patient.getAddress());
        values.put(PatientEntry.COLUMN_BLOOD_GROUP, patient.getBloodGroup());
        values.put(PatientEntry.COLUMN_PROFILE_IMAGE, patient.getProfileImage());

        String selection = PatientEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(patient.getId())};

        int rowsAffected = db.update(
                PatientEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return rowsAffected > 0;
    }

    /**
     * Delete patient
     */
    public boolean deletePatient(long patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = PatientEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(patientId)};

        int rowsDeleted = db.delete(
                PatientEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }

    /**
     * Search patients by name
     */
    public List<Patient> searchPatientsByName(String searchQuery) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = PatientEntry.COLUMN_FIRST_NAME + " LIKE ? OR " +
                PatientEntry.COLUMN_LAST_NAME + " LIKE ?";
        String[] selectionArgs = {"%" + searchQuery + "%", "%" + searchQuery + "%"};

        Cursor cursor = db.query(
                PatientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                PatientEntry.COLUMN_FIRST_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Get patient statistics
     */
    public PatientStats getPatientStats(long patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to count appointments by status
        String query = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed, " +
                "SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelled " +
                "FROM appointments WHERE patient_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

        PatientStats stats = new PatientStats();
        if (cursor != null && cursor.moveToFirst()) {
            stats.totalAppointments = cursor.getInt(0);
            stats.completedAppointments = cursor.getInt(1);
            stats.cancelledAppointments = cursor.getInt(2);
            cursor.close();
        }

        return stats;
    }

    /**
     * Helper method to convert cursor to Patient object
     */
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();
        patient.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PatientEntry._ID)));
        patient.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_USER_ID)));
        patient.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_FIRST_NAME)));
        patient.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_LAST_NAME)));
        patient.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_PHONE)));
        patient.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_BIRTH_DATE)));
        patient.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_ADDRESS)));
        patient.setBloodGroup(cursor.getString(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_BLOOD_GROUP)));
        patient.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow(PatientEntry.COLUMN_PROFILE_IMAGE)));
        return patient;
    }

    /**
     * Inner class for patient statistics
     */
    public static class PatientStats {
        public int totalAppointments;
        public int completedAppointments;
        public int cancelledAppointments;
    }
}