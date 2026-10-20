package com.example.projectdroid;

import java.io.Serializable;
import java.util.List;

public class Book implements Serializable {

    private String bookId;
    private String title;
    private List<String> authors;
    private String isbn;
    private String description;
    private String coverUrl;
    private Integer numberOfPages;
    private int totalCopies;
    private int availableCopies;

    public Book() {}

    public Book(String bookId, String title, List<String> authors, String isbn, String description, String coverUrl,
                Integer numberOfPages, int totalCopies, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.authors = authors;
        this.isbn = isbn;
        this.description = description;
        this.coverUrl = coverUrl;
        this.numberOfPages = numberOfPages;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getBookId() { return bookId; }

    public String getTitle() { return title; }
    public List<String> getAuthors() { return authors; }
    public String getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public Integer getNumberOfPages() { return numberOfPages; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthors(List<String> authors) { this.authors = authors; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setDescription(String description) { this.description = description; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setNumberOfPages(Integer numberOfPages) { this.numberOfPages = numberOfPages; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public boolean isAvailableForLoan() {
        return availableCopies > 0;
    }
}