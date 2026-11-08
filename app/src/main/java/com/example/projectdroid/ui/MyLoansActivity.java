package com.example.projectdroid.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectdroid.R;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.repository.LoanRepository;
import com.example.projectdroid.repository.RepositoryCallback;
import com.example.projectdroid.session.UserSession;

import java.util.ArrayList;
import java.util.List;

public class MyLoansActivity extends AppCompatActivity {

    // Cores dos separadores Ativos / Histórico
    private static final int COLOR_SELECTED = Color.parseColor("#6750A4");
    private static final int COLOR_NOT_SELECTED = Color.parseColor("#E0E0E0");
    private static final int COLOR_TEXT_NOT_SELECTED = Color.parseColor("#555555");

    private final LoanRepository loanRepository = new LoanRepository();
    private final List<Loan> loans = new ArrayList<>();
    private LoanAdapter adapter;
    private Button btnActive;
    private Button btnHistory;

    // false = separador "Ativos"; true = separador "Histórico"
    private boolean showingHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_loans);

        btnActive = findViewById(R.id.btnActive);
        btnHistory = findViewById(R.id.btnHistory);
        RecyclerView recyclerLoans = findViewById(R.id.recyclerLoans);

        recyclerLoans.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoanAdapter(loans);
        recyclerLoans.setAdapter(adapter);

        btnActive.setOnClickListener(v -> {
            showingHistory = false;
            carregarEmprestimos();
        });

        btnHistory.setOnClickListener(v -> {
            showingHistory = true;
            carregarEmprestimos();
        });

        adapter.setOnItemClickListener(loan -> mostrarOpcoes(loan));
    }

    // onResume corre sempre que o ecrã volta a ficar visível, por isso a lista está sempre atualizada
    @Override
    protected void onResume() {
        super.onResume();
        carregarEmprestimos();
    }

    private void carregarEmprestimos() {
        atualizarSeparadores();

        String status = showingHistory ? Loan.STATUS_RETURNED : Loan.STATUS_ACTIVE;

        loanRepository.getUserLoans(UserSession.getUserId(), status, new RepositoryCallback<List<Loan>>() {
            @Override
            public void onSuccess(List<Loan> result) {
                loans.clear();
                loans.addAll(result);
                adapter.notifyDataSetChanged();

                if (loans.isEmpty()) {
                    String message = showingHistory
                            ? "Ainda não devolveste nenhum livro."
                            : "Não tens empréstimos ativos.";
                    Toast.makeText(MyLoansActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyLoansActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // O separador que está a ser mostrado fica roxo; o outro fica cinzento
    private void atualizarSeparadores() {
        pintarSeparador(btnActive, !showingHistory);
        pintarSeparador(btnHistory, showingHistory);
    }

    private void pintarSeparador(Button button, boolean selected) {
        int background = selected ? COLOR_SELECTED : COLOR_NOT_SELECTED;
        int text = selected ? Color.WHITE : COLOR_TEXT_NOT_SELECTED;
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setTextColor(text);
    }

    // Só os empréstimos ativos podem ser devolvidos ou renovados
    private void mostrarOpcoes(Loan loan) {
        if (!loan.isActive()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(loan.getTitle())
                .setMessage("Devolver até " + loan.getDueDate())
                .setPositiveButton("Devolver", (dialog, which) -> devolver(loan))
                .setNeutralButton("Renovar", (dialog, which) -> renovar(loan))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void devolver(Loan loan) {
        loanRepository.returnLoan(loan, new RepositoryCallback<Loan>() {
            @Override
            public void onSuccess(Loan result) {
                Toast.makeText(MyLoansActivity.this, "Livro devolvido.", Toast.LENGTH_SHORT).show();
                carregarEmprestimos();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyLoansActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renovar(Loan loan) {
        loanRepository.renewLoan(loan, new RepositoryCallback<Loan>() {
            @Override
            public void onSuccess(Loan result) {
                Toast.makeText(MyLoansActivity.this,
                        "Empréstimo renovado por mais " + LoanRepository.LOAN_DAYS + " dias.", Toast.LENGTH_SHORT).show();
                carregarEmprestimos();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyLoansActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
