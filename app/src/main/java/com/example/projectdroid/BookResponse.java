package com.example.projectdroid;
import java.util.List;

public class BookResponse {
    public String isbn;
    public int stock;
    public int available;
    public BookInfo book;

    public static class BookInfo {
        public String id;
        public String title;
        public String description;
        public String isbn;
        public List<Author> authors;
        public Cover cover;
        public int numberOfPages;

        public static class Author {
            public String name;
        }

        public static class Cover {
            public String largeUrl;
            public String mediumUrl;
            public String smallUrl;
        }
    }
}
