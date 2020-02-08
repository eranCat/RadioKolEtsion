package com.erank.koletsionpods.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.db.models.Comment;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.receivers.NotificationActionReceiver;
import com.erank.koletsionpods.utils.helpers.AuthHelper;
import com.erank.koletsionpods.utils.ErrorDialog;
import com.erank.koletsionpods.utils.helpers.NotificationHelper;
import com.erank.koletsionpods.utils.helpers.SoundHelper;
import com.erank.koletsionpods.utils.listeners.NotificationActionCallback;
import com.erank.koletsionpods.utils.listeners.OnCommentClickCallback;
import com.erank.koletsionpods.utils.listeners.OnSeekBarChangeListenerAdapter;
import com.erank.koletsionpods.utils.listeners.OnTextChangedAdapter;
import com.erank.koletsionpods.viewmodels.MusicFragmentVModel;
import com.wnafee.vector.MorphButton;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static com.wnafee.vector.MorphButton.MorphState.END;
import static com.wnafee.vector.MorphButton.MorphState.START;

public class PlayerActivity extends AppCompatActivity
        implements OnClickListener, OnCommentClickCallback,
        MediaPlayer.OnPreparedListener, NotificationActionCallback {

    //    from arguments - ctor

    public static final String CURRENT_POD_POS = "currentPodcastPos";
    public static final String CURRENT_POD = "currentPodcast";
    private static final int SEEKBAR_REFRESH_RATE = 1_000;
    private MediaPlayerHelper mpHolder;
    //    from layout
    private ProgressBar playerPb, commentsPb;
    private TextView currentTime, sumTime;
    private SeekBar timeSeekbar;
    private Handler mSeekbarUpdateHandler;
    private Runnable mUpdateSeekbar;
    private MorphButton playPauseBtn;
    private View descriptionBox;
    private TextView contentTv, dateTv, playlistCountTv, likeAmountTv;
    private ImageView favoriteBtn, likeBtn;
    private boolean isFavorite, isLiked;
    private SeekBar seekbarVolume;
    private MusicFragmentVModel viewModel;
    private SoundHelper soundHelper;
    private AuthHelper authHelper;
    private EditText commentET;
    private RecyclerView commentsRv;
    private Comment editingComment;
    private int editingCommentPos;

    private NotificationActionReceiver broadcastReceiver =
            new NotificationActionReceiver(this);
    private NotificationHelper notificationHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mpHolder = MediaPlayerHelper.getInstance();
        mpHolder.addOnPreparedListener(getClass(), this);

        viewModel = MusicFragmentVModel.newInstance(this);
        soundHelper = SoundHelper.getInstance(this);
        authHelper = AuthHelper.getInstance();
        notificationHelper = NotificationHelper.getInstance(this);

        Intent intent = getIntent();
        int pos = intent.getIntExtra(CURRENT_POD_POS, -1);
        Podcast podcast = intent.getParcelableExtra(CURRENT_POD);
        viewModel.setPodcastFromArgs(podcast, pos);

        editingComment = null;
        editingCommentPos = -1;

        findViews();
        setupVolumeSeekbar();

        if (!viewModel.arePodcastsTheSame(mpHolder)) {
            viewModel.playPodcast(mpHolder);
            notificationHelper.notify(this);
        } else {
            initProgress();
        }
        fillData();

        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    private void setFavorite() {
        int drawable = isFavorite ? R.drawable.ic_star_on : R.drawable.ic_star_off;
        favoriteBtn.setImageResource(drawable);
    }

    private void setLike() {
        int drawable = isLiked ? R.drawable.ic_heart_on : R.drawable.ic_heart_off;
        likeBtn.setImageResource(drawable);
    }

    private void setupVolumeSeekbar() {

//        TODO add listener for volume change
        seekbarVolume.setMax(soundHelper.getMaxVolume());
        seekbarVolume.setProgress(soundHelper.getCurrentVolume());

        seekbarVolume.setOnSeekBarChangeListener(
                (OnSeekBarChangeListenerAdapter) (seekBar, progress, fromUser) -> {
                    if (fromUser)
                        soundHelper.setVolume(progress);
                });
    }

    private void findViews() {

        timeSeekbar = findViewById(R.id.seekBar);
        seekbarVolume = findViewById(R.id.seekBar_volume);
        playPauseBtn = findViewById(R.id.player_togglePlayBtn);
        currentTime = findViewById(R.id.currentTimeTv);
        playerPb = findViewById(R.id.playerProgressBar);
        commentsPb = findViewById(R.id.comments_pb);
        sumTime = findViewById(R.id.sumTimeTv);

        descriptionBox = findViewById(R.id.music_description_box);
        contentTv = findViewById(R.id.contentText);
        dateTv = findViewById(R.id.datePlayerTV);
        playlistCountTv = findViewById(R.id.playlistCountTv);

        likeAmountTv = findViewById(R.id.tvLikeAmount);
        likeBtn = findViewById(R.id.ivBtnLike);
        favoriteBtn = findViewById(R.id.favoriteBtn);

        commentsRv = findViewById(R.id.comments_rv);
        viewModel.setCommentsAdapter(commentsRv, this);

        commentET = findViewById(R.id.comment_et);
        ImageButton commentBtn = findViewById(R.id.comment_btn);
        commentBtn.setEnabled(false);

        commentET.addTextChangedListener((OnTextChangedAdapter) txt -> {
            boolean isValid = !txt.toString().trim().isEmpty();
            commentBtn.setEnabled(isValid);
        });
        commentET.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEND) return false;

            if (commentET.getText().toString().trim().isEmpty()) {
                commentET.setError("Write something before posting");
                commentET.setText(null);
                return true;
            }
            submitComment();
            return false;
        });

        int[] btnIds = {R.id.up_volume_btn, R.id.down_volume_btn,
                R.id.previousBtn, R.id.nextBtn, R.id.rewind_30Btn,
                R.id.forward_30Btn, R.id.shareBtn};
        for (int id : btnIds) findViewById(id).setOnClickListener(this);

        View[] buttons = {playPauseBtn, likeBtn, favoriteBtn, commentBtn};
        for (View btn : buttons) btn.setOnClickListener(this);
    }

    private void submitComment() {

        String commentContent = commentET.getText().toString().trim();
        commentET.setText(null);
        commentET.setError(null);

        if (!viewModel.isUserLogged()) {
            authHelper.showLoginDialog(this);
            return;
        }

        if (editingCommentPos == -1 || editingComment == null) {
            int lastIndex = viewModel.postComment(commentContent);
            commentsRv.scrollToPosition(lastIndex);
            return;
        }

        showProgress();
        viewModel.updateComment(editingComment, editingCommentPos, commentContent)
                .addOnFailureListener(e -> ErrorDialog.showError(this, e))
                .addOnSuccessListener(aVoid -> editingComment.setContent(commentContent))
                .addOnCompleteListener(task -> {
                    hideProgress();
                    editingCommentPos = -1;
                    editingComment = null;
                });
    }

    private void fillData() {

        boolean isLoading = viewModel.isPodcastLoading();
        playPauseBtn.setVisibility(isLoading ? GONE : VISIBLE);
        playerPb.setVisibility(isLoading ? VISIBLE : GONE);

        togglePlayPauseView();

        Podcast podcast = viewModel.currentPodcast;
        contentTv.setText(podcast.getDescription());
        playlistCountTv.setText(viewModel.getPodPositionString());
        dateTv.setText(viewModel.getPodcastDateString());
        likeAmountTv.setText(String.valueOf(viewModel.getPodLikesAmount()));

        setTitle(podcast.getDescription());

        if (descriptionBox.isAttachedToWindow())
            circleContentTv();

        isFavorite = viewModel.isUsersFavorite();
        setFavorite();

        isLiked = viewModel.doesUserLikePodcast();
        setLike();
    }

    private void togglePlayPauseView() {
        playPauseBtn.setState(viewModel.isPodcastPlaying() ? END : START, true);
    }

    private void updateCurrentTime() {
        int currentPosition = mpHolder.getCurrentMiliPosition();
        currentTime.setText(durationToTime(currentPosition));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSeekbarUpdateHandler != null) {
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSeekbarUpdateHandler != null) {
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, SEEKBAR_REFRESH_RATE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mpHolder.removeOnPreparedListener(getClass());
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        mpHolder.addOnPreparedListener(getClass(), this);
        this.registerReceiver(broadcastReceiver,
                broadcastReceiver.getIntentFilter());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        fillData();
        initProgress();
        Context context = this;
        if (context == null) {
            return;
        }
        notificationHelper.notify(context);
    }

    private void initProgress() {
        int duration = mpHolder.getDuration();
        timeSeekbar.setMax(duration);

        mSeekbarUpdateHandler = new Handler();
        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, SEEKBAR_REFRESH_RATE);


        mUpdateSeekbar = new Runnable() {
            @Override
            public void run() {
                timeSeekbar.setProgress(mpHolder.getCurrentMiliPosition());
                updateCurrentTime();
                mSeekbarUpdateHandler.postDelayed(this, SEEKBAR_REFRESH_RATE);
            }
        };
        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);

        updateCurrentTime();
        timeSeekbar.setOnSeekBarChangeListener((OnSeekBarChangeListenerAdapter)
                (seekBar, progress, fromUser) -> {
                    if (fromUser) {
                        updateCurrentTime();
                        mpHolder.seekTo(progress);
                    }
                });

        sumTime.setText(durationToTime(duration));

        playerPb.setVisibility(GONE);
        playPauseBtn.setVisibility(VISIBLE);
    }

    private void dismissKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private String durationToTime(int mili) {
        long hours = TimeUnit.MILLISECONDS.toHours(mili) % TimeUnit.DAYS.toHours(1);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mili) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mili) % TimeUnit.MINUTES.toSeconds(1);

        Locale locDef = Locale.getDefault();
        if (hours == 0) {
            return String.format(locDef, "%02d:%02d", minutes, seconds);
        }

        return String.format(locDef, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void circleContentTv() {

        // get the center for the clipping circle
        int cx = descriptionBox.getWidth() / 2;
        int cy = descriptionBox.getHeight() / 2;

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(descriptionBox, cx, cy,
                0f, finalRadius);
        anim.start();
    }

    private void playNext() {
        viewModel.playNext(mpHolder);
        fillData();
    }

    private void playPrevious() {
        viewModel.playPrevious(mpHolder);
        fillData();
    }

    private void seekBy(int seconds) {
        mpHolder.seekBy(seconds);
        updateProgress();
    }

    private void updateProgress() {
        timeSeekbar.setProgress(mpHolder.getProgress());
        updateCurrentTime();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.player_togglePlayBtn:
                viewModel.playPodcast(mpHolder);
                notificationHelper.notify(this);
                break;

            case R.id.previousBtn:
                playPrevious();
                notificationHelper.notify(this);
                break;
            case R.id.nextBtn:
                playNext();
                notificationHelper.notify(this);
                break;

            case R.id.rewind_30Btn:
                seekBy(-30);
                break;
            case R.id.forward_30Btn:
                seekBy(30);
                break;

            case R.id.down_volume_btn:
                seekbarVolume.setProgress(soundHelper.lower());
                break;
            case R.id.up_volume_btn:
                seekbarVolume.setProgress(soundHelper.raise());
                break;

            case R.id.ivBtnLike:
                if (viewModel.isUserLogged()) {
                    toggleHeart();
                } else {
                    authHelper.showLoginDialog(this);
                }
                break;

            case R.id.favoriteBtn:
                //if a guest
                if (viewModel.isUserLogged()) {
                    toggleStar();
                } else {
                    authHelper.showLoginDialog(this);
                }
                break;

            case R.id.shareBtn:
                startActivity(viewModel.getShareIntent(getResources()));
                break;

            case R.id.comment_btn:
                submitComment();
                dismissKeyboard();
                break;

        }
    }

    private void toggleHeart() {
        toggleView(likeBtn, isLiked,
                R.drawable.ic_heart_on,
                R.drawable.ic_heart_off);

        likeBtn.setOnClickListener(null);
        isLiked = !isLiked;

        viewModel.updateLike(isLiked)
                .addOnCompleteListener(task -> likeBtn.setOnClickListener(this))
                .addOnFailureListener(e -> {
                    long podLikes = viewModel.getPodLikesAmount();
                    likeAmountTv.setText(String.valueOf(podLikes));
                    isLiked = !isLiked;
                });

        long podLikes = viewModel.getPodLikesAmount();
        if (isLiked) podLikes++;
        else podLikes--;

        likeAmountTv.setText(String.valueOf(podLikes));
    }

    private void toggleStar() {
        toggleView(favoriteBtn, isFavorite,
                R.drawable.ic_star_on,
                R.drawable.ic_star_off);

        favoriteBtn.setOnClickListener(null);

        viewModel.updateFavorite(!isFavorite)
                .addOnCompleteListener(task -> favoriteBtn.setOnClickListener(this))
                .addOnSuccessListener(aVoid -> {
                    isFavorite = !isFavorite;
                    toast(isFavorite ? R.string.added_to_fav : R.string.removed_fav);
                });
    }

    private void toggleView(ImageView view, boolean isOn,
                            @DrawableRes int onImg, @DrawableRes int offImg) {
        Animation pop = AnimationUtils.loadAnimation(this, R.anim.pop);
        view.startAnimation(pop);
        view.setImageResource(isOn ? offImg : onImg);
    }

    @Override
    public void onItemClicked(Comment comment, int pos) {
        toast(String.format("%s %s", comment.getUserEmail(), comment.getContent()));
    }

    @Override
    public void onRemoveClicked(Comment comment, int pos) {
        showProgress();
        viewModel.remove(comment, pos)
                .addOnCompleteListener(task -> hideProgress())
                .addOnFailureListener(e -> ErrorDialog.showError(this, e));
    }


    @Override
    public void onItemEdit(Comment comment, int pos) {
        editingComment = comment;
        editingCommentPos = pos;
        String content = comment.getContent();
        commentET.setText(content);
        commentET.setSelection(content.length());
        commentET.requestFocus();

        InputMethodManager inputMethodManager =
                (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                commentET.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideProgress() {
        commentsPb.setVisibility(GONE);
    }

    private void showProgress() {
        commentsPb.setVisibility(VISIBLE);
    }

    @Override
    public void onNotificationPlay() {
        togglePlayPauseView();
    }

    @Override
    public void onNotificationPause() {
        togglePlayPauseView();
    }

    @Override
    public void onNotificationNext() {
        fillData();
    }

    @Override
    public void onNotificationPrevious() {
        fillData();
    }

    @Override
    public void onNotificationSeekForward() {
        updateProgress();
    }

    @Override
    public void onNotificationReplay() {
        updateProgress();
    }

    private void toast(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
