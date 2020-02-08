package com.erank.koletsionpods.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.erank.koletsionpods.R;
import com.erank.koletsionpods.adapters.PodcastAdapter;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.receivers.NotificationActionReceiver;
import com.erank.koletsionpods.utils.helpers.AuthHelper;
import com.erank.koletsionpods.utils.helpers.NotificationHelper;
import com.erank.koletsionpods.utils.listeners.OnPodcastClickListener;
import com.erank.koletsionpods.utils.listeners.PodcastsNotificationActionCallback;
import com.erank.koletsionpods.viewmodels.MainActivityViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.wnafee.vector.MorphButton;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.erank.koletsionpods.utils.helpers.NotificationHelper.EXTRA_NOTIFICATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        MenuItem.OnActionExpandListener,
        MediaPlayer.OnPreparedListener,
        OnPodcastClickListener,
        SearchView.OnQueryTextListener,
        PodcastsNotificationActionCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private DrawerLayout navigationLayout;
    private NavigationView navigationView;

    private RecyclerView rvPodcasts;
    private PodcastAdapter adapter;
    private MorphButton musicBarToggleBtn;
    private ProgressBar musicBarProgressBar;
    private View musicBar;
    private TextView musicBarDesc;
    private TextView emptyView;

    private AuthHelper authHelper;
    private MediaPlayerHelper mpHelper;
    private MainActivityViewModel viewModel;

    private NotificationActionReceiver broadcastReceiver =
            new NotificationActionReceiver(this);
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authHelper = AuthHelper.getInstance();
        mpHelper = MediaPlayerHelper.getInstance();
        viewModel = MainActivityViewModel.newInstance(this);
        notificationHelper = NotificationHelper.getInstance(this);

        findViews();
        adapter = viewModel.getNewPodcastsAdapter(this);
        rvPodcasts.setAdapter(adapter);

        musicBar.setOnClickListener(this);
        musicBarToggleBtn.setOnClickListener(this);

        updateComponents();

        registerNotificationReceiver();

        setSupportActionBar(mToolbar);

        initDrawer();

        Intent intent = getIntent();
        if (intent != null) {

            if (!intent.getBooleanExtra(EXTRA_NOTIFICATION, false))
                return;

//            get data
            int pos = mpHelper.getCurrentPodcastPosition();
            if (pos==-1)return;

            Podcast podcast = mpHelper.getCurrentPodcast();
            if (podcast == null) return;
//            call open podcast activity with data
            openPodcastActivity(podcast, pos);
//            Toast.makeText(this, podcast.getDescription(), Toast.LENGTH_LONG).show();
        }
    }

    private void findViews() {
        mToolbar = findViewById(R.id.my_toolbar);
        navigationLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationDrawer);

        rvPodcasts = findViewById(R.id.rvPlayList);
        musicBar = findViewById(R.id.musicBar);
        musicBarToggleBtn = findViewById(R.id.musicbar_morphBtn);
        musicBarDesc = findViewById(R.id.btm_dialog_description);
        musicBarProgressBar = findViewById(R.id.musicbar_progressbar);
        emptyView = findViewById(R.id.emptyView);
    }

    private void initDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, navigationLayout, mToolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        navigationLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        View headerView = navigationView.getHeaderView(0);

        MenuItem logItem = menu.findItem(R.id.nav_logUser);
        FirebaseUser user = authHelper.getCurrentUser();
        if (user == null) {
            logItem.setTitle(R.string.login);
            logItem.setOnMenuItemClickListener(item -> {
                authHelper.openLogin(this);
                return true;
            });
            return;
        }

        logItem.setTitle(R.string.signout);
        logItem.setOnMenuItemClickListener(item -> {
            showSignOutDialog();
            return true;
        });

        CircleImageView image = headerView.findViewById(R.id.nav_userProfile);
        Uri url = user.getPhotoUrl();
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_person_dummy)
                .into(image);

        TextView emailTV = headerView.findViewById(R.id.nav_mail);
        String email = user.getEmail();
        emailTV.setText(email);
        emailTV.setVisibility(email != null ? View.VISIBLE : View.GONE);

        TextView name = headerView.findViewById(R.id.nav_name);
        String displayName = user.getDisplayName();
        if (displayName == null) {
            name.setVisibility(View.INVISIBLE);
        } else {
            name.setVisibility(View.VISIBLE);
            name.setText(displayName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchItem.setOnActionExpandListener(this);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mpHelper.addOnPreparedListener(getClass(), this);
        registerNotificationReceiver();
    }

    private void registerNotificationReceiver() {
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        mpHelper.removeOnPreparedListener(getClass());
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void onResume() {
        super.onResume();
        updateComponents();
    }

    private void updateComponents() {
        Podcast podcast = mpHelper.getCurrentPodcast();
        if (podcast == null) return;

        viewModel.playingPodcast = podcast;
        viewModel.playingPodPosition = mpHelper.getCurrentPodcastPosition();
        updateMusicBar();

        adapter.refreshCurrent();

        ((LinearLayoutManager) rvPodcasts.getLayoutManager())
                .scrollToPositionWithOffset(viewModel.playingPodPosition, 30);
    }

    @Override
    public void onItemClicked(Podcast podcast, int position) {
        viewModel.setPlayingPodcast(podcast, position);
        openPodcastActivity(podcast, position);
    }


    @Override
    public void onTogglePlayPause(Podcast podcast, int position,
                                  MediaPlayer.OnPreparedListener onPreparedListener) {

        viewModel.setPlayingPodcast(podcast, position);
        viewModel.setOnPreparedListener(onPreparedListener);

        mpHelper.playPodcast(podcast, position);
        boolean isLoading = podcast.isLoading();

        musicBarToggleBtn.setVisibility(isLoading ? INVISIBLE : VISIBLE);
        musicBarProgressBar.setVisibility(isLoading ? VISIBLE : INVISIBLE);

        updateMusicBar();

        notificationHelper.notify(this);
    }

    @Override
    public void onClick(View v) {
        if (!viewModel.hasPodcast()) {
            return;
        }

        Podcast podcast = viewModel.playingPodcast;
        int position = viewModel.playingPodPosition;

        switch (v.getId()) {
            case R.id.musicBar:
                openPodcastActivity(podcast, position);
                break;
            case R.id.musicbar_morphBtn:
                mpHelper.playPodcast(podcast, position);
                adapter.refreshCurrent();
                notificationHelper.notify(this);
                break;
        }

    }

    private void openPodcastActivity(Podcast podcast, int position) {
        Intent intent = new Intent(this, PlayerActivity.class)
                .putExtra(PlayerActivity.CURRENT_POD_POS, position)
                .putExtra(PlayerActivity.CURRENT_POD, podcast);

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        startActivity(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!viewModel.callOnPreparedIfNeeded(mp)) {
            return;
        }

        updateMusicBar();
        musicBarToggleBtn.setVisibility(VISIBLE);
        musicBarProgressBar.setVisibility(INVISIBLE);

        notificationHelper.notify(this);
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        viewModel.resetSearch(adapter);
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        adapter.setSearching(true);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        viewModel.search(newText, adapter, emptyView);
        return false;
    }


    @Override
    public void onNotificationPlay() {
        updateComponents();
    }

    @Override
    public void onNotificationPause() {
        updateComponents();
    }

    @Override
    public void onNotificationNext() {
        updateComponents();
    }

    @Override
    public void onNotificationPrevious() {
        updateComponents();
    }

    private void updateMusicBar() {
        viewModel.updateMusicBar(musicBarDesc, musicBarToggleBtn);
    }

    @Override
    public void onBackPressed() {
        if (navigationLayout.isDrawerOpen(GravityCompat.START)) {
            navigationLayout.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_logUser:
                //done on init drawer
                break;

            case R.id.nav_prefs:
                if (authHelper.isUserLogged()) openProfile();
                else showLoginDialog();
                break;

//            case R.id.nav_share:
//                Todo add link getShare app
//                break;

            case R.id.nav_about:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.about_app)
                        .setMessage(R.string.about)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                break;
        }
        navigationLayout.closeDrawer(GravityCompat.START);
        return true;
//        throw new IllegalArgumentException("No such item on drawer");
    }

    private void openProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.login_required_msg)
                .setNegativeButton(R.string.no_guest, null)
                .setPositiveButton("Login",
                        (dialog, which) -> authHelper.openLogin(this))
                .show();
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
//                .setTitle(title)@StringRes int title,
                .setMessage(R.string.confirm_logout_msg)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (dialog, which) -> authHelper.signOut(this))
                .show();
    }
}