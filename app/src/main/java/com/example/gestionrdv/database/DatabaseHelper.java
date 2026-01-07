package com.example.gestionrdv.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Import the Contract classes
import com.example.gestionrdv.database.DatabaseContract.*;

/**
 * Database Helper - Manages database creation and version management
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // Create tables in order (respecting foreign key constraints)
        db.execSQL(UserEntry.CREATE_TABLE);
        db.execSQL(PatientEntry.CREATE_TABLE);
        db.execSQL(DoctorEntry.CREATE_TABLE);
        db.execSQL(AdminEntry.CREATE_TABLE);
        db.execSQL(AppointmentEntry.CREATE_TABLE);
        db.execSQL(TimeSlotEntry.CREATE_TABLE);

        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON;");

        Log.d(TAG, "Database tables created successfully");

        // Insert sample data for testing
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop all tables
        db.execSQL(TimeSlotEntry.DROP_TABLE);
        db.execSQL(AppointmentEntry.DROP_TABLE);
        db.execSQL(AdminEntry.DROP_TABLE);
        db.execSQL(DoctorEntry.DROP_TABLE);
        db.execSQL(PatientEntry.DROP_TABLE);
        db.execSQL(UserEntry.DROP_TABLE);

        // Recreate tables
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Insert sample data for testing
     */
    private void insertSampleData(SQLiteDatabase db) {
        Log.d(TAG, "Inserting sample data...");

        try {
            db.beginTransaction();

            // --- Sample Admin User ---
            ContentValues adminUser = new ContentValues();
            adminUser.put(UserEntry.COLUMN_EMAIL, "admin@hospital.ma");
            adminUser.put(UserEntry.COLUMN_PASSWORD, "admin123"); // In production, hash this!
            adminUser.put(UserEntry.COLUMN_USER_TYPE, "admin");
            long adminUserId = db.insert(UserEntry.TABLE_NAME, null, adminUser);

            ContentValues admin = new ContentValues();
            admin.put(AdminEntry.COLUMN_USER_ID, adminUserId);
            admin.put(AdminEntry.COLUMN_FIRST_NAME, "Admin");
            admin.put(AdminEntry.COLUMN_LAST_NAME, "Principal");
            admin.put(AdminEntry.COLUMN_PHONE, "+212 5 39 12 34 56");
            admin.put(AdminEntry.COLUMN_ROLE, "super_admin");
            db.insert(AdminEntry.TABLE_NAME, null, admin);

            // --- Sample Doctor 1 - Dr. Fatima Zahra ---
            ContentValues doctor1User = new ContentValues();
            doctor1User.put(UserEntry.COLUMN_EMAIL, "fatima.zahra@hospital.ma");
            doctor1User.put(UserEntry.COLUMN_PASSWORD, "doctor123");
            doctor1User.put(UserEntry.COLUMN_USER_TYPE, "doctor");
            long doctorUserId1 = db.insert(UserEntry.TABLE_NAME, null, doctor1User);

            ContentValues doctor1 = new ContentValues();
            doctor1.put(DoctorEntry.COLUMN_USER_ID, doctorUserId1);
            doctor1.put(DoctorEntry.COLUMN_FIRST_NAME, "Fatima");
            doctor1.put(DoctorEntry.COLUMN_LAST_NAME, "Zahra");
            doctor1.put(DoctorEntry.COLUMN_PHONE, "+212 6 12 34 56 78");
            doctor1.put(DoctorEntry.COLUMN_SPECIALIZATION, "Médecine Générale");
            doctor1.put(DoctorEntry.COLUMN_QUALIFICATION, "Doctorat en Médecine");
            doctor1.put(DoctorEntry.COLUMN_EXPERIENCE, 10);
            doctor1.put(DoctorEntry.COLUMN_CONSULTATION_FEE, 300.0);
            doctor1.put(DoctorEntry.COLUMN_LOCATION, "Cabinet Médical, Tanger");
            doctor1.put(DoctorEntry.COLUMN_RATING, 4.8);
            long doctorId1 = db.insert(DoctorEntry.TABLE_NAME, null, doctor1);

            // --- Sample Doctor 2 - Dr. Ahmed Bennani ---
            ContentValues doctor2User = new ContentValues();
            doctor2User.put(UserEntry.COLUMN_EMAIL, "ahmed.bennani@hospital.ma");
            doctor2User.put(UserEntry.COLUMN_PASSWORD, "doctor123");
            doctor2User.put(UserEntry.COLUMN_USER_TYPE, "doctor");
            long doctorUserId2 = db.insert(UserEntry.TABLE_NAME, null, doctor2User);

            ContentValues doctor2 = new ContentValues();
            doctor2.put(DoctorEntry.COLUMN_USER_ID, doctorUserId2);
            doctor2.put(DoctorEntry.COLUMN_FIRST_NAME, "Ahmed");
            doctor2.put(DoctorEntry.COLUMN_LAST_NAME, "Bennani");
            doctor2.put(DoctorEntry.COLUMN_PHONE, "+212 6 98 76 54 32");
            doctor2.put(DoctorEntry.COLUMN_SPECIALIZATION, "Cardiologie");
            doctor2.put(DoctorEntry.COLUMN_QUALIFICATION, "Spécialiste en Cardiologie");
            doctor2.put(DoctorEntry.COLUMN_EXPERIENCE, 15);
            doctor2.put(DoctorEntry.COLUMN_CONSULTATION_FEE, 500.0);
            doctor2.put(DoctorEntry.COLUMN_LOCATION, "Clinique Al Amal, Tanger");
            doctor2.put(DoctorEntry.COLUMN_RATING, 4.9);
            db.insert(DoctorEntry.TABLE_NAME, null, doctor2);

            // --- Sample Time Slots for Dr. Fatima ---
            for (int day = 1; day <= 5; day++) {
                // Morning slots
                ContentValues morningSlot = new ContentValues();
                morningSlot.put(TimeSlotEntry.COLUMN_DOCTOR_ID, doctorId1);
                morningSlot.put(TimeSlotEntry.COLUMN_DAY_OF_WEEK, day);
                morningSlot.put(TimeSlotEntry.COLUMN_START_TIME, "09:00");
                morningSlot.put(TimeSlotEntry.COLUMN_END_TIME, "12:00");
                morningSlot.put(TimeSlotEntry.COLUMN_IS_AVAILABLE, 1);
                db.insert(TimeSlotEntry.TABLE_NAME, null, morningSlot);

                // Afternoon slots
                ContentValues afternoonSlot = new ContentValues();
                afternoonSlot.put(TimeSlotEntry.COLUMN_DOCTOR_ID, doctorId1);
                afternoonSlot.put(TimeSlotEntry.COLUMN_DAY_OF_WEEK, day);
                afternoonSlot.put(TimeSlotEntry.COLUMN_START_TIME, "14:00");
                afternoonSlot.put(TimeSlotEntry.COLUMN_END_TIME, "17:00");
                afternoonSlot.put(TimeSlotEntry.COLUMN_IS_AVAILABLE, 1);
                db.insert(TimeSlotEntry.TABLE_NAME, null, afternoonSlot);
            }

            // --- Sample Patient ---
            ContentValues patientUser = new ContentValues();
            patientUser.put(UserEntry.COLUMN_EMAIL, "mohammed.alami@email.com");
            patientUser.put(UserEntry.COLUMN_PASSWORD, "patient123");
            patientUser.put(UserEntry.COLUMN_USER_TYPE, "patient");
            long patientUserId = db.insert(UserEntry.TABLE_NAME, null, patientUser);

            ContentValues patient = new ContentValues();
            patient.put(PatientEntry.COLUMN_USER_ID, patientUserId);
            patient.put(PatientEntry.COLUMN_FIRST_NAME, "Mohammed");
            patient.put(PatientEntry.COLUMN_LAST_NAME, "Alami");
            patient.put(PatientEntry.COLUMN_PHONE, "+212 6 11 22 33 44");
            patient.put(PatientEntry.COLUMN_BIRTH_DATE, "1990-03-15");
            patient.put(PatientEntry.COLUMN_ADDRESS, "Tanger, Maroc");
            patient.put(PatientEntry.COLUMN_BLOOD_GROUP, "O+");
            long patientId = db.insert(PatientEntry.TABLE_NAME, null, patient);

            // --- Sample Appointment ---
            ContentValues appointment = new ContentValues();
            appointment.put(AppointmentEntry.COLUMN_PATIENT_ID, patientId);
            appointment.put(AppointmentEntry.COLUMN_DOCTOR_ID, doctorId1);
            appointment.put(AppointmentEntry.COLUMN_APPOINTMENT_DATE, "2024-12-25");
            appointment.put(AppointmentEntry.COLUMN_APPOINTMENT_TIME, "10:30");
            appointment.put(AppointmentEntry.COLUMN_END_TIME, "11:00");
            appointment.put(AppointmentEntry.COLUMN_REASON, "Consultation générale");
            appointment.put(AppointmentEntry.COLUMN_NOTES, "");
            appointment.put(AppointmentEntry.COLUMN_STATUS, "confirmed");
            db.insert(AppointmentEntry.TABLE_NAME, null, appointment);

            db.setTransactionSuccessful();
            Log.d(TAG, "Sample data inserted successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error inserting sample data: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Clear all data from database (useful for testing)
     */
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TimeSlotEntry.TABLE_NAME);
        db.execSQL("DELETE FROM " + AppointmentEntry.TABLE_NAME);
        db.execSQL("DELETE FROM " + AdminEntry.TABLE_NAME);
        db.execSQL("DELETE FROM " + DoctorEntry.TABLE_NAME);
        db.execSQL("DELETE FROM " + PatientEntry.TABLE_NAME);
        db.execSQL("DELETE FROM " + UserEntry.TABLE_NAME);
        Log.d(TAG, "All data cleared");
    }
}