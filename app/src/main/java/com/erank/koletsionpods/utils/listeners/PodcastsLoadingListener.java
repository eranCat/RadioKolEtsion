package com.erank.koletsionpods.utils.listeners;

import com.erank.koletsionpods.utils.db.models.Podcast;

import java.util.List;

public interface PodcastsLoadingListener extends OnDataLoadedCallback<List<Podcast>> {
    default void onLoading() {
    }
}