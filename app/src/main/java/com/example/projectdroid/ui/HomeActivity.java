package com.example.projectdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectdroid.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnCatalog = findViewById(R.id.btnCatalog);
        Button btnMyLoans = findViewById(R.id.btnMyLoans);
        Button btnFavorites = findViewById(R.id.btnFavorites);

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
    }
}
