package com.example.projectdroid.repository;

import java.io.IOException;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Pedido Retrofit falso para os testes.
// Implementa a interface Call do Retrofit, mas em vez de ir à internet,
// o enqueue() responde logo com o resultado que escolhermos.
class FakeCall<T> implements Call<T> {

    private final T body;
    private final boolean networkError;

    private FakeCall(T body, boolean networkError) {
        this.body = body;
        this.networkError = networkError;
    }

    // Pedido que responde com sucesso (código 200) e o body indicado
    static <T> FakeCall<T> sucesso(T body) {
        return new FakeCall<>(body, false);
    }

    // Pedido que falha por falta de rede
    static <T> FakeCall<T> semRede() {
        return new FakeCall<>(null, true);
    }

    // É este o método que os repositórios chamam
    @Override
    public void enqueue(Callback<T> callback) {
        if (networkError) {
            callback.onFailure(this, new IOException("sem ligação"));
        } else {
            callback.onResponse(this, Response.success(body));
        }
    }

    // Os métodos seguintes são obrigatórios pela interface, mas os testes não os usam

    @Override
    public Response<T> execute() throws IOException {
        if (networkError) {
            throw new IOException("sem ligação");
        }
        return Response.success(body);
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return new FakeCall<>(body, networkError);
    }

    @Override
    public Request request() {
        return new Request.Builder().url("http://localhost/").build();
    }

    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
}
