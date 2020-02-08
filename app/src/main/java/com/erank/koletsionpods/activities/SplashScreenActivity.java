package com.erank.koletsionpods.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.UserDataSource;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.db.models.User;
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

        connectivity = Connectivity.getInstance();

        checkInternetBeforeContinuing();
    }

    private void checkInternetBeforeContinuing() {
        if (!connectivity.isNetworkConnected(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_internet)
                    .setMessage(R.string.no_internet_conn)
                    .setPositiveButton(R.string.try_again,
                            (dialog, which) -> checkInternetBeforeContinuing())
                    .show();
            return;
        }


        podcastsDataSource = PodcastsDataSource.getInstance();
        podcastsDataSource.loadPodcastsFromFB(this);
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
        Intent mainIntent = new Intent(this, MainActivity.class);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                boolean b = extras.getBoolean(EXTRA_NOTIFICATION, false);
                if (b) {
                    mainIntent.putExtras(extras);
                }
            }
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