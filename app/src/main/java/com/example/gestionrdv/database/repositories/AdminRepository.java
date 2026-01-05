package com.example.gestionrdv.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseContract.AdminEntry;
import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.models.Admin;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminRepository - Handles all admin-related database operations
 */
public class AdminRepository {

    private static final String TAG = "AdminRepository";
    private DatabaseHelper dbHelper;

    public AdminRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Add new admin
     */
    public long addAdmin(Admin admin) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AdminEntry.COLUMN_USER_ID, admin.getUserId());
        values.put(AdminEntry.COLUMN_FIRST_NAME, admin.getFirstName());
        values.put(AdminEntry.COLUMN_LAST_NAME, admin.getLastName());
        values.put(AdminEntry.COLUMN_PHONE, admin.getPhone());
        values.put(AdminEntry.COLUMN_ROLE, admin.getRole());

        long adminId = db.insert(AdminEntry.TABLE_NAME, null, values);

        if (adminId != -1) {
            Log.d(TAG, "Admin added successfully with ID: " + adminId);
        } else {
            Log.e(TAG, "Failed to add admin");
        }

        return adminId;
    }

    /**
     * Get admin by ID
     */
    public Admin getAdminById(long adminId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = AdminEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(adminId)};

        Cursor cursor = db.query(
                AdminEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Admin admin = null;
        if (cursor != null && cursor.moveToFirst()) {
            admin = cursorToAdmin(cursor);
            cursor.close();
        }

        return admin;
    }

    /**
     * Get admin by user ID
     */
    public Admin getAdminByUserId(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = AdminEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
                AdminEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Admin admin = null;
        if (cursor != null && cursor.moveToFirst()) {
            admin = cursorToAdmin(cursor);
            cursor.close();
        }

        return admin;
    }

    /**
     * Get all admins
     */
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                AdminEntry.TABLE_NAME,
                null, null, null, null, null,
                AdminEntry.COLUMN_FIRST_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                admins.add(cursorToAdmin(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return admins;
    }

    /**
     * Update admin information
     */
    public boolean updateAdmin(Admin admin) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AdminEntry.COLUMN_FIRST_NAME, admin.getFirstName());
        values.put(AdminEntry.COLUMN_LAST_NAME, admin.getLastName());
        values.put(AdminEntry.COLUMN_PHONE, admin.getPhone());
        values.put(AdminEntry.COLUMN_ROLE, admin.getRole());

        String selection = AdminEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(admin.getId())};

        int rowsAffected = db.update(
                AdminEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return rowsAffected > 0;
    }

    /**
     * Delete admin
     */
    public boolean deleteAdmin(long adminId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = AdminEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(adminId)};

        int rowsDeleted = db.delete(
                AdminEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }

    /**
     * Helper method to convert cursor to Admin object
     */
    private Admin cursorToAdmin(Cursor cursor) {
        Admin admin = new Admin();
        admin.setId(cursor.getLong(cursor.getColumnIndexOrThrow(AdminEntry._ID)));
        admin.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(AdminEntry.COLUMN_USER_ID)));
        admin.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(AdminEntry.COLUMN_FIRST_NAME)));
        admin.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(AdminEntry.COLUMN_LAST_NAME)));
        admin.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(AdminEntry.COLUMN_PHONE)));
        admin.setRole(cursor.getString(cursor.getColumnIndexOrThrow(AdminEntry.COLUMN_ROLE)));
        return admin;
    }
}