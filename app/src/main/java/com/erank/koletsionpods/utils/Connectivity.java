package com.erank.koletsionpods.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class Connectivity {
    private static final Connectivity ourInstance = new Connectivity();

    public static Connectivity getInstance() {
        return ourInstance;
    }

    private Connectivity() {
    }

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
