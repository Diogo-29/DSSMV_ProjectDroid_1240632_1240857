package com.example.projectdroid.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.GoogleBooksResponse;
import com.example.projectdroid.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchBookActivity extends AppCompatActivity {

    private EditText editSearchQuery;
    private BookAdapter adapter;
    private final List<Book> searchResults = new ArrayList<>();

    private final String GOOGLE_API_KEY = "AIzaSyBdMs4K1TQSDuNCaquNUZpsbTyzrY6-d0Q";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);

        editSearchQuery = findViewById(R.id.editSearchQuery);
        Button btnSearch = findViewById(R.id.btnSearch);
        RecyclerView recyclerSearchResults = findViewById(R.id.recyclerSearchResults);

        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        // reutilizamos o bookAdapter
        adapter = new BookAdapter(searchResults);
        recyclerSearchResults.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String query = editSearchQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                pesquisarNaGoogle(query);
            } else {
                Toast.makeText(SearchBookActivity.this, "Escreve um livro para pesquisar", Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnItemClickListener(book -> {
            android.content.Intent intent = new android.content.Intent(SearchBookActivity.this, BookDetailsActivity.class);

            // Passar os dados para o novo ecrã
            intent.putExtra("BOOK_TITLE", book.getTitle());
            intent.putExtra("BOOK_COVER", book.getCoverUrl());

            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                intent.putExtra("BOOK_AUTHOR", book.getAuthors().get(0));
            }

            startActivity(intent);
        });
    }

    private void pesquisarNaGoogle(String query) {
        RetrofitClient.getGoogleBooksApi().searchBooks(query, GOOGLE_API_KEY).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    searchResults.clear(); // Limpa a pesquisa anterior

                    for (GoogleBooksResponse.Item item : response.body().items) {
                        if (item.volumeInfo != null) {
                            String title = item.volumeInfo.title;

                            List<String> authors = item.volumeInfo.authors;
                            if (authors == null) {
                                authors = new ArrayList<>();
                                authors.add("Autor Desconhecido");
                            }

                            String coverUrl = null;
                            if (item.volumeInfo.imageLinks != null && item.volumeInfo.imageLinks.thumbnail != null) {
                                coverUrl = item.volumeInfo.imageLinks.thumbnail.replace("http:", "https:");
                            }

                            Book book = new Book("google_temp_id", title, authors, "isbn", "desc", coverUrl, 0, 0, 0, false);
                            searchResults.add(book);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SearchBookActivity.this, "Nenhum resultado encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                Toast.makeText(SearchBookActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}