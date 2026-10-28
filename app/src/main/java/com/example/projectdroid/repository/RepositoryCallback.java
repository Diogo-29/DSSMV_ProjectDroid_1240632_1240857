package com.example.projectdroid.repository;

// Forma de o repositório devolver o resultado ao ecrã quando o pedido à API termina
public interface RepositoryCallback<T> {

    void onSuccess(T result);

    void onError(String message);
}
