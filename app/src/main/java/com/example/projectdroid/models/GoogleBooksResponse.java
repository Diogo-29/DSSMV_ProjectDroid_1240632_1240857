package com.example.projectdroid.models;

import java.util.List;

public class GoogleBooksResponse {
    public List<Item> items;

    public static class Item {
        public VolumeInfo volumeInfo;
    }

    public static class VolumeInfo {
        public String title;
        public List<String> authors;
        public String description;
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