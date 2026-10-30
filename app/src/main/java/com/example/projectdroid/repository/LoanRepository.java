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
    public static final int MAX_COPIES_PER_BOOK = 5;

    private final RestDbApi api = RetrofitClient.getRestDbApi();

    // Número de exemplares que a biblioteca tem de um livro (entre 1 e 5).
    // A Google Books não tem stock, por isso a app simula-o a partir do id do livro:
    // o hashCode() de uma String é sempre o mesmo em qualquer telemóvel,
    // logo o mesmo livro tem sempre o mesmo número de exemplares para todos.
    // (o Math.abs vem depois do %, porque o hashCode pode ser negativo)
    public static int getTotalCopies(String googleBookId) {
        return Math.abs(googleBookId.hashCode() % MAX_COPIES_PER_BOOK) + 1;
    }

    // Exemplares disponíveis = exemplares totais - empréstimos ativos desse livro.
    // Não guardamos nenhum contador: quando alguém requisita, passa a haver mais um
    // empréstimo ATIVO (fica com menos um); quando devolve, deixa de contar (fica com mais um).
    public void getAvailableCopies(String googleBookId, RepositoryCallback<Integer> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("googleBookId", googleBookId);
        query.addProperty("status", Loan.STATUS_ACTIVE);

        api.getLoans(query.toString()).enqueue(new ApiCallback<>(new RepositoryCallback<List<Loan>>() {
            @Override
            public void onSuccess(List<Loan> activeLoans) {
                int available = getTotalCopies(googleBookId) - activeLoans.size();
                callback.onSuccess(Math.max(available, 0));
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
    // 2) verifica as regras: ainda há exemplares, o utilizador não tem já este livro
    //    e ainda não tem 3 empréstimos
    // 3) se estiver tudo bem, cria o empréstimo (POST)
    public void requestLoan(Book book, String userId, RepositoryCallback<Loan> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("status", Loan.STATUS_ACTIVE);

        api.getLoans(query.toString()).enqueue(new ApiCallback<>(new RepositoryCallback<List<Loan>>() {
            @Override
            public void onSuccess(List<Loan> activeLoans) {
                int bookActiveLoans = 0;
                int userActiveLoans = 0;

                for (Loan loan : activeLoans) {
                    boolean sameBook = book.getBookId().equals(loan.getGoogleBookId());
                    boolean sameUser = userId.equals(loan.getUserId());

                    if (sameBook && sameUser) {
                        callback.onError("Já tens este livro requisitado.");
                        return;
                    }
                    if (sameBook) {
                        bookActiveLoans++;
                    }
                    if (sameUser) {
                        userActiveLoans++;
                    }
                }

                if (bookActiveLoans >= getTotalCopies(book.getBookId())) {
                    callback.onError("Não há exemplares disponíveis deste livro.");
                    return;
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
