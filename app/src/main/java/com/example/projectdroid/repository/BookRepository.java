package com.example.projectdroid.repository;

import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.GoogleBooksResponse;
import com.example.projectdroid.network.GoogleBooksApi;
import com.example.projectdroid.network.RetrofitClient;
import com.example.projectdroid.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Tudo o que tem a ver com ir buscar livros à Google Books
public class BookRepository {

    // Temas de onde sai o livro do dia
    private static final String[] DAILY_BOOK_SUBJECTS = {
            "fiction", "fantasy", "mystery", "science fiction", "romance",
            "history", "biography", "adventure", "poetry", "classics"
    };
    private static final int DAILY_BOOK_RESULTS = 20;

    private final GoogleBooksApi api = RetrofitClient.getGoogleBooksApi();

    public void searchBooks(String query, RepositoryCallback<List<Book>> callback) {
        api.searchBooks(query).enqueue(new ApiCallback<>(new RepositoryCallback<GoogleBooksResponse>() {
            @Override
            public void onSuccess(GoogleBooksResponse response) {
                callback.onSuccess(converterLista(response));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        }));
    }

    // Vai buscar um livro completo pelo id da Google.
    // Usado nos detalhes quando o livro vem dos favoritos, que só guardam título, autores e capa.
    public void getBookById(String bookId, RepositoryCallback<Book> callback) {
        api.getBook(bookId).enqueue(new ApiCallback<>(new RepositoryCallback<GoogleBooksResponse.Item>() {
            @Override
            public void onSuccess(GoogleBooksResponse.Item item) {
                if (item == null || item.volumeInfo == null) {
                    callback.onError("Livro não encontrado.");
                    return;
                }
                callback.onSuccess(converterParaBook(item));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        }));
    }

    // Livro do dia (UC8): igual para todos os utilizadores durante o dia todo.
    // A data de hoje (ex.: 20261030) é usada como "semente" do Random.
    // Com a mesma semente, o Random dá sempre os mesmos números, por isso:
    //   - no mesmo dia, todos os telemóveis escolhem o mesmo tema e o mesmo livro;
    //   - no dia seguinte a semente muda e sai outro livro.
    // Não é preciso guardar nada numa base de dados.
    public void getDailyBook(RepositoryCallback<Book> callback) {
        long seed = Long.parseLong(DateUtils.today().replace("-", ""));
        Random random = new Random(seed);

        String subject = DAILY_BOOK_SUBJECTS[random.nextInt(DAILY_BOOK_SUBJECTS.length)];
        int position = random.nextInt(DAILY_BOOK_RESULTS);

        api.searchBooks("subject:" + subject, DAILY_BOOK_RESULTS)
                .enqueue(new ApiCallback<>(new RepositoryCallback<GoogleBooksResponse>() {
                    @Override
                    public void onSuccess(GoogleBooksResponse response) {
                        List<Book> books = converterLista(response);
                        if (books.isEmpty()) {
                            callback.onError("Não foi possível encontrar o livro do dia.");
                            return;
                        }
                        // Se a Google devolver menos livros do que o esperado, o % evita sair da lista
                        callback.onSuccess(books.get(position % books.size()));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                }));
    }

    // Converte a resposta da Google numa lista de Book.
    // Quando não há resultados, a Google não envia a lista "items".
    private List<Book> converterLista(GoogleBooksResponse response) {
        List<Book> books = new ArrayList<>();
        if (response != null && response.items != null) {
            for (GoogleBooksResponse.Item item : response.items) {
                if (item.volumeInfo != null) {
                    books.add(converterParaBook(item));
                }
            }
        }
        return books;
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
