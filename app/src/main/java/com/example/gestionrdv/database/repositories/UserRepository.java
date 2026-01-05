package com.example.gestionrdv.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseContract.UserEntry;
import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.models.User;

/**
 * UserRepository - Handles all user-related database operations
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private DatabaseHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Register new user
     * @return user ID if successful, -1 if failed
     */
    public long registerUser(String email, String password, String userType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_EMAIL, email);
        values.put(UserEntry.COLUMN_PASSWORD, password); // Hash in production!
        values.put(UserEntry.COLUMN_USER_TYPE, userType);

        long userId = db.insert(UserEntry.TABLE_NAME, null, values);

        if (userId != -1) {
            Log.d(TAG, "User registered successfully with ID: " + userId);
        } else {
            Log.e(TAG, "Failed to register user");
        }

        return userId;
    }

    /**
     * Login user - validates credentials
     * @return User object if successful, null if failed
     */
    public User login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = UserEntry.COLUMN_EMAIL + " = ? AND " +
                UserEntry.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(UserEntry._ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_EMAIL)));
            user.setUserType(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_USER_TYPE)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_CREATED_AT)));

            Log.d(TAG, "Login successful for: " + email);
            cursor.close();
        } else {
            Log.d(TAG, "Login failed for: " + email);
        }

        return user;
    }

    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = UserEntry.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                new String[]{UserEntry._ID},
                selection,
                selectionArgs,
                null, null, null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }

        return exists;
    }

    /**
     * Get user by ID
     */
    public User getUserById(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = UserEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(UserEntry._ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_EMAIL)));
            user.setUserType(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_USER_TYPE)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_CREATED_AT)));
            cursor.close();
        }

        return user;
    }

    /**
     * Update user password
     */
    public boolean updatePassword(long userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_PASSWORD, newPassword); // Hash in production!

        String selection = UserEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        int rowsAffected = db.update(
                UserEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return rowsAffected > 0;
    }

    /**
     * Delete user account
     */
    public boolean deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = UserEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        int rowsDeleted = db.delete(
                UserEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }
}