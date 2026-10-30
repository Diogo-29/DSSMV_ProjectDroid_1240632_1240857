package com.example.projectdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    // Pesquisa automática: só a partir de 3 letras e 500 ms depois de parar de escrever
    private static final int MIN_QUERY_LENGTH = 3;
    private static final long SEARCH_DELAY_MS = 500;

    private final BookRepository bookRepository = new BookRepository();
    private final List<Book> books = new ArrayList<>();
    private BookAdapter adapter;
    private EditText editSearchQuery;
    private ProgressBar progressSearch;
    private TextView textEmpty;

    // Debounce: o Handler permite agendar uma tarefa para daqui a X milissegundos.
    // Cada letra escrita cancela a pesquisa agendada e agenda uma nova,
    // por isso só se pesquisa quando o utilizador para de escrever.
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::pesquisarTextoEscrito;

    // Cada pesquisa recebe um número. Se chegar a resposta de uma pesquisa antiga
    // (ex.: "har" depois de "harry"), o número já não é o atual e a resposta é ignorada.
    private int lastSearchId = 0;

    // Última pesquisa feita, para não repetir o mesmo pedido à Google
    private String currentQuery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        editSearchQuery = findViewById(R.id.editSearchQuery);
        Button btnSearch = findViewById(R.id.btnSearch);
        progressSearch = findViewById(R.id.progressSearch);
        textEmpty = findViewById(R.id.textEmpty);
        RecyclerView recyclerBooks = findViewById(R.id.recyclerBooks);

        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(books);
        recyclerBooks.setAdapter(adapter);

        // Pesquisa automática enquanto se escreve
        editSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(searchRunnable);
                handler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
        });

        // O botão e a tecla "pesquisar" do teclado pesquisam logo, sem esperar
        btnSearch.setOnClickListener(v -> pesquisarAgora());
        editSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                pesquisarAgora();
                return true;
            }
            return false;
        });

        // Ao clicar num livro, abre os detalhes e envia o objeto Book inteiro
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(CatalogActivity.this, BookDetailsActivity.class);
            intent.putExtra(BookDetailsActivity.EXTRA_BOOK, book);
            startActivity(intent);
        });

        pesquisar(DEFAULT_QUERY);
    }

    // Se o ecrã fechar, cancela a pesquisa que estava agendada
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(searchRunnable);
    }

    // Chamado pelo debounce: com menos de 3 letras volta à lista inicial
    private void pesquisarTextoEscrito() {
        String query = editSearchQuery.getText().toString().trim();
        if (query.length() >= MIN_QUERY_LENGTH) {
            pesquisar(query);
        } else if (query.isEmpty()) {
            pesquisar(DEFAULT_QUERY);
        }
    }

    // Botão "Procurar" ou tecla do teclado
    private void pesquisarAgora() {
        handler.removeCallbacks(searchRunnable);
        String query = editSearchQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Escreve um livro para pesquisar", Toast.LENGTH_SHORT).show();
            return;
        }
        pesquisar(query);
    }

    private void pesquisar(String query) {
        if (query.equals(currentQuery)) {
            return;
        }
        currentQuery = query;

        int searchId = ++lastSearchId;
        progressSearch.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);

        bookRepository.searchBooks(query, new RepositoryCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                if (searchId != lastSearchId) {
                    return; // resposta de uma pesquisa antiga: ignorar
                }
                progressSearch.setVisibility(View.GONE);

                books.clear();
                books.addAll(result);
                adapter.notifyDataSetChanged();

                // Em vez de um Toast (que apareceria a cada letra), mostramos um texto na lista
                textEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                if (searchId != lastSearchId) {
                    return;
                }
                progressSearch.setVisibility(View.GONE);
                // Permite repetir a mesma pesquisa depois de um erro (ex.: sem internet)
                currentQuery = null;
                Toast.makeText(CatalogActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
