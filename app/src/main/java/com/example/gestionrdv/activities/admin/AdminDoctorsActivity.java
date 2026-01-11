package com.example.gestionrdv.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionrdv.R;
import com.example.gestionrdv.adapters.AdminDoctorAdapter;
import com.example.gestionrdv.database.repositories.DoctorRepository;
import com.example.gestionrdv.database.repositories.UserRepository;
import com.example.gestionrdv.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class AdminDoctorsActivity extends AppCompatActivity implements AdminDoctorAdapter.OnDoctorActionListener {

    private MaterialToolbar toolbar;
    private ImageButton addDoctorButton;
    private RecyclerView doctorsRecycler;
    private AdminDoctorAdapter adapter;

    private DoctorRepository doctorRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctors);

        doctorRepository = new DoctorRepository(this);
        userRepository = new UserRepository(this);

        initViews();
        setupRecyclerView();
        loadDoctors();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        addDoctorButton = findViewById(R.id.addDoctorButton);
        doctorsRecycler = findViewById(R.id.doctorsRecycler);

        toolbar.setNavigationOnClickListener(v -> finish());
        addDoctorButton.setOnClickListener(v -> showAddDoctorDialog());
    }

    private void setupRecyclerView() {
        adapter = new AdminDoctorAdapter(this, doctorRepository);
        doctorsRecycler.setLayoutManager(new LinearLayoutManager(this));
        doctorsRecycler.setAdapter(adapter);
    }

    private void loadDoctors() {
        List<Doctor> doctors = doctorRepository.getAllDoctors();
        adapter.setDoctors(doctors);

        if (doctors.isEmpty()) {
            Toast.makeText(this, "Aucun médecin enregistré", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddDoctorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        builder.setView(dialogView);

        // Get dialog views
        TextInputEditText etEmail = dialogView.findViewById(R.id.etDoctorEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etDoctorPassword);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etDoctorFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etDoctorLastName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etDoctorPhone);
        TextInputEditText etSpecialization = dialogView.findViewById(R.id.etDoctorSpecialization);
        TextInputEditText etQualification = dialogView.findViewById(R.id.etDoctorQualification);
        TextInputEditText etExperience = dialogView.findViewById(R.id.etDoctorExperience);
        TextInputEditText etFee = dialogView.findViewById(R.id.etDoctorFee);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etDoctorLocation);

        builder.setTitle("Ajouter un médecin");
        builder.setPositiveButton("Ajouter", null);
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to prevent auto-dismiss
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String specialization = etSpecialization.getText().toString().trim();
            String qualification = etQualification.getText().toString().trim();
            String experienceStr = etExperience.getText().toString().trim();
            String feeStr = etFee.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            // Validate required fields
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email requis");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Mot de passe requis");
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Minimum 6 caractères");
                return;
            }

            if (TextUtils.isEmpty(firstName)) {
                etFirstName.setError("Prénom requis");
                return;
            }

            if (TextUtils.isEmpty(lastName)) {
                etLastName.setError("Nom requis");
                return;
            }

            if (TextUtils.isEmpty(phone)) {
                etPhone.setError("Téléphone requis");
                return;
            }

            if (TextUtils.isEmpty(specialization)) {
                etSpecialization.setError("Spécialisation requise");
                return;
            }

            // Check if email already exists
            if (userRepository.emailExists(email)) {
                etEmail.setError("Cet email est déjà utilisé");
                return;
            }

            // Parse numeric values
            int experience = 0;
            double fee = 0.0;

            try {
                if (!TextUtils.isEmpty(experienceStr)) {
                    experience = Integer.parseInt(experienceStr);
                }
            } catch (NumberFormatException e) {
                etExperience.setError("Valeur invalide");
                return;
            }

            try {
                if (!TextUtils.isEmpty(feeStr)) {
                    fee = Double.parseDouble(feeStr);
                }
            } catch (NumberFormatException e) {
                etFee.setError("Valeur invalide");
                return;
            }

            // Create user account
            long userId = userRepository.registerUser(email, password, "doctor");

            if (userId == -1) {
                Toast.makeText(this, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create doctor profile
            Doctor doctor = new Doctor();
            doctor.setUserId(userId);
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setPhone(phone);
            doctor.setSpecialization(specialization);
            doctor.setQualification(qualification);
            doctor.setExperience(experience);
            doctor.setConsultationFee(fee);
            doctor.setLocation(location);
            doctor.setRating(5.0); // Default rating

            long doctorId = doctorRepository.addDoctor(doctor);

            if (doctorId == -1) {
                Toast.makeText(this, "Erreur lors de la création du profil médecin", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Médecin ajouté avec succès", Toast.LENGTH_LONG).show();
            dialog.dismiss();
            loadDoctors(); // Refresh list
        });
    }

    private void showEditDoctorDialog(Doctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        builder.setView(dialogView);

        // Get dialog views
        TextInputEditText etEmail = dialogView.findViewById(R.id.etDoctorEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etDoctorPassword);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etDoctorFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etDoctorLastName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etDoctorPhone);
        TextInputEditText etSpecialization = dialogView.findViewById(R.id.etDoctorSpecialization);
        TextInputEditText etQualification = dialogView.findViewById(R.id.etDoctorQualification);
        TextInputEditText etExperience = dialogView.findViewById(R.id.etDoctorExperience);
        TextInputEditText etFee = dialogView.findViewById(R.id.etDoctorFee);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etDoctorLocation);

        // Pre-fill with existing data
        etEmail.setEnabled(false); // Cannot change email
        etPassword.setVisibility(View.GONE); // Don't show password field for edit
        dialogView.findViewById(R.id.passwordInputLayout).setVisibility(View.GONE);

        etFirstName.setText(doctor.getFirstName());
        etLastName.setText(doctor.getLastName());
        etPhone.setText(doctor.getPhone());
        etSpecialization.setText(doctor.getSpecialization());
        etQualification.setText(doctor.getQualification());
        etExperience.setText(String.valueOf(doctor.getExperience()));
        etFee.setText(String.valueOf(doctor.getConsultationFee()));
        etLocation.setText(doctor.getLocation());

        builder.setTitle("Modifier le médecin");
        builder.setPositiveButton("Enregistrer", null);
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String specialization = etSpecialization.getText().toString().trim();
            String qualification = etQualification.getText().toString().trim();
            String experienceStr = etExperience.getText().toString().trim();
            String feeStr = etFee.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            // Validate
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(specialization)) {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            int experience = 0;
            double fee = 0.0;

            try {
                if (!TextUtils.isEmpty(experienceStr)) {
                    experience = Integer.parseInt(experienceStr);
                }
                if (!TextUtils.isEmpty(feeStr)) {
                    fee = Double.parseDouble(feeStr);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valeurs numériques invalides", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update doctor
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setPhone(phone);
            doctor.setSpecialization(specialization);
            doctor.setQualification(qualification);
            doctor.setExperience(experience);
            doctor.setConsultationFee(fee);
            doctor.setLocation(location);

            boolean success = doctorRepository.updateDoctor(doctor);

            if (success) {
                Toast.makeText(this, "Médecin modifié avec succès", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadDoctors();
            } else {
                Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDoctorClick(Doctor doctor) {
        // Show options dialog: Edit or Delete
        new AlertDialog.Builder(this)
                .setTitle(doctor.getFullName())
                .setItems(new String[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditDoctorDialog(doctor);
                    } else {
                        confirmDeleteDoctor(doctor);
                    }
                })
                .show();
    }

    @Override
    public void onStatusChanged(Doctor doctor, boolean isActive) {
        // Handle status change (activate/deactivate doctor)
        Toast.makeText(this, doctor.getFullName() + (isActive ? " activé" : " désactivé"), Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteDoctor(Doctor doctor) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le médecin")
                .setMessage("Êtes-vous sûr de vouloir supprimer " + doctor.getFullName() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    boolean success = doctorRepository.deleteDoctor(doctor.getId());

                    if (success) {
                        // Also delete user account
                        userRepository.deleteUser(doctor.getUserId());

                        Toast.makeText(this, "Médecin supprimé avec succès", Toast.LENGTH_SHORT).show();
                        loadDoctors();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}