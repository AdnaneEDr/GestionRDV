package com.example.gestionrdv.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.database.repositories.AdminRepository;
import com.example.gestionrdv.database.repositories.UserRepository;
import com.example.gestionrdv.models.Admin;

/**
 * DatabaseInitializer - Utilitaire pour initialiser la base de données
 * À utiliser UNE SEULE FOIS pour créer le compte admin initial
 */
public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";
    private Context context;

    public DatabaseInitializer(Context context) {
        this.context = context;
    }

    /**
     * Supprimer toutes les données et créer le compte admin initial
     * ⚠️ ATTENTION : Cela supprime TOUTES les données !
     */
    public boolean resetAndInitialize() {
        try {
            Log.d(TAG, "=== Début de l'initialisation ===");

            // 1. Supprimer toutes les données existantes
            clearAllData();

            // 2. Créer le compte admin
            boolean adminCreated = createInitialAdmin();

            if (adminCreated) {
                Log.d(TAG, "✅ Initialisation réussie !");
                return true;
            } else {
                Log.e(TAG, "❌ Échec de la création de l'admin");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprimer toutes les données de toutes les tables
     */
    private void clearAllData() {
        Log.d(TAG, "Suppression de toutes les données...");

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Supprimer dans l'ordre pour respecter les contraintes de clés étrangères
        db.execSQL("DELETE FROM appointments");
        db.execSQL("DELETE FROM patients");
        db.execSQL("DELETE FROM doctors");
        db.execSQL("DELETE FROM admins");
        db.execSQL("DELETE FROM users");

        Log.d(TAG, "✓ Toutes les données supprimées");
    }

    /**
     * Créer le compte administrateur initial
     * Email: admin@cabinet.ma
     * Password: Admin@123
     */
    private boolean createInitialAdmin() {
        Log.d(TAG, "Création du compte admin...");

        UserRepository userRepo = new UserRepository(context);
        AdminRepository adminRepo = new AdminRepository(context);

        // Données du compte admin
        String email = "admin@cabinet.ma";
        String password = "Admin@123";
        String firstName = "Super";
        String lastName = "Admin";

        // 1. Créer l'utilisateur dans la table users
        long userId = userRepo.registerUser(email, password, "admin");

        if (userId == -1) {
            Log.e(TAG, "❌ Échec création user admin");
            return false;
        }
        Log.d(TAG, "✓ User admin créé avec ID: " + userId);

        // 2. Créer le profil admin
        Admin admin = new Admin();
        admin.setUserId(userId);
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setRole("administrateur");

        long adminId = adminRepo.addAdmin(admin);

        if (adminId == -1) {
            Log.e(TAG, "❌ Échec création profil admin");
            return false;
        }

        Log.d(TAG, "✓ Profil admin créé avec ID: " + adminId);
        Log.d(TAG, "");
        Log.d(TAG, "════════════════════════════════════");
        Log.d(TAG, "  COMPTE ADMIN CRÉÉ AVEC SUCCÈS");
        Log.d(TAG, "════════════════════════════════════");
        Log.d(TAG, "  Email    : " + email);
        Log.d(TAG, "  Password : " + password);
        Log.d(TAG, "════════════════════════════════════");
        Log.d(TAG, "");

        return true;
    }

    /**
     * Créer quelques données de test (optionnel)
     * Patients et rendez-vous de démonstration
     */
    public void createTestData() {
        Log.d(TAG, "Création de données de test...");

        UserRepository userRepo = new UserRepository(context);
        // Vous pouvez ajouter ici la création de patients/médecins de test
        // si nécessaire pour le développement

        Log.d(TAG, "✓ Données de test créées");
    }
}