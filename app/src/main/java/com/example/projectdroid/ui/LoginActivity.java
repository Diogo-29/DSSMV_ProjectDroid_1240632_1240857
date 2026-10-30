package com.example.projectdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectdroid.R;
import com.example.projectdroid.repository.AuthRepository;
import com.example.projectdroid.repository.RepositoryCallback;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private final AuthRepository authRepository = new AuthRepository();

    private EditText editEmail;
    private EditText editPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (authRepository.getCurrentUser() != null) {
            abrirHome();
            return;
        }

        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView textGoToSignup = findViewById(R.id.textGoToSignup);

        btnLogin.setOnClickListener(v -> entrar());

        textGoToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void entrar() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (email.isEmpty()) {
            editEmail.setError("Escreve o teu email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Email inválido");
            return;
        }
        if (password.isEmpty()) {
            editPassword.setError("Escreve a tua password");
            return;
        }

        btnLogin.setEnabled(false);

        authRepository.login(email, password, new RepositoryCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                abrirHome();
            }

            @Override
            public void onError(String message) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
