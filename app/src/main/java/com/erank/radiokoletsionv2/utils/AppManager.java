package com.erank.radiokoletsionv2.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.erank.radiokoletsionv2.activities.SplashScreenActivity;
import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerHolder;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerService;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.List;
import java.util.Map;

public class AppManager extends Application {
    public static final String CHANNEL_ID = "notificationService";

    @Override
    public void onCreate() {
        super.onCreate();
//        needed for the time usage
        AndroidThreeTen.init(this);

//        GoogleAccountManager.init(getApplicationContext());
//        notification
        createNotificationChannel();

        //show splash screen once
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        MediaPlayerHolder.getInstance(getApplicationContext());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Oreo version and later
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Radio kol etsion",
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        stopService(new Intent(getApplicationContext(), MediaPlayerService.class));
        MediaPlayerHolder.getInstance().release();
    }
}
