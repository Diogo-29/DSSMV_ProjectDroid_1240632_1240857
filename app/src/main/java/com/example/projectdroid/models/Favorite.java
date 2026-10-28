package com.example.projectdroid.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Representa um favorito guardado na coleção "favorites" do RestDB
public class Favorite implements Serializable {

    @SerializedName("_id")
    private String id;

    private String userId;
    private String googleBookId;
    private String title;
    private String authors;
    private String coverUrl;

    public Favorite() {}

    public Favorite(String userId, String googleBookId, String title, String authors, String coverUrl) {
        this.userId = userId;
        this.googleBookId = googleBookId;
        this.title = title;
        this.authors = authors;
        this.coverUrl = coverUrl;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getGoogleBookId() { return googleBookId; }
    public String getTitle() { return title; }
    public String getAuthors() { return authors; }
    public String getCoverUrl() { return coverUrl; }

    // Converte o favorito num Book, para ser mostrado com o BookAdapter e aberto nos detalhes
    public Book toBook() {
        List<String> authorList = new ArrayList<>();
        if (authors != null) {
            authorList.addAll(Arrays.asList(authors.split(", ")));
        }
        return new Book(googleBookId, title, authorList, null, null, coverUrl, null);
    }
}
