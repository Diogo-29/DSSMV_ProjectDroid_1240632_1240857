package com.example.projectdroid.repository;

import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.GoogleBooksResponse;
import com.example.projectdroid.network.GoogleBooksApi;
import com.example.projectdroid.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

// Tudo o que tem a ver com ir buscar livros à Google Books
public class BookRepository {

    private final GoogleBooksApi api = RetrofitClient.getGoogleBooksApi();

    public void searchBooks(String query, RepositoryCallback<List<Book>> callback) {
        api.searchBooks(query).enqueue(new ApiCallback<>(new RepositoryCallback<GoogleBooksResponse>() {
            @Override
            public void onSuccess(GoogleBooksResponse response) {
                List<Book> books = new ArrayList<>();
                // Quando não há resultados, a Google não envia a lista "items"
                if (response != null && response.items != null) {
                    for (GoogleBooksResponse.Item item : response.items) {
                        if (item.volumeInfo != null) {
                            books.add(converterParaBook(item));
                        }
                    }
                }
                callback.onSuccess(books);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        }));
    }

    // Converte um resultado da Google Books num Book da nossa app
    private Book converterParaBook(GoogleBooksResponse.Item item) {
        GoogleBooksResponse.VolumeInfo info = item.volumeInfo;

        List<String> authors = info.authors;
        if (authors == null) {
            authors = new ArrayList<>();
            authors.add("Autor Desconhecido");
        }

        // A Google devolve as capas em http; o Android bloqueia http por defeito
        String coverUrl = null;
        if (info.imageLinks != null && info.imageLinks.thumbnail != null) {
            coverUrl = info.imageLinks.thumbnail.replace("http:", "https:");
        }

        return new Book(item.id, info.title, authors, obterIsbn(info), info.description,
                coverUrl, info.pageCount);
    }

    // A Google devolve vários identificadores: usamos o ISBN-13 e, se não houver, o ISBN-10
    private String obterIsbn(GoogleBooksResponse.VolumeInfo info) {
        if (info.industryIdentifiers == null) {
            return null;
        }

        String isbn10 = null;
        for (GoogleBooksResponse.IndustryIdentifier identifier : info.industryIdentifiers) {
            if ("ISBN_13".equals(identifier.type)) {
                return identifier.identifier;
            }
            if ("ISBN_10".equals(identifier.type)) {
                isbn10 = identifier.identifier;
            }
        }
        return isbn10;
    }
}
