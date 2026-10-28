package com.example.projectdroid.network;

import com.example.projectdroid.models.BookRequest;
import com.example.projectdroid.models.BookResponse;
import com.example.projectdroid.models.Loan;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

// Pedidos ao RestDB. O "q" é um filtro em JSON, por exemplo {"status":"ATIVO"}
public interface RestDbApi {

    // Coleção "books" (ainda usada pelos favoritos; vai ser substituída)
    @GET("books")
    Call<List<BookResponse>> getBooks();

    @POST("books")
    Call<BookResponse> addBook(@Body BookRequest newBook);

    // Coleção "loans"
    @GET("loans")
    Call<List<Loan>> getLoans(@Query("q") String query);

    @POST("loans")
    Call<Loan> createLoan(@Body Loan loan);
}
