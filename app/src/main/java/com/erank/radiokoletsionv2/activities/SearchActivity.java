package com.erank.radiokoletsionv2.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.fragments.podcasts.PodcastAdapter;
import com.erank.radiokoletsionv2.utils.PodcastsDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static List<Podcast> podcastsListFromJson;
    private RecyclerView results;
    private SearchView mSearchView;
    private ProgressBar progressBar;
    private View emptyView;
    private PodcastAdapter podcastAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        results = findViewById(R.id.searchResultsList);
        results.setLayoutManager(new LinearLayoutManager(this));

        emptyView = findViewById(R.id.emptyView);

        progressBar = findViewById(R.id.search_progress);


        podcastAdapter = new PodcastAdapter(this, new ArrayList<>(), false);
        results.setAdapter(podcastAdapter);

        Toolbar searchbar = findViewById(R.id.SearchToolbar);
        setSupportActionBar(searchbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();

        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint));
        setBackButton(searchItem);

        podcastsListFromJson = PodcastsDataHolder.getInstance(
                new SearchActivity.SearchLoading()).getPodcastsList();
        if (podcastsListFromJson != null) {
            initSearchData();
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void setBackButton(MenuItem searchItem) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    onBackPressed();
                    return true; // Return true to collapse action view
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }
            });
        } else {
            // do something for phones running an SDK before froyo
            if (mSearchView == null) {
                mSearchView = (SearchView) searchItem.getActionView();
            }
            mSearchView.setOnCloseListener(() -> {
                onBackPressed();
                return false;
            });
        }
    }

    private void searchByFilter(String filter) {
        List<Podcast> afterFiler = new ArrayList<>();

        if (filter != null && !filter.isEmpty()) {

            for (Podcast podcast : podcastsListFromJson) {
                if (podcast.getDescription().toLowerCase().contains(filter.toLowerCase()))
                    afterFiler.add(podcast);
            }
        }//else will stay empty list ,which will show all from adapter

        podcastAdapter.updateDataList(afterFiler);

        if (!afterFiler.isEmpty()) {
            results.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.INVISIBLE);
        } else {
            results.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String filter) {
        searchByFilter(filter);
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, "right-to-left");
    }

    private void initSearchData() {
        progressBar.setVisibility(View.INVISIBLE);
        mSearchView.setOnQueryTextListener(SearchActivity.this);
        searchByFilter(null);//show all
    }

    private class SearchLoading implements PodcastsDataHolder.FBPodcastsCallback /*JsonParser.PodcastsLoadingListener*/ {
        @Override
        public void onLoading(long dataCount) {
            progressBar.setVisibility(View.VISIBLE);
            mSearchView.setOnQueryTextListener(null);
        }

        @Override
        public void onLoaded(List<Podcast> podcastsList) {
            podcastsListFromJson = podcastsList;
            initSearchData();
        }
    }
}