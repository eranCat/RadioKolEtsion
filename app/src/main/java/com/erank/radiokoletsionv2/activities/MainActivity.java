package com.erank.radiokoletsionv2.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.account_managers.AccountType;
import com.erank.radiokoletsionv2.fragments.MusicFragment;
import com.erank.radiokoletsionv2.fragments.ProfileFragment;
import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.fragments.podcasts.PodcastsFragment;
import com.erank.radiokoletsionv2.receivers.MediaPlayerReceiver;
import com.erank.radiokoletsionv2.receivers.phone.PhoneReceiver;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerAction;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerHolder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.MorphButton.MorphState;

import java.util.NoSuchElementException;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final int READ_PHONE_STATE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String ACTION_SWAP_FRAGMENT = "swapAction";

    // receivers
    private TelephonyManager telephonyManager;
    private PhoneReceiver phoneReceiver;
    private BroadcastReceiver podcastItemTapReceiver;
    private MediaPlayerReceiver playerReceiver;

    private Toolbar mToolbar;
    private DrawerLayout navigationLayout;

    private MediaPlayerHolder mediaPlayerHolder;
    private View playbar;
    private TextView playbarDesc;
    private MorphButton playbar_PlayPauseBtn;

    private NavigationView navigationView;

    private AlertDialog loginDialog, logoutDialog;

    private FirebaseUser user;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        bindViews();

        initPodcastItemTapReceiver();
        initPlayerReceiver();

        checkInternetConnection();

        setSupportActionBar(mToolbar);

        mediaPlayerHolder = MediaPlayerHolder.getInstance();


        boolean fromIntent = checkOpenedFromIntent();

        checkLogin(fromIntent);

        if (!fromIntent) {
            swapFragment(new PodcastsFragment(), "Podcasts");
        }

        loginDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.login_required_msg)
                .setNegativeButton("Login", (dialog, which) ->
                {
                    login();
//                    startActivity(new Intent(this, LoginActivity.class));
//                    finish();
                })
                .setPositiveButton(R.string.no_guest, (dialog, which) -> {
                }).create();

        logoutDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_logout_msg)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (dialog, which) -> mAuth.signOut())
                .create();
//        TODO if savedState == null create new fragment


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, navigationLayout, mToolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        navigationLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        initDrawer(navigationView);
    }

    private void initPodcastItemTapReceiver() {
        podcastItemTapReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(ACTION_SWAP_FRAGMENT))
                swapFragment(new MusicFragment(), "Music");
                animatePlayBar(false);
            }
        };
    }

    private void initPlayerReceiver() {
        playerReceiver = new MediaPlayerReceiver() {
            @Override
            public void onPlay() {
                Log.d(TAG, "onPlay:  playbar received");
                playbar_PlayPauseBtn.setState(MorphState.END,true);
            }

            @Override
            public void onPause() {
                Log.d(TAG, "onPause:  playbar received");
                playbar_PlayPauseBtn.setState(MorphState.START,true);
            }

            @Override
            public void onSwap() {
                super.onSwap();
                animatePlayBar(false);
            }

            @Override
            public void onPreparing() {
                Log.d(TAG, "onPreparing:  playbar received");
                playbar.setOnClickListener(null);
            }

            @Override
            public void onPrepared() {
                Log.d(TAG, "onPrepared: playbar received");
                playbar.setOnClickListener(v -> {
                    swapFragment(new MusicFragment(), getResources().getString(R.string.music));
                });

                Podcast pod = mediaPlayerHolder.getPodcast();

                playbarDesc.setText(pod.getDescription());

                playbar_PlayPauseBtn.setOnClickListener(v -> mediaPlayerHolder.toggle());
                playbar_PlayPauseBtn.setEnabled(true);

                animatePlayBar(true);
            }
        };
    }

    private void bindViews() {
        mToolbar = findViewById(R.id.my_toolbar);
        playbarDesc = findViewById(R.id.btm_dialog_description);
        playbar_PlayPauseBtn = findViewById(R.id.playPauseBtn);
        playbar = findViewById(R.id.musicBar);
        navigationLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationDrawer);
    }

    private void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: main");

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        manager.registerReceiver(podcastItemTapReceiver, new IntentFilter(ACTION_SWAP_FRAGMENT));

        manager.registerReceiver(playerReceiver, playerReceiver.getFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: main");

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(podcastItemTapReceiver);
        manager.unregisterReceiver(playerReceiver);
    }

    private void initDrawer(NavigationView drawer) {
        if (drawer == null) return;

        Menu menu = drawer.getMenu();
        View headerView = drawer.getHeaderView(0);

        MenuItem logItem = menu.findItem(R.id.nav_logUser);

        if (user != null && !user.isAnonymous()) {

            logItem.setTitle(R.string.signout);
            logItem.setOnMenuItemClickListener(item -> {
                logoutDialog.show();
                return true;
            });

            CircleImageView image = headerView.findViewById(R.id.nav_userProfile);
            Uri url = user.getPhotoUrl();//userPrefs.getString("photoUrl", null);
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_person_dummy)
                    .into(image);

            TextView email = headerView.findViewById(R.id.nav_mail);
            email.setText(user.getEmail());


            TextView name = headerView.findViewById(R.id.nav_name);
            String displayName = user.getDisplayName();
            name.setText(displayName == null || displayName.isEmpty() ? user.getEmail() : displayName);

        } else {
            logItem.setTitle(R.string.login);
            logItem.setOnMenuItemClickListener(item -> {
                login();
                return true;
            });
        }
    }


    private boolean checkOpenedFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return false;


        //TODO check if the intent belongs to deep links
//        if it's the service's intent
        String data = intent.getStringExtra("data");

        if (data != null && data.equals("fromService")) {
            MusicFragment fragment = new MusicFragment();
            swapFragment(fragment, "music");
            return true;
        }


        return false;
    }

    private void checkInternetConnection() {
        if (!isNetworkConnected()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_internet)
                    .setMessage(R.string.no_internet_conn)
                    .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                    .setNegativeButton(R.string.try_again, (dialog, which) -> checkInternetConnection())
                    .show();
        }
    }

    public void checkLogin(boolean fromIntent) {

        user = mAuth.getCurrentUser();

//        first time ever , not even guest
        if (user == null) {
            if (!fromIntent) {
//                startActivity(new Intent(this, LoginActivity.class));
//                finish();
                login();
            } else {
                mAuth.signInAnonymously()
                        .addOnCompleteListener(task -> {
                            user = mAuth.getCurrentUser();
                        });
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void logout() {
//        todo add facebook
        String typeString = getPreferences(0).getString(AccountType.class.getSimpleName(), null);
        if (typeString == null) {
            Toast.makeText(this, R.string.cant_sign_out, Toast.LENGTH_SHORT).show();
            return;
        }

        AccountType type = AccountType.valueOf(typeString);

        switch (type) {
            case FACEBOOK:
                //            FacebookAccountManager.signOutFacebook(this);
                break;
            case GOOGLE:
                mAuth.signOut();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initPhoneReceivers() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE);
            return;
        }
        phoneReceiver = new PhoneReceiver(this);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        telephonyManager.listen(phoneReceiver, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_PHONE_STATE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initPhoneReceivers();
                }
                break;
        }
    }

    public void swapFragment(Fragment fragment, String title) {
        mToolbar.setTitle(title);


        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, fragment);
        if (!(fragment instanceof PodcastsFragment)) {
            transaction.addToBackStack(title);
        }

        transaction.commitAllowingStateLoss();
    }


    private void animatePlayBar(boolean in) {
        if (in && playbar.getVisibility() == View.VISIBLE) return;

        AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (in)
                    playbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!in)
                    playbar.setVisibility(View.GONE);
            }
        };
        playbar.animate()
                .translationY(in ? 1 : -1)
                .setDuration(1200)
                .setListener(animatorListener).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //    the side menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                CustomIntent.customType(this, "left-to-right");
                return true;
//            case android.R.id.home:
//                onBackPressed();
//                return true;
        }
        throw new NoSuchElementException("No item on menu found");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (telephonyManager != null) {
            telephonyManager.listen(phoneReceiver, PhoneStateListener.LISTEN_NONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (navigationLayout.isDrawerOpen(GravityCompat.START)) {
            navigationLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_logUser:
                //done on init drawer
                break;

            case R.id.nav_prefs:
                if (user != null) {
                    if (!user.isAnonymous()) {
                        swapFragment(new ProfileFragment(), user.getDisplayName());
                    } else loginDialog.show();
                } else Log.d(TAG, "onNavigationItemSelected: err with username!");
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
        navigationLayout.closeDrawer(GravityCompat.START);
        return true;
//        throw new IllegalArgumentException("No such item on drawer");
    }
}