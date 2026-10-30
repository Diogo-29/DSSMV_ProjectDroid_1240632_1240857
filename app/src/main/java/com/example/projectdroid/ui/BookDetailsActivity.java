package com.example.projectdroid.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.Favorite;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.repository.BookRepository;
import com.example.projectdroid.repository.FavoriteRepository;
import com.example.projectdroid.repository.LoanRepository;
import com.example.projectdroid.repository.RepositoryCallback;
import com.example.projectdroid.session.UserSession;
import com.example.projectdroid.utils.DateUtils;

public class BookDetailsActivity extends AppCompatActivity {

    // Chave usada para enviar o livro para este ecrã através do Intent
    public static final String EXTRA_BOOK = "BOOK";

    private final BookRepository bookRepository = new BookRepository();
    private final LoanRepository loanRepository = new LoanRepository();
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();

    private Book book;
    private ImageView imageCover;
    private TextView textTitle;
    private TextView textAuthor;
    private TextView textInfo;
    private TextView textDescriptionLabel;
    private TextView textDescription;
    private TextView textAvailability;
    private Button btnRequestLoan;
    private Button btnFavorite;

    // O favorito deste livro, se existir; null se o livro não for favorito
    private Favorite currentFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        imageCover = findViewById(R.id.detailImageCover);
        textTitle = findViewById(R.id.detailTextTitle);
        textAuthor = findViewById(R.id.detailTextAuthor);
        textInfo = findViewById(R.id.detailTextInfo);
        textDescriptionLabel = findViewById(R.id.detailTextDescriptionLabel);
        textDescription = findViewById(R.id.detailTextDescription);
        textAvailability = findViewById(R.id.detailTextAvailability);
        btnRequestLoan = findViewById(R.id.btnRequestLoan);
        btnFavorite = findViewById(R.id.btnFavorite);

        // Receber o livro enviado pelo ecrã anterior
        book = IntentCompat.getSerializableExtra(getIntent(), EXTRA_BOOK, Book.class);
        if (book == null) {
            finish();
            return;
        }

        mostrarLivro();

        // Os favoritos só guardam título, autores e capa: nesse caso pedimos o resto à Google
        if (book.getDescription() == null && book.getNumberOfPages() == null) {
            carregarLivroCompleto();
        }

        btnRequestLoan.setOnClickListener(v -> confirmarRequisicao());
        btnFavorite.setOnClickListener(v -> alternarFavorito());

        verificarDisponibilidade();
        verificarFavorito();
    }

    // ---------- Informação do livro (UC2) ----------

    private void mostrarLivro() {
        textTitle.setText(book.getTitle() != null ? book.getTitle() : "Sem Título");
        textAuthor.setText(book.getAuthorsAsText());

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(this).load(book.getCoverUrl()).into(imageCover);
        }

        // Linha com páginas e ISBN, só com o que existir
        StringBuilder info = new StringBuilder();
        if (book.getNumberOfPages() != null && book.getNumberOfPages() > 0) {
            info.append(book.getNumberOfPages()).append(" páginas");
        }
        if (book.getIsbn() != null) {
            if (info.length() > 0) {
                info.append("  •  ");
            }
            info.append("ISBN ").append(book.getIsbn());
        }
        textInfo.setText(info);
        textInfo.setVisibility(info.length() > 0 ? View.VISIBLE : View.GONE);

        // A Google pode enviar a descrição com HTML (<p>, <b>...), por isso convertemos
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            textDescription.setText(HtmlCompat.fromHtml(book.getDescription(), HtmlCompat.FROM_HTML_MODE_COMPACT));
            textDescriptionLabel.setVisibility(View.VISIBLE);
            textDescription.setVisibility(View.VISIBLE);
        } else {
            textDescriptionLabel.setVisibility(View.GONE);
            textDescription.setVisibility(View.GONE);
        }
    }

    private void carregarLivroCompleto() {
        bookRepository.getBookById(book.getBookId(), new RepositoryCallback<Book>() {
            @Override
            public void onSuccess(Book fullBook) {
                book = fullBook;
                mostrarLivro();
            }

            @Override
            public void onError(String message) {
                // Não é grave: continuamos a mostrar o título, autor e capa que já temos
            }
        });
    }

    // ---------- Empréstimo (UC4) ----------

    // Pergunta ao RestDB quantos exemplares deste livro estão emprestados e mostra os que sobram
    private void verificarDisponibilidade() {
        btnRequestLoan.setEnabled(false);
        textAvailability.setText("A verificar disponibilidade...");

        int totalCopies = LoanRepository.getTotalCopies(book.getBookId());

        loanRepository.getAvailableCopies(book.getBookId(), new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer available) {
                if (available > 0) {
                    textAvailability.setText(available + " de " + totalCopies
                            + (totalCopies == 1 ? " exemplar disponível" : " exemplares disponíveis"));
                    btnRequestLoan.setEnabled(true);
                } else {
                    textAvailability.setText(totalCopies == 1
                            ? "Indisponível (o único exemplar está requisitado)"
                            : "Indisponível (os " + totalCopies + " exemplares estão requisitados)");
                }
            }

            @Override
            public void onError(String message) {
                textAvailability.setText(message);
            }
        });
    }

    // Antes de requisitar, mostra o prazo e pede confirmação (como no diagrama UC4-SSD)
    private void confirmarRequisicao() {
        String dueDate = DateUtils.addDays(DateUtils.today(), LoanRepository.LOAN_DAYS);

        new AlertDialog.Builder(this)
                .setTitle("Confirmar requisição")
                .setMessage("Queres requisitar \"" + book.getTitle() + "\"?\n\n"
                        + "Prazo: " + LoanRepository.LOAN_DAYS + " dias (devolver até " + dueDate + ").")
                .setPositiveButton("Requisitar", (dialog, which) -> requisitar())
                .setNegativeButton("Cancelar", null)
                .show();
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
