package com.erank.koletsionpods.utils.listeners;

public interface PodcastsNotificationActionCallback extends NotificationActionCallback {
    @Override
    default void onNotificationSeekForward() {

    }

    @Override
    default void onNotificationReplay() {

    }
}
