package com.example.projectdroid.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Representa um empréstimo guardado na coleção "loans" do RestDB
public class Loan implements Serializable {

    public static final String STATUS_ACTIVE = "ATIVO";
    public static final String STATUS_RETURNED = "DEVOLVIDO";

    // O RestDB chama ao id "_id"; aqui no Java chamamos-lhe "id"
    @SerializedName("_id")
    private String id;

    private String userId;
    private String googleBookId;
    private String title;
    private String authors;
    private String coverUrl;
    private String loanDate;
    private String dueDate;
    private String returnDate;
    private String status;

    public Loan() {}

    public Loan(String userId, String googleBookId, String title, String authors, String coverUrl,
                String loanDate, String dueDate) {
        this.userId = userId;
        this.googleBookId = googleBookId;
        this.title = title;
        this.authors = authors;
        this.coverUrl = coverUrl;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = STATUS_ACTIVE;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getGoogleBookId() { return googleBookId; }
    public String getTitle() { return title; }
    public String getAuthors() { return authors; }
    public String getCoverUrl() { return coverUrl; }
    public String getLoanDate() { return loanDate; }
    public String getDueDate() { return dueDate; }
    public String getReturnDate() { return returnDate; }
    public String getStatus() { return status; }

    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }
}
