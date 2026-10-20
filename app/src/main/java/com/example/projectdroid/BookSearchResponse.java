package com.example.projectdroid;

import java.util.List;

public class BookSearchResponse {
    public String isbn;
    public String title;
    public String description;
    public List<Author> authors;
    public int numberOfPages;
    public String publishDate;
    public Cover cover;
    public int stock;
    public int available;


    public static class Author {
        public String name;
        public List<String> alternateNames;
        public String birthDate;
        public String deathDate;
        public String bio;
        public String id;
    }

    public static class Cover {
        public String largeUrl;
        public String mediumUrl;
        public String smallUrl;
    }
}