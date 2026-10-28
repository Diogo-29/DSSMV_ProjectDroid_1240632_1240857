package com.example.projectdroid.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;

import com.bumptech.glide.Glide;
import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.BookRequest;
import com.example.projectdroid.models.BookResponse;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.network.RetrofitClient;
import com.example.projectdroid.repository.LoanRepository;
import com.example.projectdroid.repository.RepositoryCallback;
import com.example.projectdroid.session.UserSession;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailsActivity extends AppCompatActivity {

    // Chave usada para enviar o livro para este ecrã através do Intent
    public static final String EXTRA_BOOK = "BOOK";

    private final LoanRepository loanRepository = new LoanRepository();
    private Book book;
    private TextView textAvailability;
    private Button btnRequestLoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        ImageView imageCover = findViewById(R.id.detailImageCover);
        TextView textTitle = findViewById(R.id.detailTextTitle);
        TextView textAuthor = findViewById(R.id.detailTextAuthor);
        textAvailability = findViewById(R.id.detailTextAvailability);
        btnRequestLoan = findViewById(R.id.btnRequestLoan);
        Button btnAddToFavorites = findViewById(R.id.btnAddToFavorites);

        // Receber o livro enviado pelo ecrã anterior
        book = IntentCompat.getSerializableExtra(getIntent(), EXTRA_BOOK, Book.class);
        if (book == null) {
            finish();
            return;
        }

        textTitle.setText(book.getTitle() != null ? book.getTitle() : "Sem Título");
        textAuthor.setText(book.getAuthorsAsText());

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(this).load(book.getCoverUrl()).into(imageCover);
        }

        btnRequestLoan.setOnClickListener(v -> requisitar());
        btnAddToFavorites.setOnClickListener(v -> guardarFavorito());

        verificarDisponibilidade();
    }

    // Pergunta ao RestDB se já existe um empréstimo ativo deste livro
    private void verificarDisponibilidade() {
        btnRequestLoan.setEnabled(false);
        textAvailability.setText("A verificar disponibilidade...");

        loanRepository.isBookAvailable(book.getBookId(), new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean available) {
                if (available) {
                    textAvailability.setText("Disponível");
                    btnRequestLoan.setEnabled(true);
                } else {
                    textAvailability.setText("Indisponível (já está requisitado)");
                }
            }

            @Override
            public void onError(String message) {
                textAvailability.setText(message);
            }
        });
    }

    private void requisitar() {
        // Desativa o botão para não criar dois empréstimos com dois cliques seguidos
        btnRequestLoan.setEnabled(false);

        loanRepository.requestLoan(book, UserSession.getUserId(), new RepositoryCallback<Loan>() {
            @Override
            public void onSuccess(Loan loan) {
                Toast.makeText(BookDetailsActivity.this,
                        "Livro requisitado! Devolver até " + loan.getDueDate(), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                verificarDisponibilidade();
            }
        });
    }

    // Favoritos: ainda usa a coleção "books" (vai passar para a coleção "favorites")
    private void guardarFavorito() {
        BookRequest favorito = new BookRequest(
                book.getTitle() != null ? book.getTitle() : "Sem Título",
                book.getAuthorsAsText(),
                book.getIsbn(),
                book.getDescription(),
                book.getCoverUrl() != null ? book.getCoverUrl() : "",
                1,
                1,
                true
        );

        RetrofitClient.getRestDbApi().addBook(favorito).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailsActivity.this, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookDetailsActivity.this, "Erro ao guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
