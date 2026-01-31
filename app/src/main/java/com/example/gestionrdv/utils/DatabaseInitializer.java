package com.example.gestionrdv.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestionrdv.database.DatabaseHelper;
import com.example.gestionrdv.database.repositories.AdminRepository;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.database.repositories.PatientRepository;
import com.example.gestionrdv.database.repositories.UserRepository;
import com.example.gestionrdv.models.Admin;
import com.example.gestionrdv.models.Doctor;
import com.example.gestionrdv.models.Patient;

/**
 * DatabaseInitializer - Utilitaire pour initialiser la base de données
 * Crée 3 comptes de test : Admin, Doctor, Patient
 */
public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";
    private Context context;

    public DatabaseInitializer(Context context) {
        this.context = context;
    }

    /**
     * Supprimer toutes les données et créer les comptes de test
     * ⚠️ ATTENTION : Cela supprime TOUTES les données !
     */
    public boolean resetAndInitialize() {
        try {
            Log.d(TAG, "=== Début de l'initialisation ===");

            // 1. Supprimer toutes les données existantes
            clearAllData();

            // 2. Créer les comptes de test
            boolean adminCreated = createTestAdmin();
            boolean doctorCreated = createTestDoctor();
            boolean patientCreated = createTestPatient();

            if (adminCreated && doctorCreated && patientCreated) {
                Log.d(TAG, "✅ Initialisation réussie !");
                printTestAccounts();
                return true;
            } else {
                Log.e(TAG, "❌ Échec de la création d'un ou plusieurs comptes");
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
     * Créer le compte administrateur de test
     * Email: Admin@cabinet.ma
     * Password: Admin@123
     */
    private boolean createTestAdmin() {
        Log.d(TAG, "Création du compte admin...");

        UserRepository userRepo = new UserRepository(context);
        AdminRepository adminRepo = new AdminRepository(context);

        String email = "Admin@cabinet.ma";
        String password = "Admin@123";
        String firstName = "Super";
        String lastName = "Admin";

        // 1. Créer l'utilisateur
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
        return true;
    }

    /**
     * Créer le compte médecin de test
     * Email: Doctor@cabinet.ma
     * Password: Doctor@123
     */
    private boolean createTestDoctor() {
        Log.d(TAG, "Création du compte doctor...");

        UserRepository userRepo = new UserRepository(context);
        DoctorRepository doctorRepo = new DoctorRepository(context);

        String email = "Doctor@cabinet.ma";
        String password = "Doctor@123";
        String firstName = "Mohamed";
        String lastName = "Bennani";

        // 1. Créer l'utilisateur
        long userId = userRepo.registerUser(email, password, "doctor");
        if (userId == -1) {
            Log.e(TAG, "❌ Échec création user doctor");
            return false;
        }
        Log.d(TAG, "✓ User doctor créé avec ID: " + userId);

        // 2. Créer le profil doctor
        Doctor doctor = new Doctor();
        doctor.setUserId(userId);
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setPhone("0661234567");
        doctor.setSpecialization("Médecine Générale");
        doctor.setQualification("Doctorat en Médecine");
        doctor.setExperience(10);
        doctor.setConsultationFee(200.0);
        doctor.setLocation("Casablanca, Maroc");
        doctor.setRating(4.5);

        long doctorId = doctorRepo.addDoctor(doctor);
        if (doctorId == -1) {
            Log.e(TAG, "❌ Échec création profil doctor");
            return false;
        }

        Log.d(TAG, "✓ Profil doctor créé avec ID: " + doctorId);
        return true;
    }

    /**
     * Créer le compte patient de test
     * Email: Patient@cabinet.ma
     * Password: Patient@123
     */
    private boolean createTestPatient() {
        Log.d(TAG, "Création du compte patient...");

        UserRepository userRepo = new UserRepository(context);
        PatientRepository patientRepo = new PatientRepository(context);

        String email = "Patient@cabinet.ma";
        String password = "Patient@123";
        String firstName = "Fatima";
        String lastName = "Zahra";

        // 1. Créer l'utilisateur
        long userId = userRepo.registerUser(email, password, "patient");
        if (userId == -1) {
            Log.e(TAG, "❌ Échec création user patient");
            return false;
        }
        Log.d(TAG, "✓ User patient créé avec ID: " + userId);

        // 2. Créer le profil patient
        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setPhone("0677889900");
        patient.setBirthDate("1990-05-15");
        patient.setAddress("Rabat, Maroc");
        patient.setBloodGroup("A+");

        long patientId = patientRepo.addPatient(patient);
        if (patientId == -1) {
            Log.e(TAG, "❌ Échec création profil patient");
            return false;
        }

        Log.d(TAG, "✓ Profil patient créé avec ID: " + patientId);
        return true;
    }

    /**
     * Afficher les comptes de test dans les logs
     */
    private void printTestAccounts() {
        Log.d(TAG, "");
        Log.d(TAG, "════════════════════════════════════════════════════");
        Log.d(TAG, "       COMPTES DE TEST CRÉÉS AVEC SUCCÈS");
        Log.d(TAG, "════════════════════════════════════════════════════");
        Log.d(TAG, "");
        Log.d(TAG, "  📧 ADMIN");
        Log.d(TAG, "     Email    : Admin@cabinet.ma");
        Log.d(TAG, "     Password : Admin@123");
        Log.d(TAG, "");
        Log.d(TAG, "  📧 DOCTOR");
        Log.d(TAG, "     Email    : Doctor@cabinet.ma");
        Log.d(TAG, "     Password : Doctor@123");
        Log.d(TAG, "");
        Log.d(TAG, "  📧 PATIENT");
        Log.d(TAG, "     Email    : Patient@cabinet.ma");
        Log.d(TAG, "     Password : Patient@123");
        Log.d(TAG, "");
        Log.d(TAG, "════════════════════════════════════════════════════");
        Log.d(TAG, "");
    }

    /**
     * Créer quelques données de test supplémentaires (optionnel)
     */
    public void createTestData() {
        Log.d(TAG, "Création de données de test supplémentaires...");
        // Ajouter ici d'autres données de test si nécessaire
        Log.d(TAG, "✓ Données de test créées");
    }
}
