package com.example.projectdroid.session;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Diz quem é o utilizador que está a usar a app, a partir da sessão do Firebase.
// O userId é o "uid" do Firebase: é ele que liga os empréstimos e favoritos (RestDB) ao utilizador.
public class UserSession {

    private static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    // null se não houver sessão iniciada
    public static String getUserId() {
        FirebaseUser user = getUser();
        return user != null ? user.getUid() : null;
    }

    public static String getUserEmail() {
        FirebaseUser user = getUser();
        return user != null ? user.getEmail() : null;
    }

    // Se a conta não tiver nome guardado, mostra o email
    public static String getUserName() {
        FirebaseUser user = getUser();
        if (user == null) {
            return null;
        }
        String name = user.getDisplayName();
        return (name != null && !name.isEmpty()) ? name : user.getEmail();
    }
}
