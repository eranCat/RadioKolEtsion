package com.erank.koletsionpods.viewmodels;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.erank.koletsionpods.db.UserDataSource;
import com.erank.koletsionpods.utils.helpers.AuthHelper;
import com.erank.koletsionpods.utils.listeners.OnUserCreatedCallback;
import com.erank.koletsionpods.utils.listeners.OnUserDataLoadedListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class LoginSignUpViewModel extends AndroidViewModel {

    private AuthHelper authHelper;

    public LoginSignUpViewModel(@NonNull Application application) {
        super(application);
        authHelper = AuthHelper.getInstance();
    }

    public static LoginSignUpViewModel newInstance(ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner).get(LoginSignUpViewModel.class);
    }

    public Intent getSignInActivity() {
        return authHelper.getSignInIntent(getApplication());
    }

    public void createUser(String email, String password, OnUserCreatedCallback callback) {
        authHelper.createUser(email,password,callback);
    }


    public void SignInUser(String email,String password,OnUserDataLoadedListener listener){
        authHelper.signInUser(email,password, listener );
    }

    public Task<AuthResult> checkResult(int requestCode, Intent data) throws ApiException {
        return authHelper.checkResult(requestCode,data);
    }

    public Task<Void> createUser(FirebaseUser user) {
        return UserDataSource.getInstance().saveUser(user);
    }
}
