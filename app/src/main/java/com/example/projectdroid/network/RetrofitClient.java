package com.example.projectdroid.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://projectdroid-8180.restdb.io/rest/";
    private static final String API_KEY = "27eea8b964c30308f397870d44a0591ae91c5";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("x-apikey", API_KEY)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(newRequest);
                }
            }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static BookApi getBookApi() {
        return getRetrofitInstance().create(BookApi.class);
    }
}
