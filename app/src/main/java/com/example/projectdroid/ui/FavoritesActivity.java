package com.example.projectdroid.ui;

/*
 * Autor: Tiago Moreira - 1240883
 */

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.BookResponse;
import com.example.projectdroid.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {

    private BookAdapter adapter;
    private final List<Book> favoriteBooksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        RecyclerView recyclerFavorites = findViewById(R.id.recyclerFavorites);
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookAdapter(favoriteBooksList);
        recyclerFavorites.setAdapter(adapter);

        fetchFavoritesFromRestDB();
    }

    private void fetchFavoritesFromRestDB() {
        RetrofitClient.getBookApi().getBooks().enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteBooksList.clear();

                    for (BookResponse res : response.body()) {

                        List<String> authors = new ArrayList<>();
                        authors.add(res.author != null ? res.author : "Sem Autor");

                        // Constrói o livro exatamente como na MainActivity
                        Book book = new Book(
                                res._id,
                                res.title,
                                authors,
                                res.isbn,
                                res.description,
                                res.cover_url,
                                0,
                                res.stock,
                                res.available,
                                res.isFavorite // O novo parâmetro
                        );

                        // Só adiciona à lista se o isFavorite for true
                        if (book.isFavorite()) {
                            favoriteBooksList.add(book);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                Toast.makeText(FavoritesActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}