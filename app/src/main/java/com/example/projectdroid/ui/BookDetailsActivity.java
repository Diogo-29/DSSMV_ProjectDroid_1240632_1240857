package com.example.projectdroid.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectdroid.R;
import com.example.projectdroid.models.BookRequest;
import com.example.projectdroid.models.BookResponse;
import com.example.projectdroid.network.RetrofitClient;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class BookDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        ImageView imageCover = findViewById(R.id.detailImageCover);
        TextView textTitle = findViewById(R.id.detailTextTitle);
        TextView textAuthor = findViewById(R.id.detailTextAuthor);
        Button btnAddToLibrary = findViewById(R.id.btnAddToLibrary);
        Button btnAddToFavorites = findViewById(R.id.btnAddToFavorites);

        // Receber os dados enviados pelo ecrã anterior
        String title = getIntent().getStringExtra("BOOK_TITLE");
        String author = getIntent().getStringExtra("BOOK_AUTHOR");
        String coverUrl = getIntent().getStringExtra("BOOK_COVER");

        // Preencher a interface
        textTitle.setText(title != null ? title : "Sem Título");
        textAuthor.setText(author != null ? author : "Sem Autor");

        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this).load(coverUrl).into(imageCover);
        }

        // Configurar botões (aqui faremos o POST para o RestDB no futuro)
        btnAddToLibrary.setOnClickListener(v -> {
            guardarNoRestDB(title, author, coverUrl, false);
        });

        btnAddToFavorites.setOnClickListener(v -> {
            guardarNoRestDB(title, author, coverUrl, true);
        });
    }
    private void guardarNoRestDB(String title, String author, String coverUrl, boolean isFavorite) {
        // Preparamos o objeto com os dados para enviar para o RestDB
        BookRequest novoLivro = new BookRequest(
                title != null ? title : "Sem Título",
                author != null ? author : "Sem Autor",
                "N/A", // ISBN provisório
                "Adicionado via Google Books",
                coverUrl != null ? coverUrl : "",
                1, // stock inicial
                1, // cópias disponíveis
                isFavorite
        );
        RetrofitClient.getBookApi().addBook(novoLivro).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailsActivity.this, "Livro guardado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a página de detalhes e volta à pesquisa
                } else {
                    Toast.makeText(BookDetailsActivity.this, "Erro ao guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}