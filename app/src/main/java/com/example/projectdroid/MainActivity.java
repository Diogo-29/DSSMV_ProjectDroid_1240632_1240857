package com.example.projectdroid;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Checkpoint: UC1 - Listar Livros (ligação à API da biblioteca).
 * Ecrã provisório, sem layout XML: mostra os títulos numa lista simples.
 * Será substituído por BookListActivity quando o frontend estiver feito.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LIBRARY_ID = "d15ff0ec-5da0-4254-b579-c175c9679925";
    private static final String IMAGE_BASE_URL = "http://193.136.62.24";

    private final List<Book> bookList = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);
        setContentView(listView);

        fetchBooks();
    }

    private void fetchBooks() {
        RetrofitClient.getBookApi().getBooks(LIBRARY_ID).enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                Log.d("BookList", "Response Code: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MainActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                bookList.clear();
                titles.clear();
                for (BookResponse br : response.body()) {
                    if (br.book == null) continue;

                    List<String> authors = new ArrayList<>();
                    if (br.book.authors != null) {
                        for (BookResponse.BookInfo.Author a : br.book.authors) {
                            authors.add(a.name);
                        }
                    }

                    String coverUrl = null;
                    if (br.book.cover != null && br.book.cover.mediumUrl != null) {
                        String url = br.book.cover.mediumUrl;
                        coverUrl = url.startsWith("/api")
                                ? IMAGE_BASE_URL + url.substring(4)
                                : IMAGE_BASE_URL + url;
                    }

                    Book book = new Book(br.book.isbn, br.book.title, authors, br.isbn,
                            br.book.description, coverUrl, br.book.numberOfPages,
                            br.stock, br.available);
                    bookList.add(book);
                    titles.add(book.getTitle() + " (" + book.getAvailableCopies() + " disponíveis)");
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
}
