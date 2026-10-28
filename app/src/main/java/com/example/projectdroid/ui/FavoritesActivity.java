package com.example.projectdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectdroid.R;
import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.Favorite;
import com.example.projectdroid.repository.FavoriteRepository;
import com.example.projectdroid.repository.RepositoryCallback;
import com.example.projectdroid.session.UserSession;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private final FavoriteRepository favoriteRepository = new FavoriteRepository();
    private final List<Book> favoriteBooks = new ArrayList<>();
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        RecyclerView recyclerFavorites = findViewById(R.id.recyclerFavorites);
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));

        // Os favoritos são mostrados como livros, por isso reutilizamos o BookAdapter
        adapter = new BookAdapter(favoriteBooks);
        recyclerFavorites.setAdapter(adapter);

        // Ao clicar, abre os detalhes, onde se pode requisitar ou remover dos favoritos
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(FavoritesActivity.this, BookDetailsActivity.class);
            intent.putExtra(BookDetailsActivity.EXTRA_BOOK, book);
            startActivity(intent);
        });
    }

    // Recarrega ao voltar dos detalhes, caso o livro tenha sido removido dos favoritos
    @Override
    protected void onResume() {
        super.onResume();
        carregarFavoritos();
    }

    private void carregarFavoritos() {
        favoriteRepository.getFavorites(UserSession.getUserId(), new RepositoryCallback<List<Favorite>>() {
            @Override
            public void onSuccess(List<Favorite> favorites) {
                favoriteBooks.clear();
                for (Favorite favorite : favorites) {
                    favoriteBooks.add(favorite.toBook());
                }
                adapter.notifyDataSetChanged();

                if (favoriteBooks.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, "Ainda não tens favoritos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FavoritesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
