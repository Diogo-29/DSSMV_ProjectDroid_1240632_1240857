package com.example.projectdroid;

public class LoanResponse {
    public String id;
    public boolean active;
    public String bookId;
    public String userId;
    public String dueDate;

    public BookResponse.BookInfo book;
    public int stock = 0;
    public int available = 0;
}