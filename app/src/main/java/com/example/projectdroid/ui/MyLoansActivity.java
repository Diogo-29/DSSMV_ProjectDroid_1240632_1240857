package com.example.projectdroid.ui;

import android.os.Bundle;
import android.widget.Toast;

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

    private final LoanRepository loanRepository = new LoanRepository();
    private final List<Loan> loans = new ArrayList<>();
    private LoanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_loans);

        RecyclerView recyclerLoans = findViewById(R.id.recyclerLoans);
        recyclerLoans.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoanAdapter(loans);
        recyclerLoans.setAdapter(adapter);
    }

    // onResume corre sempre que o ecrã volta a ficar visível, por isso a lista está sempre atualizada
    @Override
    protected void onResume() {
        super.onResume();
        carregarEmprestimos();
    }

    private void carregarEmprestimos() {
        loanRepository.getActiveLoans(UserSession.getUserId(), new RepositoryCallback<List<Loan>>() {
            @Override
            public void onSuccess(List<Loan> result) {
                loans.clear();
                loans.addAll(result);
                adapter.notifyDataSetChanged();

                if (loans.isEmpty()) {
                    Toast.makeText(MyLoansActivity.this, "Ainda não tens empréstimos ativos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyLoansActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
