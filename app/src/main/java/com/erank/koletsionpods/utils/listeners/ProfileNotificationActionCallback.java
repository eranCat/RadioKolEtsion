package com.erank.koletsionpods.utils.listeners;

public interface ProfileNotificationActionCallback extends NotificationActionCallback {

    @Override
    default void onNotificationNext() {

    }

    @Override
    default void onNotificationPrevious() {

    }

    @Override
    default void onNotificationSeekForward() {

    }

    @Override
    default void onNotificationReplay() {

    }
}
