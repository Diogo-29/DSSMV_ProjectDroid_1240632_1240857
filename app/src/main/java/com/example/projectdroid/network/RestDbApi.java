package com.example.projectdroid.network;

import com.example.projectdroid.models.Favorite;
import com.example.projectdroid.models.Loan;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Pedidos ao RestDB. O "q" é um filtro em JSON, por exemplo {"status":"ATIVO"}
public interface RestDbApi {

    // Coleção "loans"
    @GET("loans")
    Call<List<Loan>> getLoans(@Query("q") String query);

    @POST("loans")
    Call<Loan> createLoan(@Body Loan loan);

    // PATCH altera só os campos enviados; os outros ficam como estavam
    @PATCH("loans/{id}")
    Call<Loan> updateLoan(@Path("id") String id, @Body Loan changes);

    // Coleção "favorites"
    @GET("favorites")
    Call<List<Favorite>> getFavorites(@Query("q") String query);

    @POST("favorites")
    Call<Favorite> addFavorite(@Body Favorite favorite);

    @DELETE("favorites/{id}")
    Call<Void> deleteFavorite(@Path("id") String id);
}
