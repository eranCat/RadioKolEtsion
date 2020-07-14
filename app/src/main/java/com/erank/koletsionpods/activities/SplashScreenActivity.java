package com.erank.koletsionpods.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.utils.db.PodcastsDataSource;
import com.erank.koletsionpods.utils.db.UserDataSource;
import com.erank.koletsionpods.utils.db.models.Podcast;
import com.erank.koletsionpods.utils.db.models.User;
import com.erank.koletsionpods.utils.Connectivity;
import com.erank.koletsionpods.utils.ErrorDialog;
import com.erank.koletsionpods.utils.helpers.AuthHelper;
import com.erank.koletsionpods.utils.listeners.FBPodcastsCallback;
import com.erank.koletsionpods.utils.listeners.OnUserDataLoadedListener;
import com.erank.koletsionpods.utils.listeners.PodcastsLoadingListener;

import java.util.List;

import static com.erank.koletsionpods.utils.helpers.NotificationHelper.EXTRA_NOTIFICATION;

public class SplashScreenActivity extends AppCompatActivity
        implements PodcastsLoadingListener, FBPodcastsCallback {

    public static final String TAG = SplashScreenActivity.class.getName();
    private Connectivity connectivity;
    private PodcastsDataSource podcastsDataSource;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.splash_loadingBar);
        findViewById(R.id.splashImg).setOnClickListener(v -> {
            Animation pop = AnimationUtils.loadAnimation(this, R.anim.pop);
            v.startAnimation(pop);
        });

        connectivity = Connectivity.getInstance();

        checkInternetBeforeContinuing();
    }

    private void checkInternetBeforeContinuing() {
        if (!connectivity.isNetworkConnected(this)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_internet_conn)
                    .setPositiveButton(R.string.try_again,
                            (dialog, which) -> checkInternetBeforeContinuing())
                    .setNegativeButton(R.string.close,(dialog, which) -> finish())
                    .show();
            return;
        }

        if (getIntent().hasExtra(EXTRA_NOTIFICATION)) {
            if (!AuthHelper.getInstance().isUserLogged(true)) {
                startLoginActivity();
                return;
            }

            startMain(true);
            return;
        }

        podcastsDataSource = PodcastsDataSource.getInstance();
        podcastsDataSource.loadPodcastsFromFB(this);
//        loadFromJson();
    }

    private void loadFromJson() {
        PodcastsDataSource.getInstance()
                .getFromJsonToFirebase(new PodcastsLoadingListener() {
                    @Override
                    public void onLoaded(List<Podcast> data) {
                        SplashScreenActivity.this.onLoaded(data);
                    }

                    @Override
                    public void onCancelled(Exception databaseError) {
                        SplashScreenActivity.this.onCancelled(databaseError);
                    }
                });
    }


    @Override
    public void onLoaded(List<Podcast> podcasts) {
        if (!AuthHelper.getInstance().isUserLogged(true)) {
            startLoginActivity();
            return;
        }

        UserDataSource.getInstance().loadUserData(new OnUserDataLoadedListener() {
            @Override
            public void onLoaded(User user) {

                podcastsDataSource.setPodcastsEditable(user.getId());
                startMain();
            }

            @Override
            public void onCancelled(Exception e) {
                SplashScreenActivity.this.onCancelled(e);
            }
        });
    }

    private void startMain() {
        startMain(false);
    }

    private void startMain(boolean isFromNotification) {
        Intent mainIntent = new Intent(this, PodcastsActivity.class);

        if (isFromNotification) {
            mainIntent.putExtra(EXTRA_NOTIFICATION, true);
            int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
            mainIntent.addFlags(flags);
        }

        startActivity(mainIntent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginSignUpActivity.class));
    }

    @Override
    public void onCancelled(Exception e) {
        ErrorDialog.showError(this, e);
    }

    @Override
    public void onLoading(long count) {
        progressBar.setProgress(0);
        progressBar.setMax(((int) count));
    }

    @Override
    public void onItemLoaded() {
        progressBar.incrementProgressBy(1);
    }
}