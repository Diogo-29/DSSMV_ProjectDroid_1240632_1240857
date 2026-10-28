package com.example.projectdroid.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.BookResponse;
import com.example.projectdroid.models.GoogleBooksResponse;
import com.example.projectdroid.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String IMAGE_BASE_URL = "http://193.136.62.24";

    private final List<Book> bookList = new ArrayList<>();
    private BookAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookAdapter(bookList);
        recyclerView.setAdapter(adapter);

        //preencherBaseDeDados();

        fetchBooks();
        testarPesquisaGoogle();
    }

    private void fetchBooks() {

        RetrofitClient.getBookApi().getBooks().enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                Log.d("BookList", "Response Code: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MainActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                bookList.clear();


                for (BookResponse br : response.body()) {

                    List<String> authors = new ArrayList<>();
                    authors.add(br.author != null ? br.author : "Sem Autor");


                    Book book = new Book(
                            br._id,
                            br.title,
                            authors,
                            br.isbn,
                            br.description,
                            br.cover_url,
                            0,
                            br.stock,
                            br.available,
                            br.isFavorite
                    );

                    bookList.add(book);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                Log.e("BookList", "Erro: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Falha de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void testarPesquisaGoogle() {

        String googleApiKey = "AIzaSyBdMs4K1TQSDuNCaquNUZpsbTyzrY6-d0Q";


        RetrofitClient.getGoogleBooksApi().searchBooks("Harry Potter", googleApiKey).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null && !response.body().items.isEmpty()) {

                    String tituloEncontrado = response.body().items.get(0).volumeInfo.title;
                    Log.d("PESQUISA", "✅ Encontrado com sucesso: " + tituloEncontrado);
                } else {
                    Log.d("PESQUISA", "Sem resultados ou erro: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                Log.e("PESQUISA", "Erro de rede: " + t.getMessage());
            }
        });
    }
}