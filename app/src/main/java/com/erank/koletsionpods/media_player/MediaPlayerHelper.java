package com.erank.koletsionpods.media_player;


import android.media.AudioManager;
import android.media.MediaPlayer;

import androidx.core.math.MathUtils;

import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.utils.enums.PodcastState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.media.MediaPlayer.OnPreparedListener;


public class MediaPlayerHelper
        implements OnPreparedListener, MediaPlayer.OnErrorListener {

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

        initMp();
    }

    public static MediaPlayerHelper getInstance() {
        if (instance == null)
            instance = new MediaPlayerHelper();

        return instance;
    }

    private void initMp() {
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(this);
        mp.setOnErrorListener(this);
    }

    public void playPodcast(Podcast podcast, int position) {

        //if it's the same so just refreshCurrent
        if (podcast.equals(currentPodcast)) {

            if (!mp.isPlaying()) play();
            else pause();

            return;
        }
//        else - new song
        if (currentPodcast != null) {
            currentPodcast.state = PodcastState.DEFAULT;
        }

        currentPodcast = podcast;
        currentPosition = position;

        try {
            mp.reset();

            mp.setDataSource(podcast.getAudioUrl());
            mp.prepareAsync();
            podcast.state = PodcastState.LOADING;
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    public int getCurrentMiliPosition() {
        return mp.getCurrentPosition();
    }

    public int getDuration() {
        return mp.getDuration();
    }

    public void seekTo(int progress) {
        mp.seekTo(progress);
    }

    public void addOnPreparedListener(Class c, OnPreparedListener listener) {
        onPreparedListeners.put(c, listener);
    }

    public void removeOnPreparedListener(Class cKey) {
        onPreparedListeners.remove(cKey);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        currentPodcast.state = PodcastState.PREPARED;
        play();
        for (OnPreparedListener listener : onPreparedListeners.values()) {
            listener.onPrepared(mp);
        }
    }

    private Podcast playPodcast(int pos) {
        Podcast podcast = podDS.getPodcast(pos);
        playPodcast(podcast, pos);
        return podcast;
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

    public void play() {
        if (currentPodcast.isPlaySafe()) {
            mp.start();
            currentPodcast.state = PodcastState.PLAYING;
        }
    }

    public void pause() {
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

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return true;
    }
}