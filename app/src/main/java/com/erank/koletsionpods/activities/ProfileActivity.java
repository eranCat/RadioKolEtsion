package com.erank.koletsionpods.activities;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.erank.koletsionpods.R;
import com.erank.koletsionpods.adapters.PodcastAdapter;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.receivers.NotificationActionReceiver;
import com.erank.koletsionpods.utils.ErrorDialog;
import com.erank.koletsionpods.utils.helpers.NotificationHelper;
import com.erank.koletsionpods.utils.listeners.OnItemClickedCallback;
import com.erank.koletsionpods.utils.listeners.OnPodcastClickListener;
import com.erank.koletsionpods.utils.listeners.ProfileNotificationActionCallback;
import com.erank.koletsionpods.viewmodels.ProfileFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity
        implements OnPodcastClickListener, MediaPlayer.OnPreparedListener,
        ProfileNotificationActionCallback {

    private static final String TAG = ProfileActivity.class.getName();
    private TextView usernameTv;
    private TextView emailTv;
    private ImageView profileImage;

    private RecyclerView favoritesRv;
    private PodcastAdapter podcastAdapter;
    private View noFavoritesTv;


    private ProfileFragmentViewModel viewModel;
    private MediaPlayerHelper mpHelper;
    private OnItemClickedCallback<Podcast> listener;
    private MediaPlayer.OnPreparedListener onPreparedListener;

    private NotificationActionReceiver broadcastReceiver =
            new NotificationActionReceiver(this);
    private NotificationHelper notificationHelper;

    public void setListener(OnItemClickedCallback<Podcast> listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        viewModel = ProfileFragmentViewModel.newInstance(this);
        mpHelper = MediaPlayerHelper.getInstance();
        notificationHelper = NotificationHelper.getInstance(this);

        mpHelper.addOnPreparedListener(getClass(), this);

        findViews();

        loadData();

        updateUI();

        registerNotificationReceiver();
    }

    private void registerNotificationReceiver() {
        this.registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    public void onStart() {
        super.onStart();
        mpHelper.addOnPreparedListener(getClass(), this);
        registerNotificationReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        mpHelper.removeOnPreparedListener(getClass());
        this.unregisterReceiver(broadcastReceiver);
    }

    private void findViews() {
        favoritesRv = findViewById(R.id.rvFavorites);

        podcastAdapter = viewModel.getNewPodcastAdapter(this);
        favoritesRv.setAdapter(podcastAdapter);
        viewModel.setSwipeToRemove(favoritesRv, podcastAdapter);

        emailTv = findViewById(R.id.tv_email);
        noFavoritesTv = findViewById(R.id.noFavsTv);
        usernameTv = findViewById(R.id.tv_username);
        profileImage = findViewById(R.id.profilePic);
    }

    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        emailTv.setText(user.getEmail());
        usernameTv.setText(user.getDisplayName());

        Glide.with(profileImage)
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.ic_person_dummy)
                .into(profileImage);

    }

    @Override
    public void onItemClicked(Podcast podcast, int pos) {
        listener.onItemClicked(podcast, pos);
    }

    @Override
    public void onRemoveClicked(Podcast podcast, int position) {
        viewModel.remove(position, podcastAdapter, this)
                .addOnFailureListener(e -> ErrorDialog.showError(this, e))
                .addOnSuccessListener(aVoid -> updateUI());
    }

    private void updateUI() {
        boolean hasFavorites = viewModel.hasFavorites();
        favoritesRv.setVisibility(hasFavorites ? View.VISIBLE : View.INVISIBLE);
        noFavoritesTv.setVisibility(hasFavorites ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTogglePlayPause(Podcast podcast, int position,
                                  MediaPlayer.OnPreparedListener listener) {
        onPreparedListener = listener;
        mpHelper.playPodcast(podcast, position);
        notificationHelper.notify(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared(mp);
        }
        notificationHelper.notify(this);
    }

    @Override
    public void onNotificationPlay() {
        podcastAdapter.refreshCurrent();
    }

    @Override
    public void onNotificationPause() {
        podcastAdapter.refreshCurrent();
    }
}
