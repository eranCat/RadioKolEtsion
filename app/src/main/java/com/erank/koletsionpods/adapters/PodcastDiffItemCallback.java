package com.erank.koletsionpods.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.erank.koletsionpods.utils.db.models.Podcast;

public class PodcastDiffItemCallback extends DiffUtil.ItemCallback<Podcast> {
    @Override
    public boolean areItemsTheSame(@NonNull Podcast oldItem, @NonNull Podcast newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Podcast oldItem, @NonNull Podcast newItem) {
        return oldItem.equals(newItem);
    }
}
