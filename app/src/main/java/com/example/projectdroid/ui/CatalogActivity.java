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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.repository.BookRepository;
import com.example.projectdroid.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CatalogActivity extends AppCompatActivity {

    // Pesquisa feita ao abrir o ecrã, para o catálogo não aparecer vazio
    private static final String DEFAULT_QUERY = "subject:fiction";

    // Pesquisa automática: só a partir de 3 letras e 500 ms depois de parar de escrever
    private static final int MIN_QUERY_LENGTH = 3;
    private static final long SEARCH_DELAY_MS = 500;

    // Paginação: quando faltarem 5 livros para o fim da lista, pede a página seguinte
    private static final int LOAD_MORE_THRESHOLD = 5;

    private final BookRepository bookRepository = new BookRepository();
    private final List<Book> books = new ArrayList<>();
    private BookAdapter adapter;
    private LinearLayoutManager layoutManager;
    private EditText editSearchQuery;
    private ProgressBar progressSearch;
    private ProgressBar progressMore;
    private TextView textEmpty;

    // Debounce: o Handler permite agendar uma tarefa para daqui a X milissegundos.
    // Cada letra escrita cancela a pesquisa agendada e agenda uma nova,
    // por isso só se pesquisa quando o utilizador para de escrever.
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::pesquisarTextoEscrito;

    // Cada pesquisa nova recebe um número. Se chegar a resposta de uma pesquisa antiga
    // (ex.: "har" depois de "harry"), o número já não é o atual e a resposta é ignorada.
    private int lastSearchId = 0;

    // Pesquisa que está a ser mostrada, para não repetir o mesmo pedido à Google
    private String currentQuery = null;

    // Estado da paginação da pesquisa atual
    private int nextStartIndex = 0;     // posição do primeiro livro da próxima página
    private boolean isLoading = false;  // true enquanto espera por uma página (evita pedidos repetidos)
    private boolean hasMore = true;     // false quando a Google devolve uma página vazia
    private final Set<String> loadedIds = new HashSet<>(); // a Google às vezes repete livros entre páginas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        editSearchQuery = findViewById(R.id.editSearchQuery);
        Button btnSearch = findViewById(R.id.btnSearch);
        progressSearch = findViewById(R.id.progressSearch);
        progressMore = findViewById(R.id.progressMore);
        textEmpty = findViewById(R.id.textEmpty);
        RecyclerView recyclerBooks = findViewById(R.id.recyclerBooks);

        // O RecyclerView só cria as vistas dos livros visíveis e reutiliza-as durante o scroll,
        // por isso a lista pode ter centenas de livros sem gastar mais memória
        layoutManager = new LinearLayoutManager(this);
        recyclerBooks.setLayoutManager(layoutManager);
        adapter = new BookAdapter(books);
        recyclerBooks.setAdapter(adapter);

        // Scroll infinito: perto do fim da lista, carrega a página seguinte
        recyclerBooks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) {
                    return; // só interessa quando o utilizador desce
                }
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (lastVisible >= books.size() - LOAD_MORE_THRESHOLD) {
                    carregarPagina();
                }
            }
        });

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

    // Começa uma pesquisa nova: a paginação volta ao início
    private void pesquisar(String query) {
        if (query.equals(currentQuery)) {
            return;
        }
        currentQuery = query;
        lastSearchId++;
        nextStartIndex = 0;
        hasMore = true;
        isLoading = false;

        carregarPagina();
    }

    // Pede à Google a próxima página da pesquisa atual
    private void carregarPagina() {
        if (isLoading || !hasMore || currentQuery == null) {
            return;
        }
        isLoading = true;

        final int searchId = lastSearchId;
        final int startIndex = nextStartIndex;
        final boolean firstPage = (startIndex == 0);

        if (firstPage) {
            progressSearch.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        } else {
            progressMore.setVisibility(View.VISIBLE);
        }

        bookRepository.searchBooks(currentQuery, startIndex, new RepositoryCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                if (searchId != lastSearchId) {
                    return; // resposta de uma pesquisa antiga: ignorar
                }
                isLoading = false;
                progressSearch.setVisibility(View.GONE);
                progressMore.setVisibility(View.GONE);

                if (firstPage) {
                    books.clear();
                    loadedIds.clear();
                    adapter.notifyDataSetChanged();
                }

                // Página vazia: não há mais livros para esta pesquisa
                if (result.isEmpty()) {
                    hasMore = false;
                    textEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
                    return;
                }

                // Acrescenta só os livros novos ao fim da lista
                int oldSize = books.size();
                for (Book book : result) {
                    if (loadedIds.add(book.getBookId())) {
                        books.add(book);
                    }
                }
                int added = books.size() - oldSize;
                // Avisa o adapter só das posições novas, em vez de redesenhar a lista toda
                adapter.notifyItemRangeInserted(oldSize, added);

                nextStartIndex += BookRepository.PAGE_SIZE;

                // Se a página só tinha livros repetidos, pede logo a seguinte
                if (added == 0) {
                    carregarPagina();
                }
            }

            @Override
            public void onError(String message) {
                if (searchId != lastSearchId) {
                    return;
                }
                isLoading = false;
                progressSearch.setVisibility(View.GONE);
                progressMore.setVisibility(View.GONE);

                if (firstPage) {
                    // Permite repetir a mesma pesquisa depois de um erro (ex.: sem internet)
                    currentQuery = null;
                    Toast.makeText(CatalogActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    // Para de pedir páginas, para não mostrar um erro a cada scroll
                    hasMore = false;
                    Toast.makeText(CatalogActivity.this,
                            "Não foi possível carregar mais livros.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
