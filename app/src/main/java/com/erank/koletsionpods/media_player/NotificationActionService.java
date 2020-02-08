package com.erank.koletsionpods.media_player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.erank.koletsionpods.receivers.NotificationActionReceiver;
import com.erank.koletsionpods.utils.helpers.NotificationHelper;
import com.erank.koletsionpods.utils.listeners.NotificationActionCallback;

import static com.erank.koletsionpods.utils.helpers.NotificationHelper.NOTIFICATION_DELETED_ACTION;

public class NotificationActionService extends Service
        implements NotificationActionCallback, MediaPlayer.OnPreparedListener {

    private MediaPlayerHelper mediaPlayerHelper;
    private NotificationActionReceiver receiver;
    private NotificationHelper notificationHelper;
    private BroadcastReceiver deletionReceiver;

    public NotificationActionService() {
        receiver = new NotificationActionReceiver(this);

        deletionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NOTIFICATION_DELETED_ACTION))
                    stopSelf();
            }
        };

        mediaPlayerHelper = MediaPlayerHelper.getInstance();
        notificationHelper = NotificationHelper.getInstance(this);
        mediaPlayerHelper.addOnPreparedListener(getClass(), this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(receiver, receiver.getIntentFilter());
        registerReceiver(deletionReceiver, new IntentFilter(NOTIFICATION_DELETED_ACTION));
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(deletionReceiver);
        mediaPlayerHelper.pause();
        mediaPlayerHelper.removeOnPreparedListener(getClass());
        notificationHelper.cancelAll();
    }

    @Override
    public void onNotificationPlay() {
        mediaPlayerHelper.play();
        refreshNotification();
    }

    @Override
    public void onNotificationPause() {
        mediaPlayerHelper.pause();
        refreshNotification();
    }

    private void printStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : trace) {
            System.out.println(element);
        }
    }

    @Override
    public void onNotificationNext() {
        printStackTrace();
        mediaPlayerHelper.playNext();
        refreshNotification();
    }

    @Override
    public void onNotificationPrevious() {
        printStackTrace();
        mediaPlayerHelper.playPrevious();
        refreshNotification();
    }

    @Override
    public void onNotificationSeekForward() {
        mediaPlayerHelper.seekForward();
    }

    @Override
    public void onNotificationReplay() {
        mediaPlayerHelper.seekBack();
    }

    private void refreshNotification() {
        notificationHelper.notify(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        notificationHelper.notify(this);
    }
}
