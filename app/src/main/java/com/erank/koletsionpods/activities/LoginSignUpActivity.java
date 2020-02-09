package com.erank.koletsionpods.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.UserDataSource;
import com.erank.koletsionpods.db.models.User;
import com.erank.koletsionpods.utils.ErrorDialog;
import com.erank.koletsionpods.utils.helpers.AuthHelper;
import com.erank.koletsionpods.utils.listeners.OnTextChangedAdapter;
import com.erank.koletsionpods.utils.listeners.OnUserCreatedCallback;
import com.erank.koletsionpods.utils.listeners.OnUserDataLoadedListener;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class LoginSignUpActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RC_SIGN_IN = 345;
    private static final String TAG = LoginSignUpActivity.class.getSimpleName();

    private EditText emailEt, passEt, userNameEt;
    private Button loginBtn;
    private ProgressBar progressBar, usernamePb;

    private boolean isEmailValid;
    private boolean isPasswordValid;
    private boolean isUserNameValid;
    private View signUpBtn;
    private FirebaseAuth mAuth;
    private AuthHelper authHelper;
    private UserDataSource userDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        findViews();

        authHelper = AuthHelper.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userDataSource = UserDataSource.getInstance();

        signUpBtn.setOnClickListener(v -> {
            v.setEnabled(false);
            signUp();
        });
        signUpBtn.setEnabled(false);


        loginBtn.setOnClickListener(v -> {
            v.setEnabled(false);
            signIn();
        });
        loginBtn.setEnabled(false);

        isPasswordValid = false;
        isEmailValid = false;

        emailEt.addTextChangedListener((OnTextChangedAdapter) s -> {
            isEmailValid = isEmailValid();
            updateButtons();
        });

        passEt.addTextChangedListener((OnTextChangedAdapter) s -> {
            isPasswordValid = isPasswordValid();
            updateButtons();
        });

        userNameEt.addTextChangedListener((OnTextChangedAdapter) s -> {
            isUserNameValid = isEmailValid();
            updateButtons();
        });

        passEt.setOnEditorActionListener((v, actionId, event) -> {

            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                case EditorInfo.IME_ACTION_GO:
                case EditorInfo.IME_ACTION_SEND:
                    signIn();
                    return true;
            }

            return false;
        });

        userNameEt.setOnEditorActionListener((v, actionId, event) -> {

            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                case EditorInfo.IME_ACTION_GO:
                case EditorInfo.IME_ACTION_SEND:
                    signUp();
                    return true;
            }

            return false;
        });
    }

    private void updateButtons() {
        boolean enabled = isEmailValid && isPasswordValid;
        loginBtn.setEnabled(enabled);

        if (userNameEt.getVisibility() == View.VISIBLE) {
            signUpBtn.setEnabled(enabled && isUserNameValid);
        } else {
            signUpBtn.setEnabled(enabled);
        }
    }

    private void findViews() {
        findViewById(R.id.loginLayout)
                .setOnClickListener(v -> dismissKeyboard());

        signUpBtn = findViewById(R.id.sign_up_btn);
        emailEt = findViewById(R.id.et_email);
        passEt = findViewById(R.id.etPassword);
        userNameEt = findViewById(R.id.et_username);
        loginBtn = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.login_progressBar);
        usernamePb = findViewById(R.id.pb_username);

        @IdRes int[] btnIds = {R.id.google_login_btn, R.id.skipText};
        for (int id : btnIds) findViewById(id).setOnClickListener(this);
    }

    private void dismissKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login_btn:
                setProgressBarVisible(true);
                startActivityForResult(authHelper.getSignInIntent(this), RC_SIGN_IN);
                break;
            case R.id.skipText:
                signInAnonymous();
                break;
        }
    }

    private void signIn() {
        String email = emailEt.getText().toString();
        String password = passEt.getText().toString();

        setProgressBarVisible(true);
        authHelper.signInUser(email, password, new OnUserDataLoadedListener() {
            @Override
            public void onLoaded(User user) {
                toast("Successfully signed in " + user.getName());
                setProgressBarVisible(false);
                startMainActivity(user.getId());
            }

            @Override
            public void onCancelled(Exception e) {
                setProgressBarVisible(false);
                ErrorDialog.showError(LoginSignUpActivity.this, e);
            }
        });
    }

    private void signInAnonymous() {
        setProgressBarVisible(true);
        mAuth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    startMainActivity(authResult.getUser().getUid());
                    setProgressBarVisible(false);
                })
                .addOnFailureListener(e -> {
                    ErrorDialog.showError(this, e);
                    setProgressBarVisible(false);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        try {
            authHelper.checkResult(requestCode, data)
                    .addOnCompleteListener(task -> setProgressBarVisible(false))
                    .addOnSuccessListener(authResult -> saveUser(authResult.getUser()))
                    .addOnFailureListener((Exception e) -> ErrorDialog.showError(this, e));
        } catch (ApiException e) {
            ErrorDialog.showError(this, getString(R.string.gLoginErr), e);
            setProgressBarVisible(false);
        }
    }

    private void saveUser(FirebaseUser user) {
        // Sign in success, update UI with the signed-in user's information
        UserDataSource.getInstance()
                .saveUser(user)
                .addOnCompleteListener(task -> setProgressBarVisible(false))
                .addOnSuccessListener(aVoid -> {
                    String name = user.getDisplayName();
                    name = name != null && !name.isEmpty() ? " " + name : "";

                    toast(getString(R.string.welcome) + name);
                    startMainActivity(user.getUid());
                })
                .addOnFailureListener(e -> ErrorDialog.showError(this, e));
    }

    @Override
    public void onBackPressed() {
        if (mAuth.getCurrentUser() != null) {
            super.onBackPressed();
            return;
        }

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    private void signUp() {
        String un = userNameEt.getText().toString();
        if (!isUserNameValid(un)) {
            return;
        }

        usernamePb.setVisibility(View.VISIBLE);
        userDataSource.getTakenNamesRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usernamePb.setVisibility(View.INVISIBLE);

                        if (snapshot.hasChild(un)) {
                            userNameEt.setError("Username is already taken, pick another");
                            return;
                        }

                        userNameEt.setError(null);
                        continueSignUp(un);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void continueSignUp(String un) {
        String email = emailEt.getText().toString();
        String password = passEt.getText().toString();

        setProgressBarVisible(true);
        authHelper.createUser(email, password, un, new OnUserCreatedCallback() {
            @Override
            public void onLoaded(User user) {
                startMainActivity(user.getId());
                signUpBtn.setEnabled(true);
                setProgressBarVisible(false);
            }

            @Override
            public void onCancelled(Exception e) {
                ErrorDialog.showError(LoginSignUpActivity.this, e);
                signUpBtn.setEnabled(true);
                setProgressBarVisible(false);
            }
        });
    }

    private boolean isUserNameValid(String un) {
        if (userNameEt.getVisibility() != View.VISIBLE) {
            userNameEt.setVisibility(View.VISIBLE);
            userNameEt.requestFocus();
            return false;
        }
        // TODO: 2020-02-09 show in animation

        if (!authHelper.isUserNameValid(un)) {
            userNameEt.setError("Username must be 3 to 15 characters " +
                    "with any lower case character, digit or _-");
            return false;
        }

        userNameEt.setError(null);
        return true;
    }

    private boolean isEmailValid() {
        String email = emailEt.getText().toString();

        if (!authHelper.isEmailValid(email)) {
            emailEt.setError("Fill right email format");
            return false;
        }

        emailEt.setError(null);
        return true;
    }

    private boolean isPasswordValid() {

        String password = passEt.getText().toString();
        if (!authHelper.isPasswordValid(password)) {
            passEt.setError("must be 6-15 long, contain at least one" +
                    " upper cased letter and at least lower cased,and no spaces");
            return false;
        }

        passEt.setError(null);
        return true;
    }

    void startMainActivity(String uid) {
        PodcastsDataSource.getInstance().setPodcastsEditable(uid);

        setResult(RESULT_OK);
        startActivity(new Intent(this, PodcastsActivity.class));
        finishActivity(RC_SIGN_IN);
        finish();
    }

    private void setProgressBarVisible(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void toast(@StringRes int id) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
