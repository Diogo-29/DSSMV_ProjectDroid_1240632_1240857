package com.example.projectdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectdroid.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnAddBook = findViewById(R.id.btnAddBook);
        Button btnMyLibrary = findViewById(R.id.btnMyLibrary);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        btnMyLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchBookActivity.class);
            startActivity(intent);
        });


        btnFavorites.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(HomeActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });
    }
}