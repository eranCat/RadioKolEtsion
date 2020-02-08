package com.erank.koletsionpods.utils.listeners;

import com.erank.koletsionpods.db.models.Podcast;

import java.util.List;

public interface FBPodcastsCallback extends OnDataLoadedCallback<List<Podcast>> {
    default void onLoading(long childrenCount) {
    }

    default void onItemLoaded() {
    }
}