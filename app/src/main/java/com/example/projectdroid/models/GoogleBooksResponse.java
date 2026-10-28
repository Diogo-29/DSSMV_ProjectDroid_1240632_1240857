package com.example.projectdroid.models;

import java.util.List;

public class GoogleBooksResponse {
    public List<Item> items;

    public static class Item {
        public String id;
        public VolumeInfo volumeInfo;
    }

    public static class VolumeInfo {
        public String title;
        public List<String> authors;
        public String description;
        public Integer pageCount;
        public ImageLinks imageLinks;
        public List<IndustryIdentifier> industryIdentifiers;
    }

    public static class ImageLinks {
        public String thumbnail;
    }

    public static class IndustryIdentifier {
        public String type;
        public String identifier;
    }
}
