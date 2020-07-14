package com.erank.koletsionpods.adapters;

import androidx.annotation.NonNull;

import com.erank.koletsionpods.utils.db.models.Comment;

class CommentsDiffUtilCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<com.erank.koletsionpods.utils.db.models.Comment> {
    @Override
    public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
        return oldItem.equals(newItem);
    }
}
