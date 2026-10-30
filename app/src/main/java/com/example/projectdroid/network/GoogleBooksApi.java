package com.example.projectdroid.network;

import com.example.projectdroid.models.GoogleBooksResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoogleBooksApi {

    // Pesquisa paginada: startIndex = posição do primeiro resultado (0, 20, 40...),
    // maxResults = quantos resultados por página (a Google aceita até 40)
    @GET("volumes")
    Call<GoogleBooksResponse> searchBooks(@Query("q") String query,
                                          @Query("startIndex") int startIndex,
                                          @Query("maxResults") int maxResults);

    // Igual à de cima, mas escolhendo quantos resultados queremos (a Google aceita até 40)
    @GET("volumes")
    Call<GoogleBooksResponse> searchBooks(@Query("q") String query, @Query("maxResults") int maxResults);

    // Um livro pelo seu id da Google (devolve um só Item, não uma lista)
    @GET("volumes/{id}")
    Call<GoogleBooksResponse.Item> getBook(@Path("id") String bookId);
}
