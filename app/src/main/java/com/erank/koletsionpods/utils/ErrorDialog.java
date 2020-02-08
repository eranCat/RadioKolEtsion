package com.erank.koletsionpods.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.StringRes;

import com.erank.koletsionpods.R;

public class ErrorDialog {
    private static final String TAG = ErrorDialog.class.getName();

    public static AlertDialog showError(Context context, Exception e) {
        return showError(context, R.string.errorTitle, e);
    }

    public static AlertDialog showError(Context context, String title, Exception e){
        Log.d(TAG, "showError: "+e);

        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(e.getLocalizedMessage())
                .setPositiveButton(R.string.errorPosBtn, null)
                .show();
    }

    private static AlertDialog showError(Context context, @StringRes int titleRes, Exception e) {
        String title = context.getString(titleRes);
        return showError(context,title,e);
    }
}
