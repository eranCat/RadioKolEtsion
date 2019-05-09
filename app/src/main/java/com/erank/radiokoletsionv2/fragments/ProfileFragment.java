package com.erank.radiokoletsionv2.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.activities.MainActivity;
import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.fragments.podcasts.PodcastAdapter;
import com.erank.radiokoletsionv2.utils.UserDataHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ProfileFragment";
    private TextView usernameTv;
    private TextView emailTv;
    private ImageView profileImage;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView favsRv;
    private PodcastAdapter podcastAdapter;
    private View noFavsTv;

    private FirebaseUser user;
    private UserDataHolder userDataRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameTv = view.findViewById(R.id.tv_username);
        emailTv = view.findViewById(R.id.tv_email);
        profileImage = view.findViewById(R.id.profilePic);


        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        favsRv = view.findViewById(R.id.rvFavorites);
        noFavsTv = view.findViewById(R.id.noFavsTv);

        userDataRef = UserDataHolder.getInstance();

        loadData();

        setBackToolbarButton(true);

        swipeRefreshLayout.setOnRefreshListener(ProfileFragment.this);
    }

    private void loadData() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.isAnonymous()) return;

        emailTv.setText(user.getEmail());
        usernameTv.setText(user.getDisplayName());

        Glide.with(getContext())
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.ic_person_dummy)
                .into(profileImage);

        userDataRef.reloadFavorites(podcastList -> {
            podcastAdapter = new PodcastAdapter(ProfileFragment.this.getContext(),
                    podcastList, true);
            loadFavorites(podcastList);
        });
    }

    private void loadFavorites(List<Podcast> podcastList) {

        swipeRefreshLayout.setVisibility(View.VISIBLE);
        noFavsTv.setVisibility(View.INVISIBLE);

        podcastAdapter.updateDataList(podcastList);
        favsRv.setAdapter(podcastAdapter);
        favsRv.setLayoutManager(new LinearLayoutManager(getContext()));
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
        AppCompatActivity activity = (AppCompatActivity) getContext();

        if (activity == null) {
            Log.d(TAG, "reactivateHamburger: activity was null error");
//            TODO fix!!
            return;
        }

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


    @Override
    public void onRefresh() {
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
        userDataRef.reloadFavorites(this::loadFavorites);
        swipeRefreshLayout.setEnabled(true);
    }
}
