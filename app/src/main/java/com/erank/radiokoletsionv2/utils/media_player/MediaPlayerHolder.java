package com.erank.radiokoletsionv2.utils.media_player;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.utils.PodcastsDataHolder;
import com.erank.radiokoletsionv2.utils.JsonParser;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import static android.media.MediaPlayer.OnCompletionListener;
import static android.media.MediaPlayer.OnPreparedListener;


public class MediaPlayerHolder
        implements OnPreparedListener, OnCompletionListener, PodcastsDataHolder.FBPodcastsCallback

        /*JsonParser.PodcastsLoadingListener*/ {
    private static final String TAG = MediaPlayerHolder.class.getSimpleName();
    //singleton
    private static MediaPlayerHolder instance;

    private MediaPlayer mp;
    //    listeners
    private OnPreparedListener onPreparedListener;
    private OnCompletionListener onCompletionListener;

    //    state
    private boolean isPreparing, isReady;

    private Podcast podcast;//current playing
    private String currentPodID;//current id of podcast
    private List<Podcast> podcastList;//saves the list (ref) for playlist
    private WeakReference<Context> contextWeakRef;

    private LocalBroadcastManager broadcastManager;
    private int currentPosition;

    private MediaPlayerHolder(Context context) {
        if (context != null) {
            mp = new MediaPlayer();
            broadcastManager = LocalBroadcastManager.getInstance(context);
            contextWeakRef = new WeakReference<>(context);
        }
    }
    public void setData(List<Podcast> podcastsList) {
        this.podcastList = podcastsList;
    }

    //TODO call only from appManager !!!
    public static MediaPlayerHolder getInstance(Context context) {
        if (instance == null)
            instance = new MediaPlayerHolder(context);

        return instance;
    }

    public static MediaPlayerHolder getInstance() {
        return getInstance(null);
    }

    public boolean playPodcast(int currentPosition) {
        Podcast podcast = podcastList.get(currentPosition);

        //if it's the same so just toggle
        if (podcast.equals(this.podcast)) {
//            toggle();
            play();
            return false;
        }

//        else - new song
        this.podcast = podcast;
        this.currentPosition = currentPosition;
        this.currentPodID = podcast.getId();

//      switch to new song
        reset();

        try {
            mp.setDataSource(this.podcast.getAudioUrl());
            mp.setOnPreparedListener(this);
            mp.setOnCompletionListener(this);
            mp.prepareAsync();
            sendPlayerBroadcast(MediaPlayerAction.ACTION_PREPARING);
            sendPlayerBroadcast(MediaPlayerAction.ACTION_SWAP);
            isPreparing = true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;// new podcast swapped
    }

    //    cleans the mp
    public void reset() {
        if (mp != null) {
            try {
                mp.reset();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        isReady = false;
        isPreparing = true;
        sendPlayerBroadcast(MediaPlayerAction.ACTION_RESET);
    }


    public boolean toggle() {
        Log.d(TAG, "toggle: ");
        try {
            if (isPlaying()) {
                mp.pause();
                return false;
            } else {
                mp.start();
                return true;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return isPlaying();
    }


    public int getCurrentPodID() {
        return mp.getCurrentPosition();
    }

    public boolean isPlaying() {
        return !isPreparing &&
                isReady &&
                mp != null &&
                mp.isPlaying();
    }

    public int getDuration() {
        return mp.getDuration();
    }

    public void seekTo(int progress) {
        mp.seekTo(progress);
    }

    public void setOnPreparedListener(@NonNull OnPreparedListener listener) {
        onPreparedListener = listener;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPreparing = false;
        isReady = true;
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared(mp);
        }
        play();
        sendPlayerBroadcast(MediaPlayerAction.ACTION_PREPARED);
    }

    public void addOnCompletionListener(@NonNull OnCompletionListener listener) {
        onCompletionListener = listener;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion(mp);
        }
        playNext();
        sendPlayerBroadcast(MediaPlayerAction.ACTION_COMPLETED);
    }

    public void playNext() {
        if (podcastList != null) {
            currentPosition = ++currentPosition % podcastList.size();
            playPodcast(currentPosition);
        }
    }

    public void playPrevious() {
        if (podcastList != null) {
            if (currentPosition > 0) {
                currentPosition--;
            } else {
                currentPosition = podcastList.size() - 1;//last
            }

            playPodcast(currentPosition);
        }
    }

    public void setVolume(float left, float right) {
        if (mp != null) {
            mp.setVolume(left, right);
        }
    }

    public void setVolume(float volume) {
        setVolume(volume, volume);
    }

    public boolean isPreparing() {
        return isPreparing;
    }

    public boolean isReady() {
        return isReady;
    }

    public Podcast getPodcast() {
//        TODO throw null pointer exception
        return podcast;
    }

    public void play() {
        Log.d(TAG, "play: ");
        if (isReady()) {
            mp.start();
            sendPlayerBroadcast(MediaPlayerAction.ACTION_PLAY);
        }
    }

    public void pause() {
        Log.d(TAG, "pause: ");
        if (isPlaying()) {
            mp.pause();
            sendPlayerBroadcast(MediaPlayerAction.ACTION_PAUSE);
        }
    }

    @Override
    public void onLoaded(List<Podcast> podcastsList) {
        playPodcast(currentPosition);
        this.podcastList = podcastsList;
    }

    public void release() {
        mp.release();
        mp = null;
    }

    private void sendPlayerBroadcast(MediaPlayerAction action) {
        Intent intent = new Intent(action.toString());
        broadcastManager.sendBroadcast(intent);
    }
}