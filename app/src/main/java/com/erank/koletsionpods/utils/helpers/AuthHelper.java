package com.erank.koletsionpods.utils.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.activities.LoginSignUpActivity;
import com.erank.koletsionpods.db.UserDataSource;
import com.erank.koletsionpods.db.models.User;
import com.erank.koletsionpods.utils.listeners.OnUserCreatedCallback;
import com.erank.koletsionpods.utils.listeners.OnUserDataLoadedListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

import static com.erank.koletsionpods.activities.LoginSignUpActivity.RC_SIGN_IN;

public class AuthHelper {
    private static AuthHelper instance;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private AuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static AuthHelper getInstance() {
        return instance != null ? instance : (instance = new AuthHelper());
    }

    private void setAuthSignInClient(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent(Context ctx) {
        if (mGoogleSignInClient == null)
            setAuthSignInClient(ctx);
        return mGoogleSignInClient.getSignInIntent();
    }


    public Task<AuthResult> checkResult(int requestCode, Intent data) throws ApiException {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                return mAuth.signInWithCredential(credential);
            }
        }
        return null;
    }

    public void signInUser(String email, String password,
                           OnUserDataLoadedListener onUserLoadedListener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(res -> {
                    UserDataSource.getInstance()
                            .loadUserData(onUserLoadedListener);
                }).addOnFailureListener(onUserLoadedListener::onCancelled);
    }

    public void createUser(String email, String password, String un, OnUserCreatedCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    User user = new User(result.getUser());
                    user.setName(un);
                    UserDataSource.getInstance().saveUser(user)
                            .addOnSuccessListener(aVoid -> callback.onLoaded(user))
                            .addOnFailureListener(callback::onCancelled);
                })
                .addOnFailureListener(callback::onCancelled);
    }

    public void showLoginDialog(Context context) {
        new AlertDialog.Builder(context)
                .setMessage("Must be logged in to do those stuff")
                .setPositiveButton("Nope, guest", null)
                .setNegativeButton("Login", (dialog, which) -> {
                    Intent intent = new Intent(context, LoginSignUpActivity.class);
                    context.startActivity(intent);
                })
                .show();
    }

    public boolean isUserLogged() {
        return isUserLogged(false);
    }

    public boolean isUserLogged(boolean doesAnonymousCount) {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null && (doesAnonymousCount || !user.isAnonymous());
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public Task<AuthResult> signInAnonymously() {
        return mAuth.signInAnonymously()
                .addOnSuccessListener((result) -> UserDataSource.getInstance()
                        .setCurrentUser(new User(result.getUser())));
    }

    public void openLogin(Context context) {
        Intent intent = new Intent(context, LoginSignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public void signOut(Context context) {
        mAuth.signOut();
        openLogin(context);
    }

    public boolean isUserNameValid(String username) {
        final String unameRegex = "^[a-z0-9_-]{3,15}$";
        Pattern pattern = Pattern.compile(unameRegex);
        return pattern.matcher(username).matches();
    }

    public boolean isEmailValid(String email) {
        final String emailRegex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    public boolean isPasswordValid(String password) {
        String passRegex = "^(?=.*[0-9]).{6,15}$";
        Pattern pat = Pattern.compile(passRegex);
        return pat.matcher(password).matches();
    }
}