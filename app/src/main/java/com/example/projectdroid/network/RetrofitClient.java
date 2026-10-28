package com.example.projectdroid.network;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // RestDB: base de dados da app (guardar, alterar e apagar dados)
    private static final String RESTDB_BASE_URL = "https://projectdroid-8180.restdb.io/rest/";
    private static final String RESTDB_API_KEY = "27eea8b964c30308f397870d44a0591ae91c5";

    // Google Books: só para ir buscar a informação dos livros
    private static final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/";
    private static final String GOOGLE_BOOKS_API_KEY = "AIzaSyBdMs4K1TQSDuNCaquNUZpsbTyzrY6-d0Q";

    private static Retrofit restDbRetrofit = null;
    private static Retrofit googleBooksRetrofit = null;

    public static RestDbApi getRestDbApi() {
        if (restDbRetrofit == null) {
            // O RestDB pede a API key no header "x-apikey" em todos os pedidos
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("x-apikey", RESTDB_API_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            restDbRetrofit = new Retrofit.Builder()
                    .baseUrl(RESTDB_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return restDbRetrofit.create(RestDbApi.class);
    }

    public static GoogleBooksApi getGoogleBooksApi() {
        if (googleBooksRetrofit == null) {
            // A Google Books pede a API key no URL, no parâmetro "key"
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        HttpUrl url = chain.request().url().newBuilder()
                                .addQueryParameter("key", GOOGLE_BOOKS_API_KEY)
                                .build();
                        Request request = chain.request().newBuilder()
                                .url(url)
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            googleBooksRetrofit = new Retrofit.Builder()
                    .baseUrl(GOOGLE_BOOKS_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return googleBooksRetrofit.create(GoogleBooksApi.class);
    }
}
