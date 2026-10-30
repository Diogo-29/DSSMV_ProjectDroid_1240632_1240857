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

public class SignupActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository authRepository = new AuthRepository();

    private EditText editName;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editConfirmPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        TextView textGoToLogin = findViewById(R.id.textGoToLogin);

        btnSignup.setOnClickListener(v -> registar());

        textGoToLogin.setOnClickListener(v -> finish());
    }

    private void registar() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();

        if (!dadosValidos(name, email, password, confirmPassword)) {
            return;
        }

        btnSignup.setEnabled(false);

        authRepository.register(name, email, password, new RepositoryCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(SignupActivity.this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();

                // Depois de criar a conta, o Firebase já deixa a sessão iniciada
                Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                btnSignup.setEnabled(true);
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean dadosValidos(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            editName.setError("Escreve o teu nome");
            return false;
        }
        if (email.isEmpty()) {
            editEmail.setError("Escreve o teu email");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Email inválido");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            editPassword.setError("A password tem de ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("As passwords não coincidem");
            return false;
        }
        return true;
    }
}
