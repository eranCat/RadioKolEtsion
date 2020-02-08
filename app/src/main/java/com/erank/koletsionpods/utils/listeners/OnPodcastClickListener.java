package com.erank.koletsionpods.utils.listeners;

import android.media.MediaPlayer;

import com.erank.koletsionpods.db.models.Podcast;

public interface OnPodcastClickListener extends OnItemClickedCallback<Podcast>{
    void onTogglePlayPause(Podcast podcast, int position,
                           MediaPlayer.OnPreparedListener onPreparedListener);
}