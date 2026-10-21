package com.example.projectdroid.network;
import com.example.projectdroid.models.BookResponse;
import com.example.projectdroid.models.BookSearchResponse;
import com.example.projectdroid.models.LoanResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookApi {

    @GET("books")
    Call<List<BookResponse>> getBooks();

    @GET("/v1/search")
    Call<List<BookSearchResponse>> searchBooks(
            @Query("query") String query,
            @Query("page") int page
    );

    @GET("library/{libraryId}/book/{isbn}")
    Call<BookResponse> getBookByIsbn(@Path("libraryId") String libraryId, @Path("isbn") String isbn);

    @POST("library/{libraryId}/book/{bookId}/checkout")
    Call<Void> checkoutBook(@Path("libraryId") String libraryId, @Path("bookId") String bookId, @Query("userId") String userId);

    @POST("library/{libraryId}/book/{isbn}/checkin")
    Call<Void> checkinBook(
            @Path("libraryId") String libraryId,
            @Path("isbn") String isbn,
            @Query("userId") String userId
    );

    @GET("user/checked-out")
    Call<List<LoanResponse>> getCheckedOutBooks(@Query("userId") String userId);

    @GET("user/checkout-history")
    Call<List<LoanResponse>> getCheckoutHistory(@Query("userId") String userId);
}