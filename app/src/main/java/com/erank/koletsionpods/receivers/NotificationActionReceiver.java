package com.erank.koletsionpods.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.erank.koletsionpods.utils.enums.MPServiceStates;
import com.erank.koletsionpods.utils.listeners.NotificationActionCallback;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationActionReceiver.class.getName();

    private final NotificationActionCallback callback;
    private final IntentFilter intentFilter;

    public NotificationActionReceiver(NotificationActionCallback callback) {
        this.callback = callback;
        intentFilter = new IntentFilter(NotificationActionBroadcaster.ACTION_NOTIFICATION);
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("action");
        MPServiceStates action = MPServiceStates.valueOf(name);

        switch (action) {
            case ACTION_PLAY:
                callback.onNotificationPlay();
                break;
            case ACTION_PAUSE:
                callback.onNotificationPause();
                break;
            case ACTION_FORWARD:
                callback.onNotificationSeekForward();
                break;
            case ACTION_REWIND:
                callback.onNotificationReplay();
                break;
            case ACTION_NEXT:
                callback.onNotificationNext();
                break;
            case ACTION_PREVIOUS:
                callback.onNotificationPrevious();
                break;
        }
    }
}
