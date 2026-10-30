package com.example.projectdroid.repository;

// Guarda o que o repositório devolveu, para depois o teste verificar
class TestCallback<T> implements RepositoryCallback<T> {

    T result;
    String error;
    boolean successCalled = false;

    @Override
    public void onSuccess(T result) {
        this.successCalled = true;
        this.result = result;
    }

    @Override
    public void onError(String message) {
        this.error = message;
    }
}
