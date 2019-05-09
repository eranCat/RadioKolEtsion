package com.erank.radiokoletsionv2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerListener;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerAction;

public class MediaPlayerReceiver extends BroadcastReceiver implements MediaPlayerListener {

    private IntentFilter filter;

    public MediaPlayerReceiver() {
        filter = new IntentFilter();
        for (MediaPlayerAction action : MediaPlayerAction.values()) {
            filter.addAction(action.name());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MediaPlayerAction state = MediaPlayerAction.valueOf(action);
        switch (state) {

            case ACTION_PLAY:
                onPlay();
                break;
            case ACTION_PAUSE:
                onPause();
                break;
            case ACTION_NEXT:
                break;
            case ACTION_PREVIOUS:
                break;
            case ACTION_RESET:
                onReset();
                break;
            case ACTION_SWAP:
                onSwap();
                break;
            case ACTION_PREPARING:
                onPreparing();
                break;
            case ACTION_PREPARED:
                onPrepared();
                break;
            case ACTION_COMPLETED:
                onCompleted();
                break;
        }
    }

    public IntentFilter getFilter() {
        return filter;
    }

    @Override
    public void onPlay() {
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onReset() {

    }

    @Override
    public void onSwap() {

    }

    @Override
    public void onPreparing() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onCompleted() {

    }
}
