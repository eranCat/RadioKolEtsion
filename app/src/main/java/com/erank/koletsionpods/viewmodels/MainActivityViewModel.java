package com.erank.koletsionpods.viewmodels;

import android.app.Application;
import android.media.MediaPlayer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.erank.koletsionpods.adapters.PodcastAdapter;
import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.utils.listeners.OnPodcastClickListener;
import com.wnafee.vector.MorphButton;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.wnafee.vector.MorphButton.MorphState.END;
import static com.wnafee.vector.MorphButton.MorphState.START;

public class MainActivityViewModel extends AndroidViewModel {


    private List<Podcast> podcasts;
    public Podcast playingPodcast;
    public int playingPodPosition = -1;
    private PodcastsDataSource podDataSource;
    private MediaPlayer.OnPreparedListener onPreparedListener;


    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        podDataSource = PodcastsDataSource.getInstance();
        podcasts = podDataSource.getPodcasts();
    }

    public void setPlayingPodcast(Podcast podcast, int position) {
        playingPodcast = podcast;
        playingPodPosition = position;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public boolean hasPodcast() {
        return playingPodcast != null && playingPodPosition > -1;
    }

    public void updateMusicBar(TextView playbarDesc, MorphButton playbar_toggleBtn) {
        if (playingPodcast != null) {
            playbarDesc.setText(playingPodcast.getDescription());
            playbar_toggleBtn.setState(playingPodcast.isPlaying() ? END : START, true);
        }
    }

    public boolean callOnPreparedIfNeeded(MediaPlayer mp) {
        if (playingPodcast == null
                || playingPodPosition == -1
                || onPreparedListener == null) {
            return false;
        }

        onPreparedListener.onPrepared(mp);
        return true;
    }

    public void search(String filter, PodcastAdapter adapter, TextView emptyView) {
        if (filter == null || filter.isEmpty()) {
            adapter.submitList(podcasts);
            emptyView.setVisibility(GONE);
            return;
        }
        List<Podcast> afterFiler = podDataSource.getPodcastsFiltered(filter);

        adapter.submitList(afterFiler);
        emptyView.setVisibility(afterFiler.isEmpty() ? VISIBLE : INVISIBLE);
    }

    public void resetSearch(PodcastAdapter adapter) {
        adapter.submitList(podcasts);
    }

    public PodcastAdapter getNewPodcastsAdapter(OnPodcastClickListener listener) {
        return new PodcastAdapter(podcasts,listener);
    }

    public static MainActivityViewModel newInstance(ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner).get(MainActivityViewModel.class);
    }

}
