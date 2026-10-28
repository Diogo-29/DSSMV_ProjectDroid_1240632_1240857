package com.example.projectdroid.repository;

import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.Favorite;
import com.example.projectdroid.network.RestDbApi;
import com.example.projectdroid.network.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.List;

// Tudo o que tem a ver com favoritos (coleção "favorites" do RestDB)
public class FavoriteRepository {

    private final RestDbApi api = RetrofitClient.getRestDbApi();

    // Todos os favoritos de um utilizador (GET)
    public void getFavorites(String userId, RepositoryCallback<List<Favorite>> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("userId", userId);

        api.getFavorites(query.toString()).enqueue(new ApiCallback<>(callback));
    }

    // Procura se um livro já é favorito do utilizador: devolve o Favorite, ou null se não for (GET)
    public void findFavorite(String userId, String googleBookId, RepositoryCallback<Favorite> callback) {
        JsonObject query = new JsonObject();
        query.addProperty("userId", userId);
        query.addProperty("googleBookId", googleBookId);

        api.getFavorites(query.toString()).enqueue(new ApiCallback<>(new RepositoryCallback<List<Favorite>>() {
            @Override
            public void onSuccess(List<Favorite> favorites) {
                callback.onSuccess(favorites.isEmpty() ? null : favorites.get(0));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        }));
    }

    // Adicionar aos favoritos (POST)
    public void addFavorite(Book book, String userId, RepositoryCallback<Favorite> callback) {
        Favorite favorite = new Favorite(
                userId,
                book.getBookId(),
                book.getTitle(),
                book.getAuthorsAsText(),
                book.getCoverUrl()
        );

        api.addFavorite(favorite).enqueue(new ApiCallback<>(callback));
    }

    // Remover dos favoritos (DELETE)
    public void removeFavorite(Favorite favorite, RepositoryCallback<Void> callback) {
        api.deleteFavorite(favorite.getId()).enqueue(new ApiCallback<>(callback));
    }
}
