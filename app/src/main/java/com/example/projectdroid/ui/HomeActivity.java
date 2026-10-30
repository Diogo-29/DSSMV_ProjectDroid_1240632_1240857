package com.example.projectdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectdroid.R;
import com.example.projectdroid.repository.AuthRepository;
import com.example.projectdroid.session.UserSession;

public class HomeActivity extends AppCompatActivity {

    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Segurança: sem sessão iniciada não se pode estar na Home
        if (UserSession.getUserId() == null) {
            abrirLogin();
            return;
        }

        setContentView(R.layout.activity_home);

        TextView textWelcome = findViewById(R.id.textWelcome);
        Button btnCatalog = findViewById(R.id.btnCatalog);
        Button btnMyLoans = findViewById(R.id.btnMyLoans);
        Button btnFavorites = findViewById(R.id.btnFavorites);
        Button btnDailyBook = findViewById(R.id.btnDailyBook);
        Button btnLogout = findViewById(R.id.btnLogout);

        textWelcome.setText("Olá, " + UserSession.getUserName());

        btnCatalog.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CatalogActivity.class);
            startActivity(intent);
        });

        btnMyLoans.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyLoansActivity.class);
            startActivity(intent);
        });

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // UC8 - Livro do dia (abanar o telemóvel)
        btnDailyBook.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DailyBookActivity.class);
            startActivity(intent);
        });

        // UC11 - Terminar sessão
        btnLogout.setOnClickListener(v -> {
            authRepository.logout();
            abrirLogin();
        });
    }

    // As flags apagam os ecrãs anteriores, para o "voltar" não regressar à Home sem sessão
    private void abrirLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
