package com.example.projectdroid.models;

public class BookRequest {
    public String title;
    public String author;
    public String isbn;
    public String description;
    public String cover_url;
    public int stock;
    public int available;
    public boolean isFavorite;

    public BookRequest(String title, String author, String isbn, String description, String cover_url, int stock, int available, boolean isFavorite) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.cover_url = cover_url;
        this.stock = stock;
        this.available = available;
        this.isFavorite = isFavorite;
    }
}