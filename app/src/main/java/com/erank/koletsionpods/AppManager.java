package com.erank.koletsionpods;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.erank.koletsionpods.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.media_player.NotificationActionService;
import com.erank.koletsionpods.utils.helpers.NotificationHelper;
import com.google.firebase.FirebaseApp;

public class AppManager extends Application {
    public static final String CHANNEL_ID = "notificationService";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

//        notification
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Oreo version and later
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(new Intent(this, NotificationActionService.class));
        NotificationHelper.getInstance(this).cancelAll();
        MediaPlayerHelper.getInstance().release();
    }
}
