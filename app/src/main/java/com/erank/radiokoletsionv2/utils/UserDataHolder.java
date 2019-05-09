package com.erank.radiokoletsionv2.utils;

import androidx.annotation.NonNull;

import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserDataHolder {

    public static final String USERS_TABLE_NAME = "Users";

    private static UserDataHolder instance;

    private UserDataHolder() {
    }

    public static UserDataHolder getInstance() {
        return instance == null ? (instance = new UserDataHolder()) : instance;
    }

    private FirebaseUser user;
    private List<Podcast> favorites;

    public void reloadFavorites(OnFavoritesLoaded onFavoritesLoaded) {
        favorites = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)return;

        DatabaseReference usersData = FirebaseDatabase.getInstance().getReference(UserDataHolder.USERS_TABLE_NAME)
                .child(user.getUid());

        DatabaseReference podcastsRef = FirebaseDatabase.getInstance().getReference(PodcastsDataHolder.PODCASTS_TABLE_NAME);

        usersData.child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    List<Podcast> podcastList = new ArrayList<>();

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String podcastKey = child.getKey();
                        addPodcastFromJson(podcastKey, podcastsRef, podcastList);
                    }

                    onFavoritesLoaded.onLoaded(podcastList);
                    UserDataHolder.this.favorites = podcastList;
                }
//                else { TODO reactivate this back at profilefragment
//                    noFavsTv.setVisibility(View.VISIBLE);
//                    swipeRefreshLayout.setVisibility(View.INVISIBLE);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addPodcastFromJson(String podcastKey, DatabaseReference podcastsRef,
                                    List<Podcast> podcastList) {
        podcastsRef.child(podcastKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Podcast podcast = dataSnapshot.getValue(Podcast.class);
                podcastList.add(podcast);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public List<Podcast> getFavorites() {
        return favorites;
    }

    public Task<Void> updateFavorite(boolean flag, String podcastID) {
        DatabaseReference usersDBRef = FirebaseDatabase.getInstance().getReference(UserDataHolder.USERS_TABLE_NAME)
                .child(user.getUid());

        Task<Void> favoritesTask = usersDBRef.child(user.getUid())
                .child("favorites")
                .child(podcastID)
                .setValue(flag ? true : null);

        return favoritesTask;
    }

    public interface OnFavoritesLoaded {
        void onLoaded(List<Podcast> podcastList);
    }

}
