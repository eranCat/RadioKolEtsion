package com.erank.koletsionpods.utils.media_player;

import android.media.MediaPlayer;
import android.os.AsyncTask;

import com.erank.koletsionpods.utils.db.models.Podcast;

import java.io.IOException;

class SetDSAsync extends AsyncTask<Podcast, Void, Void> {
    private static final boolean USING_DEFAULT = true;

    private MediaPlayer mp;

    SetDSAsync(MediaPlayer mp) {
        this.mp = mp;
    }

    @Override
    protected Void doInBackground(Podcast... podcasts) {
        Podcast podcast = podcasts[0];
        try {
            String audioUrl = !USING_DEFAULT ?
                    podcast.getAudioUrl() :
                    podcast.getDefaultStream();

            mp.setDataSource(audioUrl);
            mp.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception any) {
            any.printStackTrace();
            try {
                mp.setDataSource(podcast.getDefaultStream());
                mp.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
