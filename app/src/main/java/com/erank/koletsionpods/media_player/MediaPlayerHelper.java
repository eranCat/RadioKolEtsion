package com.erank.koletsionpods.media_player;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import androidx.core.math.MathUtils;

import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.utils.enums.PodcastState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.media.MediaPlayer.OnPreparedListener;


public class MediaPlayerHelper
        implements OnPreparedListener {

    private static final String TAG = MediaPlayerHelper.class.getName();
    //singleton
    private static MediaPlayerHelper instance;
    private final PodcastsDataSource podDS;

    private MediaPlayer mp;
    //    listeners
    private Map<Class, OnPreparedListener> onPreparedListeners;

    private Podcast currentPodcast;
    private int currentPosition;

    private MediaPlayerHelper() {
        podDS = PodcastsDataSource.getInstance();

        onPreparedListeners = new HashMap<>();

        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(this);
    }

    public static MediaPlayerHelper getInstance() {
        if (instance == null)
            instance = new MediaPlayerHelper();

        return instance;
    }

    public void playPodcast(Podcast podcast, int position) {

        //if it's the same so just refreshCurrent
        if (currentPodcast != null) {
            if (podcast.getId().equals(currentPodcast.getId())) {

                if (!mp.isPlaying()) play();
                else pause();

                return;
            } else currentPodcast.state = PodcastState.DEFAULT;
        }

        currentPodcast = podcast;
        currentPosition = position;

        mp.reset();

        new SetDSAsync(mp).execute(podcast.getAudioUrl());
        podcast.state = PodcastState.LOADING;
    }

    private Podcast playPodcast(int pos) {
        Podcast podcast = podDS.getPodcast(pos);
        playPodcast(podcast, pos);
        return podcast;
    }

    public int getCurrentMiliPosition() {
        return mp.getCurrentPosition();
    }

    public int getDuration() {
        if (currentPodcast.isLoading() || currentPodcast.state == PodcastState.DEFAULT)
            return 0;

        return mp.getDuration();
    }

    public void seekTo(int progress) {
        mp.seekTo(progress);
    }

    public void addOnPreparedListener(OnPreparedListener listener) {
        onPreparedListeners.put(listener.getClass(), listener);
    }

    public void removeOnPreparedListener(OnPreparedListener listener) {
        onPreparedListeners.remove(listener.getClass());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        currentPodcast.state = PodcastState.PREPARED;
        play();
        for (OnPreparedListener listener : onPreparedListeners.values()) {
            listener.onPrepared(mp);
        }
    }

    public Podcast playNext() {
        currentPosition = (currentPosition + 1) % podDS.getPodcastsSize();
        return playPodcast(currentPosition);
    }

    public Podcast playPrevious() {
        int size = podDS.getPodcastsSize();
        currentPosition = (currentPosition > 0 ? currentPosition : size) - 1;
        return playPodcast(currentPosition);
    }

    void seekForward() {
        seekBy(30);
    }

    void seekBack() {
        seekBy(-30);
    }


    public void seekBy(int sec) {
        if (!currentPodcast.isReady()) {
            return;
        }

        int position = mp.getCurrentPosition();
        int res = position + sec * 1000;
        int clamped = MathUtils.clamp(res, 0, mp.getDuration());
        mp.seekTo(clamped);
    }

    public Podcast getCurrentPodcast() {
        return currentPodcast;
    }

    public int getCurrentPodcastPosition() {
        return currentPosition;
    }

    public int getProgress() {
        return mp.getCurrentPosition();
    }

    void play() {
        if (currentPodcast.isPlaySafe()) {
            mp.start();
            currentPodcast.state = PodcastState.PLAYING;
        }
    }

    void pause() {
        PodcastState state = currentPodcast.state;
        if (state == PodcastState.PLAYING) {
            mp.pause();
            currentPodcast.state = PodcastState.PAUSED;
        }
    }

    public void release() {
        mp.release();
        mp = null;
    }
}