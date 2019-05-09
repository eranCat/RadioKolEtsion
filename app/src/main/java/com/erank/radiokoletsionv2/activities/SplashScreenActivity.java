package com.erank.radiokoletsionv2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.utils.PodcastsDataHolder;

import java.util.List;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity
        implements Animation.AnimationListener, PodcastsDataHolder.FBPodcastsCallback /*, JsonParser.PodcastsLoadingListener*/ {
    private FrameLayout layout;
    private ProgressBar progressBar;//horizontal fillable bar ====>-------
    //TODO add listener to progress of downloading

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.splash_loadingBar);

        PodcastsDataHolder.getInstance(this);
    }

    @Override
    public void onLoading(long dataCount) {
        progressBar.setProgress(0);
        progressBar.setMax(Long.valueOf(dataCount).intValue());
    }

    @Override
    public void onItemLoaded() {
        progressBar.incrementProgressBy(1);
    }

    @Override
    public void onLoaded(List<Podcast> podcastsList) {
        Animation fadeOut = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.fade_out_animation);
        fadeOut.setAnimationListener(SplashScreenActivity.this);

        layout = findViewById(R.id.splashActivity_layout);
        layout.startAnimation(fadeOut);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        layout.setVisibility(View.GONE);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
