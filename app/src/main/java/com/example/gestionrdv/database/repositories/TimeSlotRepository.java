package com.example.gestionrdv.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseContract.TimeSlotEntry;
import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.models.TimeSlot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TimeSlotRepository - Handles doctor availability time slots
 */
public class TimeSlotRepository {

    private static final String TAG = "TimeSlotRepository";
    private DatabaseHelper dbHelper;

    public TimeSlotRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Add time slot for a doctor
     */
    public long addTimeSlot(TimeSlot timeSlot) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TimeSlotEntry.COLUMN_DOCTOR_ID, timeSlot.getDoctorId());
        values.put(TimeSlotEntry.COLUMN_DAY_OF_WEEK, timeSlot.getDayOfWeek());
        values.put(TimeSlotEntry.COLUMN_START_TIME, timeSlot.getStartTime());
        values.put(TimeSlotEntry.COLUMN_END_TIME, timeSlot.getEndTime());
        values.put(TimeSlotEntry.COLUMN_IS_AVAILABLE, timeSlot.isAvailable() ? 1 : 0);

        long id = db.insert(TimeSlotEntry.TABLE_NAME, null, values);

        if (id != -1) {
            Log.d(TAG, "Time slot added successfully");
        }

        return id;
    }

    /**
     * Get all time slots for a doctor
     */
    public List<TimeSlot> getDoctorTimeSlots(long doctorId) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = TimeSlotEntry.COLUMN_DOCTOR_ID + " = ?";
        String[] selectionArgs = {String.valueOf(doctorId)};

        Cursor cursor = db.query(
                TimeSlotEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                TimeSlotEntry.COLUMN_DAY_OF_WEEK + " ASC, " +
                        TimeSlotEntry.COLUMN_START_TIME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                timeSlots.add(cursorToTimeSlot(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return timeSlots;
    }

    /**
     * Get time slots for a specific day of week
     */
    public List<TimeSlot> getDoctorTimeSlotsForDay(long doctorId, int dayOfWeek) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = TimeSlotEntry.COLUMN_DOCTOR_ID + " = ? AND " +
                TimeSlotEntry.COLUMN_DAY_OF_WEEK + " = ? AND " +
                TimeSlotEntry.COLUMN_IS_AVAILABLE + " = 1";
        String[] selectionArgs = {String.valueOf(doctorId), String.valueOf(dayOfWeek)};

        Cursor cursor = db.query(
                TimeSlotEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                TimeSlotEntry.COLUMN_START_TIME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                timeSlots.add(cursorToTimeSlot(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return timeSlots;
    }

    /**
     * Generate available time slots for a specific date
     * This breaks down the doctor's schedule into bookable slots (e.g., 30-minute intervals)
     */
    public List<String> getAvailableTimeSlotsForDate(long doctorId, String date, int slotDurationMinutes) {
        List<String> availableSlots = new ArrayList<>();

        // Get day of week from date
        int dayOfWeek = getDayOfWeekFromDate(date);

        // Get doctor's time slots for that day
        List<TimeSlot> timeSlots = getDoctorTimeSlotsForDay(doctorId, dayOfWeek);

        // For each time slot, generate bookable times
        for (TimeSlot slot : timeSlots) {
            List<String> generatedSlots = generateTimeSlots(
                    slot.getStartTime(),
                    slot.getEndTime(),
                    slotDurationMinutes
            );
            availableSlots.addAll(generatedSlots);
        }

        // Remove already booked slots
        availableSlots = filterBookedSlots(doctorId, date, availableSlots);

        return availableSlots;
    }

    /**
     * Update time slot
     */
    public boolean updateTimeSlot(TimeSlot timeSlot) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TimeSlotEntry.COLUMN_DAY_OF_WEEK, timeSlot.getDayOfWeek());
        values.put(TimeSlotEntry.COLUMN_START_TIME, timeSlot.getStartTime());
        values.put(TimeSlotEntry.COLUMN_END_TIME, timeSlot.getEndTime());
        values.put(TimeSlotEntry.COLUMN_IS_AVAILABLE, timeSlot.isAvailable() ? 1 : 0);

        String selection = TimeSlotEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(timeSlot.getId())};

        int rowsAffected = db.update(
                TimeSlotEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return rowsAffected > 0;
    }

    /**
     * Delete time slot
     */
    public boolean deleteTimeSlot(long timeSlotId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = TimeSlotEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(timeSlotId)};

        int rowsDeleted = db.delete(
                TimeSlotEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }

    /**
     * Delete all time slots for a doctor
     */
    public boolean deleteDoctorTimeSlots(long doctorId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = TimeSlotEntry.COLUMN_DOCTOR_ID + " = ?";
        String[] selectionArgs = {String.valueOf(doctorId)};

        int rowsDeleted = db.delete(
                TimeSlotEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }

    /**
     * Helper: Convert cursor to TimeSlot object
     */
    private TimeSlot cursorToTimeSlot(Cursor cursor) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TimeSlotEntry._ID)));
        timeSlot.setDoctorId(cursor.getLong(cursor.getColumnIndexOrThrow(TimeSlotEntry.COLUMN_DOCTOR_ID)));
        timeSlot.setDayOfWeek(cursor.getInt(cursor.getColumnIndexOrThrow(TimeSlotEntry.COLUMN_DAY_OF_WEEK)));
        timeSlot.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(TimeSlotEntry.COLUMN_START_TIME)));
        timeSlot.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(TimeSlotEntry.COLUMN_END_TIME)));
        timeSlot.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(TimeSlotEntry.COLUMN_IS_AVAILABLE)) == 1);
        return timeSlot;
    }

    /**
     * Helper: Get day of week from date string (1=Monday, 7=Sunday)
     */
    private int getDayOfWeekFromDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Convert Calendar.DAY_OF_WEEK (1=Sunday) to our format (1=Monday)
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // If Sunday(1) -> 7, otherwise day-1 (Mon(2)->1, Tue(3)->2, etc.)
            return dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
            return 1; // Default to Monday
        }
    }

    /**
     * Helper: Generate time slots within a range
     */
    private List<String> generateTimeSlots(String startTime, String endTime, int intervalMinutes) {
        List<String> slots = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            while (calendar.getTime().before(end)) {
                slots.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.MINUTE, intervalMinutes);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error generating time slots: " + e.getMessage());
        }

        return slots;
    }

    /**
     * Helper: Filter out already booked slots
     */
    private List<String> filterBookedSlots(long doctorId, String date, List<String> slots) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> availableSlots = new ArrayList<>(slots);

        // Query existing appointments for this doctor and date
        String query = "SELECT appointment_time FROM appointments " +
                "WHERE doctor_id = ? AND appointment_date = ? " +
                "AND status != 'cancelled'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(doctorId), date});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String bookedTime = cursor.getString(0);
                availableSlots.remove(bookedTime);
            }
            cursor.close();
        }

        return availableSlots;
    }
}