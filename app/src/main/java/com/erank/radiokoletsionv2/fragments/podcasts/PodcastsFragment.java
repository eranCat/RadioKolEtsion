package com.erank.radiokoletsionv2.fragments.podcasts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.utils.PodcastsDataHolder;

import java.util.List;
import java.util.Map;

public class PodcastsFragment extends Fragment {

    private RecyclerView rvPodcasts;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_podacasts_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPodcasts = view.findViewById(R.id.rvPlayList);

        progressBar = view.findViewById(R.id.progressBarList);

        PodcastLoadingAdapter loadingAdapter = new PodcastLoadingAdapter();
        List<Podcast> podcastList = PodcastsDataHolder
                .getInstance(loadingAdapter).getPodcastsList();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            PodcastsDataHolder.getInstance(loadingAdapter).refresh();
        });
        if (podcastList != null) {
            initData(podcastList, getContext());
        }

    }

    private void initData(List<Podcast> podcastsList, Context context) {
        PodcastAdapter adapter = new PodcastAdapter(context, podcastsList);

        rvPodcasts.setLayoutManager(new LinearLayoutManager(context));
        rvPodcasts.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);

        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);
    }

    class PodcastLoadingAdapter implements PodcastsDataHolder.FBPodcastsCallback/*JsonParser.PodcastsLoadingListener*/ {

        @Override
        public void onLoading(long dataCount) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                activity.runOnUiThread(() ->
                {
                    progressBar.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setEnabled(false);
                });
            }
        }

        @Override
        public void onLoaded(List<Podcast> podcastsList) {
            initData(podcastsList, getContext());
        }
    }
}
