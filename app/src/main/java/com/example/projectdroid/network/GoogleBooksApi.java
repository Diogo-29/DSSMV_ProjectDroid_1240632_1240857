package com.example.projectdroid.network;

import com.example.projectdroid.models.GoogleBooksResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksApi {

    @GET("volumes")
    Call<GoogleBooksResponse> searchBooks(@Query("q") String query);
}
