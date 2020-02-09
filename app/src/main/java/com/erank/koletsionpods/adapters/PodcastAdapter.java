package com.erank.koletsionpods.adapters;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.db.PodcastsDataSource;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.utils.enums.PodcastState;
import com.erank.koletsionpods.utils.listeners.OnPodcastClickListener;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.MorphButton.MorphState;

import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class PodcastAdapter extends ListAdapter<Podcast, PodcastAdapter.PodcastHolder> {

    private OnPodcastClickListener listener;
    private boolean isSearching;
    private boolean isRemovable;
    private int currentHolderPosOriginal;
    private int currentHolderPos;

    public PodcastAdapter(@NonNull List<Podcast> podcasts,
                          boolean isRemovable, OnPodcastClickListener listener) {
        super(new PodcastDiffItemCallback());
        submitList(podcasts);
        this.isRemovable = isRemovable;
        this.listener = listener;
        currentHolderPos = currentHolderPosOriginal = -1;
    }

    public PodcastAdapter(List<Podcast> podcastList, OnPodcastClickListener listener) {
        this(podcastList, false, listener);
    }

    //binds the accepted information into the inner class props-> single item props:
    @NonNull
    @Override
    public PodcastHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.podcast_item, parent, false);
        return new PodcastHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastHolder holder, int position) {
        holder.fill(getItem(position));
    }

    public void setPosition(int position) {
        currentHolderPos = position;
    }

    public void refreshCurrent() {
        if (isSearching) {
            if (currentHolderPosOriginal != -1)
                notifyItemChanged(currentHolderPosOriginal);
        } else if (currentHolderPos != -1)
            notifyItemChanged(currentHolderPos);
    }

    public void setSearching(boolean searching) {
        isSearching = searching;
    }

    @Override
    public void submitList(@Nullable List<Podcast> list) {
        if (list!=null && isSearching) {
            if (currentHolderPosOriginal == -1)
                currentHolderPosOriginal = currentHolderPos;

            if (currentHolderPos >= 0 && currentHolderPos < getCurrentList().size()) {
                Podcast podcast = getItem(currentHolderPos);
                currentHolderPos = indexOff(podcast, list);
            }
        }
        super.submitList(list);
    }

    private <T> int indexOff(T p, List<T> list) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).equals(p)) return i;

        return -1;
    }

    //inner class:
    class PodcastHolder extends RecyclerView.ViewHolder {

        private final TextView tvDesc, tvDate, tvLikes;
        private final MorphButton civPlay;
        private final ProgressBar progressBar;
        private final View likesSection;
        private final ImageButton removeBtn;

        //ctor -> bind the props to the single item counterparts:
        PodcastHolder(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            civPlay = itemView.findViewById(R.id.civPlay);

            progressBar = itemView.findViewById(R.id.progressBarLoading);
            tvDate = itemView.findViewById(R.id.dateTv);

            likesSection = itemView.findViewById(R.id.likes_section);
            tvLikes = itemView.findViewById(R.id.pod_likes_tv);

            removeBtn = itemView.findViewById(R.id.removeFromFavBtn);
            removeBtn.setVisibility(isRemovable ? VISIBLE : GONE);
        }

        private CharSequence getDateString(Date date) {
            return DateUtils.getRelativeTimeSpanString(date.getTime());
//            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
//            return dateFormat.format(podcast.getDate());
        }

        void fill(Podcast podcast) {
            int i = !isSearching ? getAdapterPosition() : PodcastsDataSource.getInstance().indexOf(podcast);
            itemView.setOnClickListener(v -> listener.onItemClicked(podcast, i));

            if (isRemovable)
                removeBtn.setOnClickListener(v -> listener.onRemoveClicked(podcast, i));

            civPlay.setOnClickListener(v -> toggle(podcast));

            tvDesc.setText(podcast.getDescription());
            tvDate.setText(getDateString(podcast.getDate()));

            setLook(podcast);

            long likesAmount = podcast.getLikesAmount();
            if (likesAmount <= 0) {
                likesSection.setVisibility(GONE);
                return;
            }
            likesSection.setVisibility(VISIBLE);
            tvLikes.setText(getFormattedLikes(likesAmount));
        }

        @SuppressLint("DefaultLocale")
        private String getFormattedLikes(long number) {
            if (number < 1000)
                return String.valueOf(number);

            int exp = (int) (Math.log(number) / Math.log(1000));
            char[] kmgtpe = {'K', 'M', 'G', 'T', 'P', 'E'};
            return String.format("%.1f %c", number / Math.pow(1000, exp), kmgtpe[exp - 1]);
        }

        private void toggle(Podcast podcast) {
            listener.onTogglePlayPause(podcast, getAdapterPosition());

            if (currentHolderPos != -1 && podcast.isLoading()) {
                notifyItemChanged(currentHolderPos);
            }

            currentHolderPos = getAdapterPosition();
            setLook(podcast);
        }

        private void setLook(Podcast podcast) {
            progressBar.setVisibility(GONE);
            civPlay.setVisibility(VISIBLE);

            if (podcast.state == PodcastState.DEFAULT) {
                setBgColor(false);
                togglePlayBtnState(false, false);
                return;
            }

            if (podcast.isLoading()) {
                civPlay.setVisibility(GONE);
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            setBgColor(true);
            if (podcast.isPrepared()) {
                togglePlayBtnState(false);
                return;
            }

            togglePlayBtnState(podcast.isPlaying());
        }

        private void setBgColor(boolean isActive) {
            int color = isActive ? R.color.podcast_playing_color :
                    R.color.podcast_normal_color;

            int c = itemView.getContext().getResources().getColor(color);
            ((CardView) itemView).setCardBackgroundColor(c);
        }

        private void togglePlayBtnState(boolean isPlaying) {
            togglePlayBtnState(isPlaying, true);
        }

        private void togglePlayBtnState(boolean isPlaying, boolean shouldAnimate) {
            MorphState state = isPlaying ? MorphState.END : MorphState.START;
            civPlay.setState(state, shouldAnimate);
        }
    }
}