package com.example.projectdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.repository.BookRepository;
import com.example.projectdroid.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {

    // Pesquisa feita ao abrir o ecrã, para o catálogo não aparecer vazio
    private static final String DEFAULT_QUERY = "subject:fiction";

    private final BookRepository bookRepository = new BookRepository();
    private final List<Book> books = new ArrayList<>();
    private BookAdapter adapter;
    private EditText editSearchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        editSearchQuery = findViewById(R.id.editSearchQuery);
        Button btnSearch = findViewById(R.id.btnSearch);
        RecyclerView recyclerBooks = findViewById(R.id.recyclerBooks);

        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(books);
        recyclerBooks.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String query = editSearchQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                pesquisar(query);
            } else {
                Toast.makeText(this, "Escreve um livro para pesquisar", Toast.LENGTH_SHORT).show();
            }
        });

        // Ao clicar num livro, abre os detalhes e envia o objeto Book inteiro
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(CatalogActivity.this, BookDetailsActivity.class);
            intent.putExtra(BookDetailsActivity.EXTRA_BOOK, book);
            startActivity(intent);
        });

        pesquisar(DEFAULT_QUERY);
    }

    private void pesquisar(String query) {
        bookRepository.searchBooks(query, new RepositoryCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                books.clear();
                books.addAll(result);
                adapter.notifyDataSetChanged();

                if (books.isEmpty()) {
                    Toast.makeText(CatalogActivity.this, "Nenhum resultado encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CatalogActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
