package com.example.projectdroid.repository;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public void login(String email, String password, RepositoryCallback<FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(e -> callback.onError(traduzirErro(e)));
    }


    public void register(String name, String email, String password, RepositoryCallback<FirebaseUser> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    user.updateProfile(profile).addOnCompleteListener(task -> callback.onSuccess(user));
                })
                .addOnFailureListener(e -> callback.onError(traduzirErro(e)));
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }


    private String traduzirErro(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "A password tem de ter pelo menos 6 caracteres.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Já existe uma conta com este email.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException
                || e instanceof FirebaseAuthInvalidUserException) {
            return "Email ou password incorretos.";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Sem ligação à internet.";
        }
        return "Erro: " + e.getMessage();
    }
}
