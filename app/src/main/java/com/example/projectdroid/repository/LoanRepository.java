package com.example.projectdroid.repository;

import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.network.RestDbApi;
import com.example.projectdroid.network.RetrofitClient;
import com.example.projectdroid.utils.DateUtils;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Tudo o que tem a ver com empréstimos: as regras da biblioteca e os pedidos ao RestDB
public class LoanRepository {

    public static final int LOAN_DAYS = 14;
    public static final int MAX_ACTIVE_LOANS = 3;

    private final RestDbApi api = RetrofitClient.getRestDbApi();

    // Um livro está disponível se não houver nenhum empréstimo ativo dele (cada livro tem 1 cópia)
    public void isBookAvailable(String googleBookId, RepositoryCallback<Boolean> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("googleBookId", googleBookId);
        query.addProperty("status", Loan.STATUS_ACTIVE);

        api.getLoans(query.toString()).enqueue(new Callback<List<Loan>>() {
            @Override
            public void onResponse(Call<List<Loan>> call, Response<List<Loan>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Erro: " + response.code());
                    return;
                }
                callback.onSuccess(response.body().isEmpty());
            }

            @Override
            public void onFailure(Call<List<Loan>> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    // Empréstimos ativos de um utilizador
    public void getActiveLoans(String userId, RepositoryCallback<List<Loan>> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("userId", userId);
        query.addProperty("status", Loan.STATUS_ACTIVE);

        api.getLoans(query.toString()).enqueue(new Callback<List<Loan>>() {
            @Override
            public void onResponse(Call<List<Loan>> call, Response<List<Loan>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Erro: " + response.code());
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<Loan>> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    // Requisitar um livro:
    // 1) vai buscar todos os empréstimos ativos (GET)
    // 2) verifica se o livro está livre e se o utilizador ainda não tem 3 empréstimos
    // 3) se estiver tudo bem, cria o empréstimo (POST)
    public void requestLoan(Book book, String userId, RepositoryCallback<Loan> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("status", Loan.STATUS_ACTIVE);

        api.getLoans(query.toString()).enqueue(new Callback<List<Loan>>() {
            @Override
            public void onResponse(Call<List<Loan>> call, Response<List<Loan>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Erro: " + response.code());
                    return;
                }

                int userActiveLoans = 0;
                for (Loan loan : response.body()) {
                    if (book.getBookId().equals(loan.getGoogleBookId())) {
                        callback.onError("Este livro já está requisitado.");
                        return;
                    }
                    if (userId.equals(loan.getUserId())) {
                        userActiveLoans++;
                    }
                }

                if (userActiveLoans >= MAX_ACTIVE_LOANS) {
                    callback.onError("Já tens " + MAX_ACTIVE_LOANS
                            + " empréstimos ativos. Devolve um livro antes de requisitar outro.");
                    return;
                }

                createLoan(book, userId, callback);
            }

            @Override
            public void onFailure(Call<List<Loan>> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    private void createLoan(Book book, String userId, RepositoryCallback<Loan> callback) {
        Loan newLoan = new Loan(
                userId,
                book.getBookId(),
                book.getTitle(),
                book.getAuthorsAsText(),
                book.getCoverUrl(),
                DateUtils.today(),
                DateUtils.daysFromToday(LOAN_DAYS)
        );

        api.createLoan(newLoan).enqueue(new Callback<Loan>() {
            @Override
            public void onResponse(Call<Loan> call, Response<Loan> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Erro ao requisitar: " + response.code());
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Loan> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }
}
