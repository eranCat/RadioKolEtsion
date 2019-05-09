package com.erank.radiokoletsionv2.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.activities.LoginActivity;
import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.receivers.MediaPlayerReceiver;
import com.erank.radiokoletsionv2.utils.UserDataHolder;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerAction;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.MorphButton.MorphState;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.widget.SeekBar.OnClickListener;
import static android.widget.SeekBar.OnSeekBarChangeListener;
import static com.erank.radiokoletsionv2.utils.PodcastsDataHolder.PODCASTS_TABLE_NAME;
import static com.erank.radiokoletsionv2.utils.UserDataHolder.USERS_TABLE_NAME;

public class MusicFragment extends Fragment implements OnClickListener {

    private static final String TAG = "MusicFragment";
    private DatabaseReference podcastsDBRef;
    private FirebaseUser user;
    //    from arguments - ctor
    private Podcast podcast;
    private MediaPlayerHolder mPlayerHolder;
    //    from layout
    private ProgressBar progressBar;
    private TextView currentTime, sumTime;
    private SeekBar seekBar;
    private MorphButton playPauseBtn;
    private ImageView prevBtn, nextBtn, shareBtn;

    private ImageView favorite;
    private TextView content;
    private TextView dateTv;
    private boolean isFavorite;
    private boolean isLiked;
    private List<Podcast> podcastFavoritesList;
    private TextView likeAmountTv;
    private ImageView likeBtn;

    private AlertDialog loginDialog;

    private Handler mSeekbarUpdateHandler;
    private OnSeekBarChangeListener seekBarChangeListener;
    private Runnable mUpdateSeekbar;
    private MediaPlayerReceiver mPlayerReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: music");

        user = FirebaseAuth.getInstance().getCurrentUser();
        podcastsDBRef = FirebaseDatabase.getInstance().getReference(PODCASTS_TABLE_NAME);

        mPlayerHolder = MediaPlayerHolder.getInstance();

        bindIds(view);
        loadUserData();

        mSeekbarUpdateHandler = new Handler();

        podcast = mPlayerHolder.getPodcast();
        if (podcast != null)
            fillData();

        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);

        initListeners();
        initPlayerReceiver();

        setBackToolbarButton(true);
    }

    private void loadUserData() {
        if (user != null && !user.isAnonymous()) {
            UserDataHolder userDataHolder = UserDataHolder.getInstance();
            podcastFavoritesList = userDataHolder.getFavorites();
            if (podcastFavoritesList == null) {
                userDataHolder.reloadFavorites(podcastList -> podcastFavoritesList = podcastList);
            }
        }
    }

    private void initPlayerReceiver() {
        this.mPlayerReceiver = new MediaPlayerReceiver(){
            @Override
            public void onPlay() {

            }

            @Override
            public void onPause() {

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mPlayerReceiver,mPlayerReceiver.getFilter());
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.unregisterReceiver(mPlayerReceiver);
    }

    private void bindIds(@NonNull View view) {
        seekBar = view.findViewById(R.id.seekBar);
        playPauseBtn = view.findViewById(R.id.togglePlayBtn);
        currentTime = view.findViewById(R.id.currentTimeTv);
        progressBar = view.findViewById(R.id.playerProgressBar);
        sumTime = view.findViewById(R.id.sumTimeTv);
        content = view.findViewById(R.id.contentText);
        dateTv = view.findViewById(R.id.datePlayerTV);

        prevBtn = view.findViewById(R.id.previousBtn);
        nextBtn = view.findViewById(R.id.nextBtn);

        shareBtn = view.findViewById(R.id.shareBtn);
        likeAmountTv = view.findViewById(R.id.tvLikeAmount);
        likeBtn = view.findViewById(R.id.ivBtnLike);
        favorite = view.findViewById(R.id.favoriteBtn);
    }

    private void initListeners() {
        seekBarChangeListener = new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentTime.setText(durationToTime(progress));
                    mPlayerHolder.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        mUpdateSeekbar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mPlayerHolder.getCurrentPodID());
                updateCurrentTime();
                //updates every second
                mSeekbarUpdateHandler.postDelayed(this, 1000);
            }
        };

        AppCompatActivity activity = (AppCompatActivity) getContext();
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
//            startActivity(new Intent(activity, LoginActivity.class));
//            activity.finish();
            loginWithFirebaseAuth();
        };
        loginDialog = new AlertDialog.Builder(activity)
                .setMessage("Must be logged in to do those stuff")
                .setNegativeButton("Login", onClickListener)
                .setPositiveButton("Nope, guest", null)
                .create();


        playPauseBtn.setOnClickListener(v -> {
            //returns is playing
            if (mPlayerHolder.toggle()) {
                mSeekbarUpdateHandler.post(mUpdateSeekbar);
            } else {
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
            }
        });

        View[] buttons = {prevBtn, nextBtn, shareBtn, likeBtn, favorite};
        for (View button : buttons) button.setOnClickListener(this);

        mPlayerHolder.addOnCompletionListener(mp -> fillData());
    }

    private void loginWithFirebaseAuth() {
        startActivity(new Intent(getContext(), LoginActivity.class));
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.AnonymousBuilder().build(),
//                new AuthUI.IdpConfig.GoogleBuilder().build(),
////                        new AuthUI.IdpConfig.FacebookBuilder().build(),
//                new AuthUI.IdpConfig.EmailBuilder().build()
//        );
//        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .build();

//        getActivity().startActivityForResult(intent,MainActivity.RC_SIGNIN);
//        todo ehmm idk
    }

    private void fillData() {
        content.setText(podcast.getDescription());

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE - dd/MM/yy");
        DateFormat format = new SimpleDateFormat("EEEE - dd/MM/yy", Locale.getDefault());
        dateTv.setText(format.format(podcast.getDate()));

        if (!mPlayerHolder.isPreparing()) {
            updateComponents();
            return;
        }

        //updates here when ready
        mPlayerHolder.setOnPreparedListener(mp -> updateComponents());
//        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 1000);

        progressBar.setVisibility(View.VISIBLE);//loading...
        playPauseBtn.setVisibility(View.INVISIBLE);

        likeAmountTv.setText(String.valueOf(podcast.getLikesAmount()));
    }

    private void updateCurrentTime() {
        int currentPosition = mPlayerHolder.getCurrentPodID();
        currentTime.setText(durationToTime(currentPosition));
    }

    private void updateComponents() {
        initProgress();

        MorphState state = mPlayerHolder.isPlaying() ? MorphState.END : MorphState.START;
        if (playPauseBtn.getState() != state)
            playPauseBtn.setState(state, true);
    }

    private void initProgress() {
        int duration = mPlayerHolder.getDuration();

        seekBar.setMax(duration);
        updateCurrentTime();
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        sumTime.setText(durationToTime(duration));

        progressBar.setVisibility(View.GONE);

        playPauseBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDetach() {
        setBackToolbarButton(false);
        super.onDetach();
    }

    private void setBackToolbarButton(boolean b) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(b);
        actionBar.setDisplayShowHomeEnabled(b);

        Toolbar toolbar = activity.findViewById(R.id.my_toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            activity.onBackPressed();
            reactivateHamburger(toolbar);
        });
    }

    private void reactivateHamburger(Toolbar mToolbar) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity == null) return;

        DrawerLayout drawerLayout = activity.findViewById(R.id.drawerLayout);

        // Initialize ActionBarDrawerToggle, which will control toggle of hamburger.
        // You set the values of R.string.open and R.string.close accordingly.
        // Also, you can implement drawer toggle listener if you want.
        ActionBarDrawerToggle mDrawerToggle =
                new ActionBarDrawerToggle(activity, drawerLayout, mToolbar,
                        R.string.nav_drawer_open, R.string.nav_drawer_close);
        // Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(mDrawerToggle);
        // Calling sync state is necessary to show your hamburger icon...
        // or so I hear. Doesn't hurt including it even if you find it works
        // without it on your test device(s)
        mDrawerToggle.syncState();
    }

    @SuppressLint("DefaultLocale")
    private String durationToTime(int sec) {

        long hours = TimeUnit.MILLISECONDS.toHours(sec);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(sec) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(sec) % TimeUnit.MINUTES.toSeconds(1);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.previousBtn:
                switchSong(() -> mPlayerHolder.playPrevious());
                break;
            case R.id.nextBtn:
                switchSong(() -> mPlayerHolder.playNext());
                break;

            case R.id.ivBtnLike:
                if (user == null || user.isAnonymous()) {
                    likeBtn.setImageResource(R.drawable.ic_heart_off);
                    loginDialog.show();
                } else {
                    toggleHeart();
                    if (!isLiked) {
                        likePodcast();
                    } else {
                        unlikePodcast();
                    }
                }
                break;

            case R.id.favoriteBtn:
                //if a guest
                if (user == null || user.isAnonymous()) {
                    favorite.setImageResource(R.drawable.ic_star_off);
                    loginDialog.show();
                } else {
                    toggleStar();
                    if (!isFavorite) {
                        addToFavorites();
                    } else {
                        removeFromFavorites();
                    }
                }
                break;
            case R.id.shareBtn:
                share();
                break;

        }
    }

    private void likePodcast() {
        updateLike(true);
    }

    private void unlikePodcast() {
        updateLike(false);
    }

    private void updateLike(boolean b) {
        likeBtn.setOnClickListener(null);
        String uid = user.getUid();
        podcastsDBRef.child(podcast.getId())
                .child("likes")
                .child(uid).setValue(b ? true : null)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        isLiked = !isLiked;
                    likeBtn.setOnClickListener(MusicFragment.this);

                    //todo optional read from db any time there's a like
                    long likesAmount = podcast.getLikesAmount();
                    String amountTxt = String.valueOf(isLiked ? likesAmount + 1 : likesAmount);
                    likeAmountTv.setText(amountTxt);
                });
    }


    private void removeFromFavorites() {
        updateFavorite(false);
    }

    private void addToFavorites() {
        updateFavorite(true);
    }

    private void updateFavorite(boolean b) {
        favorite.setOnClickListener(null);

        UserDataHolder.getInstance()
                .updateFavorite(b, podcast.getId())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isFavorite = !isFavorite;
                        int resId = b ? R.string.added_to_fav : R.string.removed_fav;
                        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT)
                                .show();
                    }
                    favorite.setOnClickListener(MusicFragment.this);
                });

        if (b) {
            podcastFavoritesList.add(podcast);
        } else {
            podcastFavoritesList.remove(podcast);
        }
    }

    private void toggleHeart() {
        Animation expandIn = AnimationUtils.loadAnimation(getContext(), R.anim.expand_in);
        expandIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {

                if (isLiked) {
                    likeBtn.setImageResource(R.drawable.ic_heart_on);
                } else {
                    likeBtn.setImageResource(R.drawable.ic_heart_off);
                }
            }

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        likeBtn.startAnimation(expandIn);
    }

    private void toggleStar() {
        Animation expandIn = AnimationUtils.loadAnimation(getContext(), R.anim.expand_in);
        expandIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (isFavorite) {
                    favorite.setImageResource(R.drawable.ic_star_on);
                } else {
                    favorite.setImageResource(R.drawable.ic_star_off);
                }
            }

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        favorite.startAnimation(expandIn);
    }

    private void switchSong(Runnable action) {
        action.run();
        podcast = mPlayerHolder.getPodcast();
        fillData();
    }

    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String msg = getResources().getString(R.string.check_out_pod);
        intent.putExtra(Intent.EXTRA_SUBJECT,
                msg + podcast.getDescription());
        intent.putExtra(Intent.EXTRA_TEXT,
                "bep://be_podcast" + podcast.getAudioUrl());
        String shareTxt = getResources().getString(R.string.share_poscast);
        startActivity(Intent.createChooser(intent, shareTxt));
    }

}
