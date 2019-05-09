package com.erank.radiokoletsionv2.utils.media_player;

public interface MediaPlayerListener {
    void onPlay();
    void onPause();
    void onReset();

    void onSwap();

    void onPreparing();
    void onPrepared();

    void onCompleted();
}
