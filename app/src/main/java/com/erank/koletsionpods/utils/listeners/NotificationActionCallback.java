package com.erank.koletsionpods.utils.listeners;

public interface NotificationActionCallback {
    void onNotificationPlay();

    void onNotificationPause();

    void onNotificationNext();

    void onNotificationPrevious();

    void onNotificationSeekForward();

    void onNotificationReplay();
}