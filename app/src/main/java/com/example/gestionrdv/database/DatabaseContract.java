package com.example.gestionrdv.database;

import android.provider.BaseColumns;

/**
 * Database Contract - Defines database schema
 */
public final class DatabaseContract {

    private DatabaseContract() {} // Prevent instantiation

    // Database constants
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GestionRDV.db";

    /**
     * Users table - Base table for all user types
     */
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_USER_TYPE = "user_type"; // "patient", "doctor", "admin"
        public static final String COLUMN_CREATED_AT = "created_at";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL, " +
                        COLUMN_USER_TYPE + " TEXT NOT NULL, " +
                        COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Patients table - Extended information for patients
     */
    public static class PatientEntry implements BaseColumns {
        public static final String TABLE_NAME = "patients";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_BIRTH_DATE = "birth_date";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_BLOOD_GROUP = "blood_group";
        public static final String COLUMN_PROFILE_IMAGE = "profile_image";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID + " INTEGER NOT NULL, " +
                        COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                        COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                        COLUMN_PHONE + " TEXT, " +
                        COLUMN_BIRTH_DATE + " TEXT, " +
                        COLUMN_ADDRESS + " TEXT, " +
                        COLUMN_BLOOD_GROUP + " TEXT, " +
                        COLUMN_PROFILE_IMAGE + " TEXT, " +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                        UserEntry.TABLE_NAME + "(" + UserEntry._ID + ") ON DELETE CASCADE)";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Doctors table - Extended information for doctors
     */
    public static class DoctorEntry implements BaseColumns {
        public static final String TABLE_NAME = "doctors";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_SPECIALIZATION = "specialization";
        public static final String COLUMN_QUALIFICATION = "qualification";
        public static final String COLUMN_EXPERIENCE = "experience";
        public static final String COLUMN_CONSULTATION_FEE = "consultation_fee";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_PROFILE_IMAGE = "profile_image";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID + " INTEGER NOT NULL, " +
                        COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                        COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                        COLUMN_PHONE + " TEXT, " +
                        COLUMN_SPECIALIZATION + " TEXT NOT NULL, " +
                        COLUMN_QUALIFICATION + " TEXT, " +
                        COLUMN_EXPERIENCE + " INTEGER, " +
                        COLUMN_CONSULTATION_FEE + " REAL, " +
                        COLUMN_LOCATION + " TEXT, " +
                        COLUMN_RATING + " REAL DEFAULT 0, " +
                        COLUMN_PROFILE_IMAGE + " TEXT, " +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                        UserEntry.TABLE_NAME + "(" + UserEntry._ID + ") ON DELETE CASCADE)";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Admins table - Admin users
     */
    public static class AdminEntry implements BaseColumns {
        public static final String TABLE_NAME = "admins";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_ROLE = "role"; // "super_admin", "admin"

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID + " INTEGER NOT NULL, " +
                        COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                        COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                        COLUMN_PHONE + " TEXT, " +
                        COLUMN_ROLE + " TEXT DEFAULT 'admin', " +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                        UserEntry.TABLE_NAME + "(" + UserEntry._ID + ") ON DELETE CASCADE)";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Appointments table
     */
    public static class AppointmentEntry implements BaseColumns {
        public static final String TABLE_NAME = "appointments";
        public static final String COLUMN_PATIENT_ID = "patient_id";
        public static final String COLUMN_DOCTOR_ID = "doctor_id";
        public static final String COLUMN_APPOINTMENT_DATE = "appointment_date";
        public static final String COLUMN_APPOINTMENT_TIME = "appointment_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_REASON = "reason";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_STATUS = "status"; // "pending", "confirmed", "completed", "cancelled"
        public static final String COLUMN_CREATED_AT = "created_at";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PATIENT_ID + " INTEGER NOT NULL, " +
                        COLUMN_DOCTOR_ID + " INTEGER NOT NULL, " +
                        COLUMN_APPOINTMENT_DATE + " TEXT NOT NULL, " +
                        COLUMN_APPOINTMENT_TIME + " TEXT NOT NULL, " +
                        COLUMN_END_TIME + " TEXT NOT NULL, " +
                        COLUMN_REASON + " TEXT, " +
                        COLUMN_NOTES + " TEXT, " +
                        COLUMN_STATUS + " TEXT DEFAULT 'pending', " +
                        COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(" + COLUMN_PATIENT_ID + ") REFERENCES " +
                        PatientEntry.TABLE_NAME + "(" + PatientEntry._ID + ") ON DELETE CASCADE, " +
                        "FOREIGN KEY(" + COLUMN_DOCTOR_ID + ") REFERENCES " +
                        DoctorEntry.TABLE_NAME + "(" + DoctorEntry._ID + ") ON DELETE CASCADE)";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Time Slots table - Doctor availability
     */
    public static class TimeSlotEntry implements BaseColumns {
        public static final String TABLE_NAME = "time_slots";
        public static final String COLUMN_DOCTOR_ID = "doctor_id";
        public static final String COLUMN_DAY_OF_WEEK = "day_of_week"; // 1=Monday, 7=Sunday
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_IS_AVAILABLE = "is_available";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DOCTOR_ID + " INTEGER NOT NULL, " +
                        COLUMN_DAY_OF_WEEK + " INTEGER NOT NULL, " +
                        COLUMN_START_TIME + " TEXT NOT NULL, " +
                        COLUMN_END_TIME + " TEXT NOT NULL, " +
                        COLUMN_IS_AVAILABLE + " INTEGER DEFAULT 1, " +
                        "FOREIGN KEY(" + COLUMN_DOCTOR_ID + ") REFERENCES " +
                        DoctorEntry.TABLE_NAME + "(" + DoctorEntry._ID + ") ON DELETE CASCADE)";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}