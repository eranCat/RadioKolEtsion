package com.erank.koletsionpods.utils.db.models;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User {
    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private Set<String> favoritesIds;

    public User() {
        setFavorites(new HashMap<>());
    }

    public User(FirebaseUser user) {
        this();
        this.setId(user.getUid());
        this.setEmail(user.getEmail());

        Uri photoUrl = user.getPhotoUrl();
        this.setPhotoUrl(photoUrl != null ? photoUrl.toString() : "");

        String displayName = user.getDisplayName();
        this.setName(displayName != null ? displayName : "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Map<String, Boolean> getFavorites() {
        Map<String, Boolean> map = new HashMap<>();
        for (String id : favoritesIds) map.put(id, true);
        return map;
    }

    public void setFavorites(Map<String, Boolean> favoritesIds) {
        setFavorites(new HashSet<>(favoritesIds.keySet()));
    }

    @Exclude
    public void setFavorites(Set<String> ids) {
        this.favoritesIds = ids;
    }

    @Exclude
    public Set<String> getFavoritesIdsSet() {
        return favoritesIds;
    }

    @Exclude
    public void addFavorite(Podcast podcast) {
        favoritesIds.add(podcast.getId());
    }

    @Exclude
    public void removeFavorite(String podcastId) {
        favoritesIds.remove(podcastId);
    }

    @Exclude
    public boolean isFavoritePodcast(Podcast podcast) {
        return favoritesIds.contains(podcast.getId());
    }
}
