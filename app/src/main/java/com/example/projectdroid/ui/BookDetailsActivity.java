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
import com.example.projectdroid.models.Favorite;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.repository.FavoriteRepository;
import com.example.projectdroid.repository.LoanRepository;
import com.example.projectdroid.repository.RepositoryCallback;
import com.example.projectdroid.session.UserSession;

public class BookDetailsActivity extends AppCompatActivity {

    // Chave usada para enviar o livro para este ecrã através do Intent
    public static final String EXTRA_BOOK = "BOOK";

    private final LoanRepository loanRepository = new LoanRepository();
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();

    private Book book;
    private TextView textAvailability;
    private Button btnRequestLoan;
    private Button btnFavorite;

    // O favorito deste livro, se existir; null se o livro não for favorito
    private Favorite currentFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        ImageView imageCover = findViewById(R.id.detailImageCover);
        TextView textTitle = findViewById(R.id.detailTextTitle);
        TextView textAuthor = findViewById(R.id.detailTextAuthor);
        textAvailability = findViewById(R.id.detailTextAvailability);
        btnRequestLoan = findViewById(R.id.btnRequestLoan);
        btnFavorite = findViewById(R.id.btnFavorite);

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
        btnFavorite.setOnClickListener(v -> alternarFavorito());

        verificarDisponibilidade();
        verificarFavorito();
    }

    // ---------- Empréstimo ----------

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

    // ---------- Favoritos ----------

    // Pergunta ao RestDB se este livro já é favorito, para saber o texto do botão
    private void verificarFavorito() {
        btnFavorite.setEnabled(false);

        favoriteRepository.findFavorite(UserSession.getUserId(), book.getBookId(), new RepositoryCallback<Favorite>() {
            @Override
            public void onSuccess(Favorite favorite) {
                currentFavorite = favorite;
                atualizarBotaoFavorito();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarBotaoFavorito() {
        btnFavorite.setEnabled(true);
        if (currentFavorite == null) {
            btnFavorite.setText("Adicionar aos favoritos");
        } else {
            btnFavorite.setText("Remover dos favoritos");
        }
    }

    // Se não é favorito, adiciona (POST); se já é, remove (DELETE)
    private void alternarFavorito() {
        btnFavorite.setEnabled(false);

        if (currentFavorite == null) {
            favoriteRepository.addFavorite(book, UserSession.getUserId(), new RepositoryCallback<Favorite>() {
                @Override
                public void onSuccess(Favorite favorite) {
                    currentFavorite = favorite;
                    atualizarBotaoFavorito();
                    Toast.makeText(BookDetailsActivity.this, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    atualizarBotaoFavorito();
                    Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            favoriteRepository.removeFavorite(currentFavorite, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    currentFavorite = null;
                    atualizarBotaoFavorito();
                    Toast.makeText(BookDetailsActivity.this, "Removido dos favoritos", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    atualizarBotaoFavorito();
                    Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
