package com.example.projectdroid.models;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

// Representa um livro do catálogo (os livros vêm da Google Books)
public class Book implements Serializable {

    private final String bookId;
    private final String title;
    private final List<String> authors;
    private final String isbn;
    private final String description;
    private final String coverUrl;
    private final Integer numberOfPages;

    public Book(String bookId, String title, List<String> authors, String isbn, String description,
                String coverUrl, Integer numberOfPages) {
        this.bookId = bookId;
        this.title = title;
        this.authors = authors;
        this.isbn = isbn;
        this.description = description;
        this.coverUrl = coverUrl;
        this.numberOfPages = numberOfPages;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public List<String> getAuthors() { return authors; }
    public String getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public Integer getNumberOfPages() { return numberOfPages; }

    // Junta todos os autores num só texto, separados por vírgula
    public String getAuthorsAsText() {
        if (authors == null || authors.isEmpty()) {
            return "Sem Autor";
        }
        return TextUtils.join(", ", authors);
    }
}
