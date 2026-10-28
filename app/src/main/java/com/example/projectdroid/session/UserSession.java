package com.example.projectdroid.session;

// Guarda quem é o utilizador que está a usar a app.
// Enquanto o login não está feito, usamos sempre o mesmo utilizador de teste.
public class UserSession {

    private static String userId = "utilizador-teste";

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String newUserId) {
        userId = newUserId;
    }
}
