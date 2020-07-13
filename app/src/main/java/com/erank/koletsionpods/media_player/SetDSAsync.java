package com.erank.koletsionpods.media_player;

import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.io.IOException;

class SetDSAsync extends AsyncTask<String, Void, Void> {

    private MediaPlayer mp;

    SetDSAsync(MediaPlayer mp) {
        this.mp = mp;
    }

    @Override
    protected Void doInBackground(String... urls) {
        try {
            mp.setDataSource(urls[0]);
            mp.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
