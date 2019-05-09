package com.erank.radiokoletsionv2.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.account_managers.AccountType;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginDetailsDialog extends DialogFragment implements View.OnClickListener {

    private EditText emailEt, passEt;
    private FirebaseAuth mAuth;

    private AccountType accountType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_details_dialog, container, false);
    }

    public static LoginDetailsDialog newInstance(AccountType type) {

        Bundle args = new Bundle();

        args.putString(AccountType.class.getName(),type.name());

        LoginDetailsDialog fragment = new LoginDetailsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String key = AccountType.class.getName();
            String type = arguments.getString(key);
            accountType = AccountType.valueOf(type);
        }

        emailEt = view.findViewById(R.id.et_email);
        passEt = view.findViewById(R.id.etPassword);

        mAuth = FirebaseAuth.getInstance();

        view.findViewById(R.id.btn_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String email = emailEt.getText().toString();
        if (email.isEmpty()) {
            emailEt.setError("Fill email to continue!");
            return;
        }

        String password = passEt.getText().toString();
        if (password.isEmpty()) {
            passEt.setError("Fill password to continue!");
            return;
        }

        v.setEnabled(false);

        switch (accountType) {

            case GOOGLE:
                // Configure Google Sign In
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                break;
            case FACEBOOK:
                break;
        }

//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnSuccessListener(authResult -> {
//                    Intent successIntent = new Intent(LoginActivity.ACTION_LOGIN);
//                    successIntent.putExtra("authResult",authResult);
//                    LocalBroadcastManager.getInstance(getContext())
//                            .sendBroadcast(successIntent);
//                    dismiss();
//                })
//                .addOnFailureListener(error -> {
//                    Intent failIntent = new Intent(LoginActivity.ACTION_LOGIN);
//                    failIntent.putExtra("error",error.getLocalizedMessage());
//                    LocalBroadcastManager.getInstance(getContext())
//                            .sendBroadcast(failIntent);
//                    dismiss();
//                });
    }
}
