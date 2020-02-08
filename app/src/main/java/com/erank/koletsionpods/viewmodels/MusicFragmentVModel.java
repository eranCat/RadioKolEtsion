package com.erank.koletsionpods.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.erank.koletsionpods.adapters.CommentsAdapter;
import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.UserDataSource;
import com.erank.koletsionpods.db.models.Comment;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.utils.helpers.AuthHelper;
import com.erank.koletsionpods.utils.helpers.SharingHelper;
import com.erank.koletsionpods.utils.listeners.OnCommentClickCallback;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MusicFragmentVModel extends AndroidViewModel {

    public Podcast currentPodcast;
    private int podcastPosition;
    private UserDataSource usersDataBase;
    private PodcastsDataSource podcastsDS;
    private FirebaseUser user;
    private CommentsAdapter commentsAdapter;
    private AuthHelper authHelper;

    public MusicFragmentVModel(@NonNull Application application) {
        super(application);

        usersDataBase = UserDataSource.getInstance();
        podcastsDS = PodcastsDataSource.getInstance();
        authHelper = AuthHelper.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public static MusicFragmentVModel newInstance(ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner).get(MusicFragmentVModel.class);
    }

    public void setPodcastFromArgs(Podcast podcast, int pos) {
        podcastPosition = pos;
        currentPodcast = podcast;
    }

    public boolean isUserLogged() {
        return authHelper.isUserLogged(false);
    }

    public String getPodPositionString() {
        return String.format("%s/%s", podcastPosition + 1,
                podcastsDS.getPodcastsSize());
    }

    public String getPodcastDateString() {
        DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.FULL);
        return format.format(currentPodcast.getDate());
    }

    public void playPodcast(MediaPlayerHelper mpHolder) {
        mpHolder.playPodcast(currentPodcast, podcastPosition);
    }

    public void playNext(MediaPlayerHelper mpHolder) {
        currentPodcast = mpHolder.playNext();
        podcastPosition = mpHolder.getCurrentPodcastPosition();
    }

    public void playPrevious(MediaPlayerHelper mpHolder) {
        currentPodcast = mpHolder.playPrevious();
        podcastPosition = mpHolder.getCurrentPodcastPosition();
    }

    public Task<Void> updateLike(boolean isLiked) {
        return podcastsDS.updateLike(isLiked, currentPodcast, user);
    }

    public long getPodLikesAmount() {
        return currentPodcast.getLikesAmount();
    }

    public Task<Void> updateFavorite(boolean isFavorite) {
        return usersDataBase.updateFavorite(isFavorite, currentPodcast, user);
    }

    public Intent getShareIntent(Resources rss) {
        return SharingHelper.getInstance().getShare(rss, currentPodcast);
    }

    public boolean doesUserLikePodcast() {
        return currentPodcast.getLikedUIDs().contains(user.getUid());
    }

    public boolean isUsersFavorite() {
        return usersDataBase.isFavoritePodcast(currentPodcast);
    }

    public int postComment(String commentContent) {
        Comment comment = podcastsDS.commentOnPost(commentContent, currentPodcast);
        currentPodcast.addComment(comment);
        int lastIndex = currentPodcast.getCommentsList().size() - 1;
        commentsAdapter.submitList(currentPodcast.getCommentsList());
        commentsAdapter.notifyItemInserted(lastIndex);
        return lastIndex;
    }

    public Task<Void> remove(Comment comment, int pos) {
        return podcastsDS
                .removeComment(currentPodcast, comment)
                .addOnSuccessListener(aVoid -> {
                    currentPodcast.removeComment(comment);
                    commentsAdapter.submitList(currentPodcast.getCommentsList());
                    commentsAdapter.notifyItemRemoved(pos);
                });
    }

    public Task<Void> updateComment(Comment comment, int pos, String content) {
        return podcastsDS.updateComment(currentPodcast, comment, content)
                .addOnSuccessListener(v -> commentsAdapter.notifyItemChanged(pos));
    }

    public void setCommentsAdapter(RecyclerView rv, OnCommentClickCallback callback) {
        commentsAdapter = new CommentsAdapter(currentPodcast.getCommentsList());
        commentsAdapter.setCallback(callback);
        rv.setAdapter(commentsAdapter);
    }

    public boolean isPodcastPlaying() {
        return currentPodcast.isPlaying();
    }

    public boolean isPodcastLoading() {
        return currentPodcast.isLoading();
    }

    public boolean arePodcastsTheSame(MediaPlayerHelper mpHolder) {
        return currentPodcast.equals(mpHolder.getCurrentPodcast());
    }

    public String getPodDescription() {
        return currentPodcast.getDescription();
    }
}
