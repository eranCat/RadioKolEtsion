package com.erank.koletsionpods.utils.helpers;

import android.content.Context;
import android.media.AudioManager;

import static android.media.AudioManager.ADJUST_LOWER;
import static android.media.AudioManager.ADJUST_RAISE;
import static android.media.AudioManager.STREAM_MUSIC;

public class SoundHelper {
    private static SoundHelper ourInstance ;

    public static SoundHelper getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance   = new SoundHelper(context);
        }
        return ourInstance;
    }
    private AudioManager audioManager;

    private SoundHelper(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public int getMaxVolume() {
        return audioManager.getStreamMaxVolume(STREAM_MUSIC);
    }

    public int getCurrentVolume() {
        return audioManager.getStreamVolume(STREAM_MUSIC);
    }

    public void setVolume(int newVolume) {
        audioManager.setStreamVolume(STREAM_MUSIC, newVolume, 0);
    }

    private int adjustVolume(int adjust){
        audioManager.adjustVolume(adjust, AudioManager.FLAG_PLAY_SOUND);
        return getCurrentVolume();
    }


    public int lower() {
        return adjustVolume(ADJUST_LOWER);
    }

    public int raise() {
        return adjustVolume(ADJUST_RAISE);
    }
}
