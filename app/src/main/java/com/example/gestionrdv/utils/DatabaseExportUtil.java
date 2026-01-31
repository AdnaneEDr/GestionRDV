package com.example.gestionrdv.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.gestionrdv.database.DatabaseContract;
import com.example.gestionrdv.database.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Utility class for exporting the database
 * Use this to create a database file that can be:
 * 1. Committed to Git (place in app/src/main/assets/databases/)
 * 2. Shared with collaborators
 * 3. Bundled into the APK
 */
public class DatabaseExportUtil {

    private static final String TAG = "DatabaseExportUtil";

    /**
     * Export the database to the Downloads folder
     * After export, copy the file to: app/src/main/assets/databases/GestionRDV.db
     *
     * @param context Application context
     * @return The path where the database was exported, or null if failed
     */
    public static String exportToDownloads(Context context) {
        try {
            // Get the current database file
            File currentDB = context.getDatabasePath(DatabaseContract.DATABASE_NAME);

            if (!currentDB.exists()) {
                Log.e(TAG, "Database file does not exist");
                return null;
            }

            // Create destination in Downloads folder
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File destinationFile = new File(downloadsDir, DatabaseContract.DATABASE_NAME);

            // Ensure Downloads directory exists
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            // Copy the database
            copyFile(currentDB, destinationFile);

            String exportPath = destinationFile.getAbsolutePath();
            Log.d(TAG, "Database exported to: " + exportPath);

            return exportPath;

        } catch (IOException e) {
            Log.e(TAG, "Error exporting database: " + e.getMessage());
            return null;
        }
    }

    /**
     * Export the database to a specific path
     *
     * @param context Application context
     * @param destinationPath Full path where to save the database
     * @return true if export was successful
     */
    public static boolean exportToPath(Context context, String destinationPath) {
        try {
            File currentDB = context.getDatabasePath(DatabaseContract.DATABASE_NAME);

            if (!currentDB.exists()) {
                Log.e(TAG, "Database file does not exist");
                return false;
            }

            File destinationFile = new File(destinationPath);

            // Ensure destination directory exists
            File destDir = destinationFile.getParentFile();
            if (destDir != null && !destDir.exists()) {
                destDir.mkdirs();
            }

            copyFile(currentDB, destinationFile);

            Log.d(TAG, "Database exported to: " + destinationPath);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error exporting database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the path where the database should be placed in the project
     * for it to be bundled into the APK
     */
    public static String getAssetsDatabasePath() {
        return "app/src/main/assets/databases/" + DatabaseContract.DATABASE_NAME;
    }

    /**
     * Show export instructions to the user
     */
    public static void showExportInstructions(Context context) {
        String message = "To share your database:\n\n" +
                "1. Export will save to Downloads folder\n" +
                "2. Copy the file to your project:\n" +
                "   " + getAssetsDatabasePath() + "\n" +
                "3. Commit and push to Git\n\n" +
                "Your collaborators will get this database when they install the app.";

        Toast.makeText(context, "Database exported! Check Downloads folder.", Toast.LENGTH_LONG).show();
        Log.i(TAG, message);
    }

    /**
     * Export and show toast notification
     */
    public static void exportWithNotification(Context context) {
        String path = exportToDownloads(context);
        if (path != null) {
            Toast.makeText(context,
                    "Database exported to Downloads folder!\nCopy to: " + getAssetsDatabasePath(),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to export database", Toast.LENGTH_SHORT).show();
        }
    }

    private static void copyFile(File source, File destination) throws IOException {
        FileChannel srcChannel = new FileInputStream(source).getChannel();
        FileChannel dstChannel = new FileOutputStream(destination).getChannel();

        try {
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        } finally {
            srcChannel.close();
            dstChannel.close();
        }
    }
}
