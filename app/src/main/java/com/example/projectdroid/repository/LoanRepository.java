package com.example.projectdroid.repository;

import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.network.RestDbApi;
import com.example.projectdroid.network.RetrofitClient;
import com.example.projectdroid.utils.DateUtils;
import com.google.gson.JsonObject;

import java.util.List;

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

        api.getLoans(query.toString()).enqueue(new ApiCallback<>(new RepositoryCallback<List<Loan>>() {
            @Override
            public void onSuccess(List<Loan> loans) {
                callback.onSuccess(loans.isEmpty());
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        }));
    }

    // Empréstimos de um utilizador com um estado: ATIVO (atuais) ou DEVOLVIDO (histórico)
    public void getUserLoans(String userId, String status, RepositoryCallback<List<Loan>> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("userId", userId);
        query.addProperty("status", status);

        api.getLoans(query.toString()).enqueue(new ApiCallback<>(callback));
    }

    // Requisitar um livro:
    // 1) vai buscar todos os empréstimos ativos (GET)
    // 2) verifica se o livro está livre e se o utilizador ainda não tem 3 empréstimos
    // 3) se estiver tudo bem, cria o empréstimo (POST)
    public void requestLoan(Book book, String userId, RepositoryCallback<Loan> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("status", Loan.STATUS_ACTIVE);

        api.getLoans(query.toString()).enqueue(new ApiCallback<>(new RepositoryCallback<List<Loan>>() {
            @Override
            public void onSuccess(List<Loan> activeLoans) {
                int userActiveLoans = 0;
                for (Loan loan : activeLoans) {
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
            public void onError(String message) {
                callback.onError(message);
            }
        }));
    }

    private void createLoan(Book book, String userId, RepositoryCallback<Loan> callback) {
        String today = DateUtils.today();

        Loan newLoan = new Loan(
                userId,
                book.getBookId(),
                book.getTitle(),
                book.getAuthorsAsText(),
                book.getCoverUrl(),
                today,
                DateUtils.addDays(today, LOAN_DAYS)
        );

        api.createLoan(newLoan).enqueue(new ApiCallback<>(callback));
    }

    // Devolver: o empréstimo não é apagado; passa a DEVOLVIDO e fica no histórico (PATCH)
    public void returnLoan(Loan loan, RepositoryCallback<Loan> callback) {
        Loan changes = new Loan();
        changes.setStatus(Loan.STATUS_RETURNED);
        changes.setReturnDate(DateUtils.today());

        api.updateLoan(loan.getId(), changes).enqueue(new ApiCallback<>(callback));
    }

    // Renovar: mais 14 dias a contar da data limite atual (PATCH).
    // Um empréstimo em atraso não pode ser renovado, tem de ser devolvido.
    public void renewLoan(Loan loan, RepositoryCallback<Loan> callback) {
        if (DateUtils.isBeforeToday(loan.getDueDate())) {
            callback.onError("Este empréstimo está em atraso. Tens de devolver o livro.");
            return;
        }

        Loan changes = new Loan();
        changes.setDueDate(DateUtils.addDays(loan.getDueDate(), LOAN_DAYS));

        api.updateLoan(loan.getId(), changes).enqueue(new ApiCallback<>(callback));
    }
}
