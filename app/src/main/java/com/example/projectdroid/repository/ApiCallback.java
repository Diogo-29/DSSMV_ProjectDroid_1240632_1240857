package com.example.projectdroid.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Converte a resposta do Retrofit no nosso RepositoryCallback.
// Assim o tratamento de erros (código HTTP e falta de rede) está escrito num só sítio.
public class ApiCallback<T> implements Callback<T> {

    private final RepositoryCallback<T> callback;

    public ApiCallback(RepositoryCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!response.isSuccessful()) {
            callback.onError("Erro: " + response.code());
            return;
        }
        callback.onSuccess(response.body());
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        callback.onError("Erro de rede: " + t.getMessage());
    }
}
