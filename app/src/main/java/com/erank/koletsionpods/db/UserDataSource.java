package com.erank.koletsionpods.db;

import androidx.annotation.NonNull;

import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.db.models.User;
import com.erank.koletsionpods.utils.listeners.OnUserDataLoadedListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class UserDataSource {

    private static final String USERS_TABLE_NAME = "users";
    private static final String TAKEN_USERNAMES_TABLE = "takenNames";

    private static UserDataSource instance;
    private DatabaseReference usersRef, takenUNamesRef;
    private FirebaseAuth mAuth;
    private List<Podcast> favorites;
    private User currentUser;

    private UserDataSource() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(USERS_TABLE_NAME);
        takenUNamesRef = database.getReference(TAKEN_USERNAMES_TABLE);
        favorites = new ArrayList<>();
    }

    public static UserDataSource getInstance() {
        return instance == null ? (instance = new UserDataSource()) : instance;
    }

    public void loadUserData(OnUserDataLoadedListener listener) {
        FirebaseUser fbUser = mAuth.getCurrentUser();
        if (fbUser == null) {
            listener.onCancelled(new NullPointerException("no firebase user"));
            return;
        }

        if (fbUser.isAnonymous()) {
            listener.onLoaded(currentUser = new User(fbUser));
            return;
        }

        usersRef.child(fbUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user == null) {
                            listener.onCancelled(new JSONException("User wasn't parsed properly"));
                            return;
                        }

                        favorites = PodcastsDataSource.getInstance().getFavorites(user);
                        currentUser = user;
                        listener.onLoaded(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onCancelled(error.toException());
                    }
                });
    }

    public List<Podcast> getFavorites() {
        return favorites;
    }

    public Task<Void> updateFavorite(boolean isFavorite, Podcast podcast) {

        String podcastId = podcast.getId();
        return getFavoritesRef().child(podcastId)
                .setValue(isFavorite ? true : null)
                .addOnSuccessListener(aVoid -> {
                    if (isFavorite) {
                        currentUser.addFavorite(podcast);
                        favorites.add(podcast);
                    } else {
                        favorites.remove(podcast);
                        currentUser.removeFavorite(podcastId);
                    }
                });
    }

    private DatabaseReference getFavoritesRef() {
        return usersRef.child(currentUser.getId()).child("favorites");
    }

    public Task<Void> removeUserPodcastFromFavorites(int position) {
        String podId = favorites.get(position).getId();

        return getFavoritesRef().child(podId)
                .setValue(null)
                .addOnCompleteListener(task -> {
                    currentUser.removeFavorite(podId);
                    favorites.remove(position);
                });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Task<Void> saveUser(User user) {
        takenUNamesRef.child(user.getName()).setValue(true);

        return usersRef.child(user.getId()).setValue(user)
                .addOnSuccessListener(aVoid -> currentUser = user);
    }

    public Task<Void> saveUser(FirebaseUser user) {
        return saveUser(new User(user));
    }

    public boolean isFavoritePodcast(Podcast podcast) {
        return currentUser.isFavoritePodcast(podcast);
    }

    public DatabaseReference getTakenNamesRef() {
        return takenUNamesRef;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
