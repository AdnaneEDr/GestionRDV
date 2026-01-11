package com.example.gestionrdv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView loadingText;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Lie le layout XML fourni

        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingText);

        // Lancer l'animation de chargement
        startLoadingAnimation();
    }

    private void startLoadingAnimation() {
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 5; // Vitesse du chargement

                // Mise à jour de l'interface utilisateur sur le thread principal
                handler.post(() -> {
                    progressBar.setProgress(progressStatus);
                    if (progressStatus == 100) {
                        loadingText.setText("Terminé");
                        navigateToLogin();
                    }
                });

                try {
                    // Délai pour simuler un chargement (ex: 150ms * 20 étapes = 3 secondes)
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //boohoo
            }
        }).start();
    }

    private void navigateToLogin() {
        // Redirection vers LoginActivity située dans le même package
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Empêche de revenir au splash screen avec le bouton 'Retour'
    }
}
