package com.erank.koletsionpods.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.models.User;
import com.erank.koletsionpods.utils.ErrorDialog;
import com.erank.koletsionpods.utils.listeners.OnTextChangedAdapter;
import com.erank.koletsionpods.utils.listeners.OnUserCreatedCallback;
import com.erank.koletsionpods.utils.listeners.OnUserDataLoadedListener;
import com.erank.koletsionpods.viewmodels.LoginSignUpViewModel;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;


public class LoginSignUpActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RC_SIGN_IN = 345;
    private static final String TAG = LoginSignUpActivity.class.getSimpleName();

    private EditText emailEt, passEt;
    private Button loginBtn;
    private ProgressBar progressBar;

    private boolean isEmailValid;
    private boolean isPasswordValid;
    private View signUpBtn;
    private FirebaseAuth mAuth;

    private LoginSignUpViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        findViews();

        viewModel = LoginSignUpViewModel.newInstance(this);

        mAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(v -> createUser());
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
            updateLoginBtn();
        });

        passEt.addTextChangedListener((OnTextChangedAdapter) s -> {
            isPasswordValid = isPasswordValid();
            updateLoginBtn();
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
    }

    private void findViews() {
        signUpBtn = findViewById(R.id.sign_up_btn);
        emailEt = findViewById(R.id.et_email);
        passEt = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.login_progressBar);

        @IdRes int[] btnIds = {R.id.google_login_btn, R.id.skipText, R.id.fb_login_btn};
        for (int id : btnIds) findViewById(id).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login_btn:
                setProgressBarVisible(true);
                startActivityForResult(viewModel.getSignInActivity(), RC_SIGN_IN);
                break;
            case R.id.skipText:
                signInAnonymous();
                break;
            case R.id.fb_login_btn:
                break;
        }
    }

    private void signIn() {
        String email = emailEt.getText().toString();
        String password = passEt.getText().toString();

        setProgressBarVisible(true);
        viewModel.SignInUser(email, password, new OnUserDataLoadedListener() {
            @Override
            public void onLoaded(User user) {
                toast("Successfully signed in " + user.getName());
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
            viewModel.checkResult(requestCode, data)
                    .addOnSuccessListener(authResult -> saveUser(authResult.getUser()))
                    .addOnFailureListener((Exception e) -> {
                        ErrorDialog.showError(this, e);
                        setProgressBarVisible(false);
                    });
        } catch (ApiException e) {
            ErrorDialog.showError(this, getString(R.string.gLoginErr), e);
        }
    }

    private void saveUser(FirebaseUser user) {
        // Sign in success, update UI with the signed-in user's information
        viewModel.createUser(user)
                .addOnSuccessListener(aVoid -> {
                    toast(R.string.welcome);
                    startMainActivity(user.getUid());
                })
                .addOnFailureListener(e -> {
                    ErrorDialog.showError(this, e);
                    setProgressBarVisible(false);
                });
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

    private void updateLoginBtn() {
        loginBtn.setEnabled(isEmailValid && isPasswordValid);
        signUpBtn.setEnabled(isEmailValid && isPasswordValid);
    }

    private void createUser() {
        String email = emailEt.getText().toString();
        String password = passEt.getText().toString();
        setProgressBarVisible(true);
        viewModel.createUser(email, password, new OnUserCreatedCallback() {
            @Override
            public void onLoaded(User user) {
                startMainActivity(user.getId());
            }

            @Override
            public void onCancelled(Exception e) {
                loginBtn.setEnabled(true);
                ErrorDialog.showError(LoginSignUpActivity.this, e);
                setProgressBarVisible(false);
            }
        });
    }

    private boolean isEmailValid() {
        String email = emailEt.getText().toString();
        if (email.isEmpty()) {
            emailEt.setError("Fill email to continue!");
            return false;
        }

        String emailRegex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        Pattern pat = Pattern.compile(emailRegex);
        if (!pat.matcher(email).matches()) {
            emailEt.setError("Wrong email format");
            return false;
        }

        emailEt.setError(null);
        return true;
    }

    private boolean isPasswordValid() {

        String password = passEt.getText().toString();
        if (password.isEmpty()) {
            passEt.setError("Fill password to continue!");
            return false;
        }

        if (passEt.length() < 6 || passEt.length() > 10) {
            passEt.setError("length must be between 4 to 10");
            return false;
        }

        passEt.setError(null);
        return true;
    }

    void startMainActivity(String uid) {
        PodcastsDataSource.getInstance().setPodcastsEditable(uid);

        setResult(RESULT_OK);
        startActivity(new Intent(this, MainActivity.class));
        finishActivity(RC_SIGN_IN);
        finish();
    }

    private void setProgressBarVisible(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void toast(@StringRes int id){
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
