package com.example.gestionrdv.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionrdv.R;
import com.example.gestionrdv.api.ApiService;
import com.example.gestionrdv.api.RetrofitClient;
import com.example.gestionrdv.model.User;
import com.example.gestionrdv.ui.patient.PatientHomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail, editPassword;
    Button btnLogin;
    TextView txtSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editTextEmail);
        editPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        txtSignUp = findViewById(R.id.textViewSignUp);

        btnLogin.setOnClickListener(v -> loginUser());

        txtSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String pass = editPassword.getText().toString().trim();

        if(email.isEmpty() || pass.isEmpty()) return;

        User loginRequest = new User(email, pass);

        RetrofitClient.getInstance().login(loginRequest).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User loggedInUser = response.body();
                    Toast.makeText(LoginActivity.this, "Welcome " + loggedInUser.getFirstName(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, PatientHomeActivity.class);

                    intent.putExtra("USER_ID", loggedInUser.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}