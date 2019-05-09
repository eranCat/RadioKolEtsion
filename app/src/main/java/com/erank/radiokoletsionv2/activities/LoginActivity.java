package com.erank.radiokoletsionv2.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.account_managers.AccountType;
import com.erank.radiokoletsionv2.fragments.LoginDetailsDialog;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;


public class LoginActivity extends AppCompatActivity {

    public static final String ACTION_LOGIN = "actionLogin";
    public static final int RC_SIGNIN = 31415;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String EMAIL = "email";
    private FirebaseAuth mAuth;

    private BroadcastReceiver loginReceiver;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        View googleLoginBtn = findViewById(R.id.google_login_btn);
        ConstraintLayout fbLoginBtn = findViewById(R.id.fb_login_btn);

        googleLoginBtn.setOnClickListener(v -> showLoginDetailsDialog(AccountType.GOOGLE));
        fbLoginBtn.setOnClickListener(v -> showLoginDetailsDialog(AccountType.FACEBOOK));

        findViewById(R.id.skipText).setOnClickListener(v -> {
            mAuth.signInAnonymously();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        loginReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String error = intent.getStringExtra("error");
                if (error != null) {
                    Snackbar.make(googleLoginBtn, error, Snackbar.LENGTH_LONG);
                } else {
                    Toast.makeText(context, "Logged in successfully", Toast.LENGTH_LONG).show();

                    Intent mainIntent = new Intent(context, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
            }
        };
    }

    private void showLoginDetailsDialog(AccountType type) {
        FragmentManager fm = getSupportFragmentManager();
        LoginDetailsDialog editNameDialogFragment = LoginDetailsDialog.newInstance(type);//consider newInstance
        editNameDialogFragment.show(fm, "fragment_loginDetails");
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(ACTION_LOGIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(loginReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
// RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGNIN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showDialog("sign in canceled");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showDialog("no internet connection");
                    return;
                }

                showDialog("unknown error");
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    private void showDialog(String s) {
        new AlertDialog.Builder(this)
                .setTitle("Login")
                .setMessage(s)
                .show();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.loginLayout),
                                "Authentication Failed.",
                                Snackbar.LENGTH_SHORT)
//                                .setAction("try again", v -> {
//                                    //todo try again
//                                })
                                .show();
                    }
                });
    }

    private void signIn() {
        GoogleSignInApi mGoogleSignInClient = Auth.GoogleSignInApi;
//        Intent signInIntent = mGoogleSignInClient.silentSignIn();
//        startActivityForResult(signInIntent, RC_SIGNIN);
    }

    @Override
    public void onBackPressed() {
        if (mAuth.getCurrentUser() != null) {//connected - probably as guest
            super.onBackPressed();
            return;
        }

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private void setupFacebookLogin() {

        LoginButton fbLoginButton = findViewById(R.id.fb_login_btn);
        fbLoginButton.setReadPermissions(Collections.singletonList(EMAIL));
//        facebook login
        AppEventsLogger.activateApp(getApplication());
        callbackManager = CallbackManager.Factory.create();
        // If you are using in a fragment, call fbLoginButton.setFragment(this);

        // Callback registration
//        fbLoginButton.registerCallback(callbackManager, FacebookAccountManager.getFacebookCallBack(this));
    }

    private void setupGoogleLogin() {
        SignInButton btn = findViewById(R.id.google_login_btn);
//        GoogleAccountManager.init(this);
//        btn.setOnClickListener(v -> GoogleAccountManager.signIn(this));
    }

    /*
    private void initAccountsApi() {
String type = userPrefs.getString(AccountType.getName(), null);
if (type == null) return;

AccountType google = AccountType.GOOGLE;
if (type.equals(google.toString())) {
//            GoogleAccountManager.init(this);
} else {
AccountType facebook = AccountType.FACEBOOK;
if (type.equals(facebook.toString())) {

}
}
}
     */

    //        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.AnonymousBuilder().build(),
//                new AuthUI.IdpConfig.GoogleBuilder().build(),
////                new AuthUI.IdpConfig.EmailBuilder().build(),
//                        new AuthUI.IdpConfig.FacebookBuilder().build()
//        );
//
//        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .setIsSmartLockEnabled(true,true)
//                .build();
//        startActivityForResult(intent, RC_SIGNIN);//TODO fix this UI thing
}
