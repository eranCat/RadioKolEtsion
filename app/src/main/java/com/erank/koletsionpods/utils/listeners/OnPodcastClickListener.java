package com.erank.koletsionpods.utils.listeners;

import com.erank.koletsionpods.utils.db.models.Podcast;

public interface OnPodcastClickListener extends OnItemClickedCallback<Podcast>{
    void onTogglePlayPause(Podcast podcast, int position);
}