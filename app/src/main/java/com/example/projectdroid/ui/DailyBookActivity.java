package com.example.projectdroid.ui;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.repository.BookRepository;
import com.example.projectdroid.repository.RepositoryCallback;
import com.example.projectdroid.utils.ShakeDetector;

// UC8 - Ver o livro do dia: o utilizador abana o telemóvel para o descobrir
public class DailyBookActivity extends AppCompatActivity {

    private final BookRepository bookRepository = new BookRepository();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    private TextView textInstructions;
    private LinearLayout layoutDailyBook;
    private ImageView imageCover;
    private TextView textTitle;
    private TextView textAuthor;

    private Book dailyBook;          // fica preenchido quando a Google responder
    private boolean shaken = false;  // true depois de o utilizador abanar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_book);

        textInstructions = findViewById(R.id.textInstructions);
        layoutDailyBook = findViewById(R.id.layoutDailyBook);
        imageCover = findViewById(R.id.dailyImageCover);
        textTitle = findViewById(R.id.dailyTextTitle);
        textAuthor = findViewById(R.id.dailyTextAuthor);
        Button btnDetails = findViewById(R.id.btnDailyDetails);
        Button btnReveal = findViewById(R.id.btnReveal);

        // Ir buscar o acelerómetro ao sistema
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector(this::aoAbanar);

        // Se o telemóvel não tiver acelerómetro, mostra um botão em alternativa
        if (accelerometer == null) {
            textInstructions.setText("Este telemóvel não tem acelerómetro.");
            btnReveal.setVisibility(View.VISIBLE);
            btnReveal.setOnClickListener(v -> aoAbanar());
        }

        btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(DailyBookActivity.this, BookDetailsActivity.class);
            intent.putExtra(BookDetailsActivity.EXTRA_BOOK, dailyBook);
            startActivity(intent);
        });

        // Pedimos o livro logo ao abrir, para já estar pronto quando o utilizador abanar
        carregarLivroDoDia();
    }

    // O sensor só fica ligado enquanto o ecrã está visível, para não gastar bateria
    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null && !shaken) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }

    private void carregarLivroDoDia() {
        bookRepository.getDailyBook(new RepositoryCallback<Book>() {
            @Override
            public void onSuccess(Book book) {
                dailyBook = book;
                // Se o utilizador abanou antes de a resposta chegar, mostra já
                if (shaken) {
                    mostrarLivro();
                }
            }

            @Override
            public void onError(String message) {
                textInstructions.setText(message);
            }
        });
    }

    private void aoAbanar() {
        if (shaken) {
            return;
        }
        shaken = true;

        // O livro já foi revelado: deixamos de ouvir o sensor
        sensorManager.unregisterListener(shakeDetector);

        // Pequena vibração para o utilizador sentir que o abanão foi detetado
        textInstructions.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        if (dailyBook != null) {
            mostrarLivro();
        } else {
            textInstructions.setText("A carregar o livro do dia...");
        }
    }

    private void mostrarLivro() {
        textInstructions.setVisibility(View.GONE);
        layoutDailyBook.setVisibility(View.VISIBLE);

        textTitle.setText(dailyBook.getTitle() != null ? dailyBook.getTitle() : "Sem Título");
        textAuthor.setText(dailyBook.getAuthorsAsText());

        if (dailyBook.getCoverUrl() != null && !dailyBook.getCoverUrl().isEmpty()) {
            Glide.with(this).load(dailyBook.getCoverUrl()).into(imageCover);
        }
    }
}
