package com.example.gestionrdv.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Session Manager - Manages user login session
 */
public class SessionManager {

    private static final String PREF_NAME = "HospitalAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_ID = "profileId"; // patient_id or doctor_id
    private static final String KEY_FULL_NAME = "fullName";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(long userId, String userType, String email,
                                   long profileId, String fullName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_EMAIL, email);
        editor.putLong(KEY_PROFILE_ID, profileId);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get user type (patient, doctor, admin)
     */
    public String getUserType() {
        return prefs.getString(KEY_USER_TYPE, null);
    }

    /**
     * Get user ID (from users table)
     */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Get profile ID (patient_id or doctor_id from respective tables)
     */
    public long getProfileId() {
        return prefs.getLong(KEY_PROFILE_ID, -1);
    }

    /**
     * Get user email
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "User");
    }

    /**
     * Check if user is patient
     */
    public boolean isPatient() {
        return "patient".equals(getUserType());
    }

    /**
     * Check if user is doctor
     */
    public boolean isDoctor() {
        return "doctor".equals(getUserType());
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return "admin".equals(getUserType());
    }

    /**
     * Logout user
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}