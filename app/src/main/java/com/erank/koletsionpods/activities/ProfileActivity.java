package com.erank.koletsionpods.activities;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.erank.koletsionpods.R;
import com.erank.koletsionpods.adapters.PodcastAdapter;
import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.UserDataSource;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.receivers.NotificationActionReceiver;
import com.erank.koletsionpods.utils.ErrorDialog;
import com.erank.koletsionpods.utils.helpers.NotificationHelper;
import com.erank.koletsionpods.utils.listeners.OnPodcastClickListener;
import com.erank.koletsionpods.utils.listeners.ProfileNotificationActionCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ProfileActivity extends AppCompatActivity
        implements OnPodcastClickListener, MediaPlayer.OnPreparedListener,
        ProfileNotificationActionCallback {

    private static final String TAG = ProfileActivity.class.getName();
    private TextView usernameTv;
    private TextView emailTv;
    private ImageView profileImage;

    private RecyclerView favoritesRv;
    private ProgressBar progressBarList;
    private PodcastAdapter podcastAdapter;
    private View noFavoritesTv;
    private UserDataSource usersDS;
    private MediaPlayerHelper mpHelper;

    private NotificationActionReceiver broadcastReceiver =
            new NotificationActionReceiver(this);
    private NotificationHelper notificationHelper;
    private PodcastsDataSource podcastsDs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usersDS = UserDataSource.getInstance();
        mpHelper = MediaPlayerHelper.getInstance();
        mpHelper.addOnPreparedListener(this);
        notificationHelper = NotificationHelper.getInstance(this);
        podcastsDs = PodcastsDataSource.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViews();

        loadUserData();

        updateFavorites();

        registerNotificationReceiver();
    }

    private void registerNotificationReceiver() {
        this.registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    public void onStart() {
        super.onStart();
        mpHelper.addOnPreparedListener(this);
        registerNotificationReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        mpHelper.removeOnPreparedListener(this);
        this.unregisterReceiver(broadcastReceiver);
    }

    private void findViews() {
        favoritesRv = findViewById(R.id.rvPodcasts);

        podcastAdapter = new PodcastAdapter(usersDS.getFavorites(), true, this);
        favoritesRv.setAdapter(podcastAdapter);
        setSwipeToRemove();

        emailTv = findViewById(R.id.tv_email);
        noFavoritesTv = findViewById(R.id.noFavsTv);
        usernameTv = findViewById(R.id.tv_username);
        profileImage = findViewById(R.id.profilePic);
        progressBarList = findViewById(R.id.progressBarList);

        noFavoritesTv.setOnClickListener(v -> onBackPressed());
    }

    public void setSwipeToRemove() {
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0
                , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeFavorite(viewHolder.getAdapterPosition());
            }
        });
        itemTouchHelper.attachToRecyclerView(favoritesRv);
    }


    public void removeFavorite(int pos) {
        progressBarList.setVisibility(VISIBLE);
        usersDS.removeUserPodcastFromFavorites(pos)
                .addOnCompleteListener(task -> progressBarList.setVisibility(GONE))
                .addOnFailureListener(e -> ErrorDialog.showError(this, e))
                .addOnSuccessListener(aVoid -> {
                    podcastAdapter.notifyItemRemoved(pos);
                    updateFavorites();
                });
    }

    private void showRemoveDialog(int position) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to remove from favorites?")
                .setNegativeButton("nah", null)
                .setPositiveButton("yeah", (d, w) -> {
                    removeFavorite(position);
                })
                .show();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        emailTv.setText(user.getEmail());

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            String name = usersDS.getCurrentUser().getName();
            usernameTv.setText(name);
        } else {
            usernameTv.setText(displayName);
        }

        Glide.with(profileImage)
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.dog_face)
                .into(profileImage);

    }

    @Override
    public void onItemClicked(Podcast podcast, int pos) {
        Intent intent = new Intent(this, PlayerActivity.class)
                .putExtra(PlayerActivity.CURRENT_POD_ID, podcast.getId());

        startActivity(intent);
    }

    @Override
    public void onTogglePlayPause(Podcast podcast, int position) {
        mpHelper.playPodcast(podcast, podcastsDs.indexOf(podcast));
        notificationHelper.notify(this);
    }

    @Override
    public void onRemoveClicked(Podcast podcast, int position) {
        showRemoveDialog(position);
    }

    private void updateFavorites() {
        boolean hasFavorites = !usersDS.getFavorites().isEmpty();
        favoritesRv.setVisibility(hasFavorites ? VISIBLE : INVISIBLE);
        noFavoritesTv.setVisibility(hasFavorites ? GONE : VISIBLE);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        notificationHelper.notify(this);
        podcastAdapter.refreshCurrent();
    }

    @Override
    public void onNotificationPlay() {
        podcastAdapter.refreshCurrent();
    }

    @Override
    public void onNotificationPause() {
        podcastAdapter.refreshCurrent();
    }

    @Override
    public void onNotificationNext() {
        podcastAdapter.refreshCurrent();
    }

    @Override
    public void onNotificationPrevious() {
        podcastAdapter.refreshCurrent();
    }
}
