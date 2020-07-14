package com.erank.koletsionpods.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import com.erank.koletsionpods.utils.db.PodcastsDataSource;
import com.erank.koletsionpods.utils.db.UserDataSource;
import com.erank.koletsionpods.utils.db.models.Podcast;
import com.erank.koletsionpods.utils.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.receivers.NotificationActionReceiver;
import com.erank.koletsionpods.utils.helpers.AuthHelper;
import com.erank.koletsionpods.utils.helpers.NotificationHelper;
import com.erank.koletsionpods.utils.listeners.OnPodcastClickListener;
import com.erank.koletsionpods.utils.listeners.PodcastsNotificationActionCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.erank.koletsionpods.utils.helpers.NotificationHelper.EXTRA_NOTIFICATION;

public class PodcastsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MenuItem.OnActionExpandListener,
        MediaPlayer.OnPreparedListener,
        OnPodcastClickListener,
        SearchView.OnQueryTextListener,
        PodcastsNotificationActionCallback {

    private static final String TAG = PodcastsActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private RecyclerView rvPodcasts;
    private PodcastAdapter adapter;
    private TextView emptyView;

    private AuthHelper authHelper;
    private MediaPlayerHelper mpHelper;

    private NotificationActionReceiver broadcastReceiver =
            new NotificationActionReceiver(this);
    private NotificationHelper notificationHelper;
    private LinearLayoutManager rvPodcastsLayoutManager;

    private PodcastsDataSource podDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);

        authHelper = AuthHelper.getInstance();
        mpHelper = MediaPlayerHelper.getInstance();
        notificationHelper = NotificationHelper.getInstance(this);

        findViews();

        podDataSource = PodcastsDataSource.getInstance();

        adapter = new PodcastAdapter(podDataSource.getPodcasts(),this);
        rvPodcasts.setAdapter(adapter);
        rvPodcastsLayoutManager = (LinearLayoutManager) rvPodcasts.getLayoutManager();

        updateComponents();

        registerNotificationReceiver();

        initDrawer();

        Intent intent = getIntent();
        if (intent != null) {

            if (!intent.getBooleanExtra(EXTRA_NOTIFICATION, false))
                return;

//            get data
            int pos = mpHelper.getCurrentPodcastPosition();
            if (pos == -1) return;

            Podcast podcast = mpHelper.getCurrentPodcast();
            if (podcast == null) return;

            openPlayerActivity(podcast.getId());
        }
    }

    private void findViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        navigationView = findViewById(R.id.navigationDrawer);
        rvPodcasts = findViewById(R.id.rvPodcasts);
        emptyView = findViewById(R.id.emptyView);
    }

    private void initDrawer() {
        drawer = findViewById(R.id.drawerLayout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
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
                .placeholder(R.drawable.dog_face)
                .into(image);

        TextView emailTV = headerView.findViewById(R.id.nav_mail);
        String email = user.getEmail();
        emailTV.setText(email);
        emailTV.setVisibility(email != null ? View.VISIBLE : View.GONE);

        TextView nameTv = headerView.findViewById(R.id.nav_name);
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            String name = UserDataSource.getInstance().getCurrentUser().getName();
            nameTv.setText(name);
        } else {
            nameTv.setText(displayName);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
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
        mpHelper.addOnPreparedListener(this);
        registerNotificationReceiver();
    }

    private void registerNotificationReceiver() {
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        mpHelper.removeOnPreparedListener(this);
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void onResume() {
        super.onResume();
        updateComponents();
    }

    private void updateComponents() {
        Podcast podcast = mpHelper.getCurrentPodcast();
        int position = mpHelper.getCurrentPodcastPosition();

        if (podcast == null || position == -1) return;

        adapter.setPosition(position);
        adapter.refreshCurrent();

        rvPodcastsLayoutManager.scrollToPositionWithOffset(position, 30);
    }

    @Override
    public void onItemClicked(Podcast podcast, int position) {
        openPlayerActivity(podcast.getId());
    }


    @Override
    public void onTogglePlayPause(Podcast podcast, int position) {
        mpHelper.playPodcast(podcast, position);
        notificationHelper.notify(this);
    }

    private void openPlayerActivity(String podId) {
        Intent intent = new Intent(this, PlayerActivity.class)
                .putExtra(PlayerActivity.CURRENT_POD_ID, podId);

        startActivity(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        notificationHelper.notify(this);
        adapter.refreshCurrent();
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        adapter.submitList(podDataSource.getPodcasts());
        adapter.setSearching(false);
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
    public boolean onQueryTextChange(String filter) {
        if (filter == null || filter.isEmpty()) {
            adapter.submitList(podDataSource.getPodcasts());
            emptyView.setVisibility(GONE);
            return false;
        }
        List<Podcast> afterFiler = podDataSource.getPodcastsFiltered(filter);

        adapter.submitList(afterFiler);
        emptyView.setVisibility(afterFiler.isEmpty() ? VISIBLE : INVISIBLE);

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
//                Todo add link share app
//                break;

            case R.id.nav_about:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.about_app)
                        .setMessage(R.string.about)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
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