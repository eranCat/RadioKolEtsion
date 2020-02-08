package com.erank.koletsionpods.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionBroadcaster extends BroadcastReceiver {

    public static final String ACTION_NOTIFICATION = "PodcastsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(
                new Intent(ACTION_NOTIFICATION)
                .putExtra("action",intent.getAction())
        );
    }
}
