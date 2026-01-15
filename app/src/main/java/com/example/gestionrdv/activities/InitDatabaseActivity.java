package com.example.gestionrdv.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;
import com.example.gestionrdv.utils.DatabaseInitializer;

/**
 * InitDatabaseActivity - Activité pour initialiser la base de données
 * À utiliser UNE SEULE FOIS au début du projet
 *
 * INSTRUCTIONS:
 * 1. Lancez cette activité
 * 2. Cliquez sur "Initialiser la base"
 * 3. Confirmez
 * 4. Le compte admin sera créé
 * 5. Connectez-vous avec: admin@cabinet.ma / Admin@123
 */
public class InitDatabaseActivity extends AppCompatActivity {

    private Button btnInitialize;
    private TextView tvInfo, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_database);

        btnInitialize = findViewById(R.id.btnInitialize);
        tvInfo = findViewById(R.id.tvInfo);
        tvStatus = findViewById(R.id.tvStatus);

        // Afficher les informations
        tvInfo.setText(
                "Cette action va :\n\n" +
                        "1. ❌ Supprimer TOUTES les données existantes\n" +
                        "2. ✅ Créer le compte administrateur\n\n" +
                        "Identifiants admin :\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "📧 Email: admin@cabinet.ma\n" +
                        "🔑 Password: Admin@123\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "⚠️ Cette action est IRRÉVERSIBLE !"
        );

        btnInitialize.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmation")
                .setMessage("Êtes-vous sûr de vouloir réinitialiser la base de données ?\n\nToutes les données existantes seront supprimées.")
                .setPositiveButton("Oui, continuer", (dialog, which) -> initializeDatabase())
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void initializeDatabase() {
        tvStatus.setText("⏳ Initialisation en cours...");
        btnInitialize.setEnabled(false);

        // Exécuter l'initialisation
        DatabaseInitializer initializer = new DatabaseInitializer(this);
        boolean success = initializer.resetAndInitialize();

        if (success) {
            tvStatus.setText(
                    "✅ SUCCÈS !\n\n" +
                            "La base de données a été initialisée.\n\n" +
                            "Vous pouvez maintenant vous connecter avec :\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📧 admin@cabinet.ma\n" +
                            "🔑 Admin@123\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            );
            Toast.makeText(this, "Base de données initialisée avec succès !", Toast.LENGTH_LONG).show();
        } else {
            tvStatus.setText("❌ ÉCHEC de l'initialisation.\n\nVérifiez les logs.");
            Toast.makeText(this, "Erreur lors de l'initialisation", Toast.LENGTH_SHORT).show();
            btnInitialize.setEnabled(true);
        }
    }
}