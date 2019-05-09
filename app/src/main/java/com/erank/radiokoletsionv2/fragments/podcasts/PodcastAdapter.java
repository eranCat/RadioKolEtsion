package com.erank.radiokoletsionv2.fragments.podcasts;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.activities.MainActivity;
import com.erank.radiokoletsionv2.activities.SearchActivity;
import com.erank.radiokoletsionv2.utils.UserDataHolder;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerHolder;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastHolder> {

    private boolean isRemovable;
    private Context context;
    private List<Podcast> podcastList;
    private List<Podcast> podcastListFull;

    private MediaPlayerHolder mediaPlayer;

    private PodcastHolder currentHolder;

    private DatabaseReference usersTableRef;
    private FirebaseUser user;


    //RV ctor -> used to get the properties that will be inserted into the RV layout.
    PodcastAdapter(Context context, List<Podcast> podcastList) {
        this(context, podcastList, false);
    }

    public PodcastAdapter(Context context, List<Podcast> podcastList, boolean removable) {
        this.podcastList = podcastList;
        this.podcastListFull = new ArrayList<>(podcastList);
        this.context = context;

        this.mediaPlayer = MediaPlayerHolder.getInstance();
        context.startService(new Intent(context, MediaPlayerService.class));

        currentHolder = null;

        this.isRemovable = removable;
        if (isRemovable) {//probably the favorites list
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && !user.isAnonymous()) {
                usersTableRef = FirebaseDatabase.getInstance()
                        .getReference(UserDataHolder.USERS_TABLE_NAME);
            }
        }
    }

    private boolean handlePodcast(PodcastHolder holder) {

        boolean swapped = mediaPlayer.playPodcast(holder.positionInList);
        if (!swapped)
            return false;

        if (currentHolder != null) {
            currentHolder.reset();
        }
        currentHolder = holder;

        mediaPlayer.setOnPreparedListener(mp -> {
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.playBtn.setVisibility(View.VISIBLE);
            holder.pauseBtn.setVisibility(View.VISIBLE);
        });

        return true;
    }

    private void openPlayerFragment(PodcastHolder holder) {
        if (!handlePodcast(holder))
            mediaPlayer.play();

        Intent intent = new Intent(MainActivity.ACTION_SWAP_FRAGMENT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        if (context instanceof SearchActivity) {
            ((SearchActivity) context).finish();
        }
    }


    //binds the accepted information into the inner class props-> single item props:
    @NonNull
    @Override
    public PodcastHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ref to the inflater obj:
        LayoutInflater inflater = LayoutInflater.from(context);

        //inflate the single item
        View view = inflater.inflate(R.layout.podacast_item, parent, false);

        //init the RV holder:
        //return the holder
        return new PodcastHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull PodcastHolder holder, int position) {

        if (isRemovable) {
            holder.removeBtn.setVisibility(View.VISIBLE);
        }

        holder.positionInList = position;

        //pod class obj from the podcastList(index)
        Podcast podcast = podcastList.get(position);
        holder.podcast = podcast;
        //DESCRIPTION listener:

        holder.tvDesc.setText(podcast.getDescription());

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE - yy/MM/dd", Locale.getDefault());
        holder.tvDate.setText(formatter.format(podcast.getDate()));
    }


    //size of the list:

    @Override
    public int getItemCount() {
        return podcastList.size();
    }

    public void updateDataList(List<Podcast> podcastList) {
        if (podcastList == null || podcastList.isEmpty())
            this.podcastList = podcastListFull;
        else
            this.podcastList = podcastList;
        notifyDataSetChanged();
    }

    private void togglePlayPause(@NonNull PodcastHolder holder) {
        if (mediaPlayer.isPlaying()) {
            holder.playBtn.setVisibility(View.INVISIBLE);
            holder.pauseBtn.setVisibility(View.VISIBLE);
        } else {
            holder.playBtn.setVisibility(View.VISIBLE);
            holder.pauseBtn.setVisibility(View.INVISIBLE);
        }
    }

    //inner class:
    class PodcastHolder extends RecyclerView.ViewHolder {

        //props:
        TextView tvDesc;
        TextView tvDate;
        //        MorphButton civPlay; // checked ? pause(end): play(start)
        ImageButton playBtn, pauseBtn;
        ProgressBar progressBar;

        ImageButton removeBtn;

        Podcast podcast;

        int positionInList;

        //ctor -> bind the props to the single item counterparts:
        public PodcastHolder(@NonNull View itemView) {
            super(itemView);

            tvDesc = itemView.findViewById(R.id.tvDesc);
//            civPlay = itemView.findViewById(R.id.civPlay);
            playBtn = itemView.findViewById(R.id.PlayImgBtn);
            pauseBtn = itemView.findViewById(R.id.PauseImgBtn);

            progressBar = itemView.findViewById(R.id.progressBarLoading);
            tvDate = itemView.findViewById(R.id.dateTv);

            removeBtn = itemView.findViewById(R.id.removeFromFavBtn);

            playBtn.setOnClickListener(v -> handleClick(this));
            pauseBtn.setOnClickListener(v -> mediaPlayer.pause());
            removeBtn.setOnClickListener(v -> {
                if (usersTableRef != null) {
                    removeFavorite(podcast, user);
                }
            });

            itemView.setOnClickListener(v -> openPlayerFragment(this));
        }

        private void removeFavorite(Podcast podcast, FirebaseUser user) {
            if (user == null) return;

            Task<Void> changeFavoritesTask = usersTableRef.child(user.getUid())
                    .child("favorites")
                    .child(podcast.getId()).setValue(null);

            ProgressDialog progressDialog = new ProgressDialog(itemView.getContext());
            progressDialog.setTitle("Updating favorites");
            progressDialog.setMessage("Removing from favorites, please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            changeFavoritesTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    PodcastAdapter podcastAdapterRef = PodcastAdapter.this;
                    podcastAdapterRef.podcastList.remove(podcast);
                    podcastAdapterRef.podcastListFull.remove(podcast);
                    podcastAdapterRef.notifyDataSetChanged();
                    Toast.makeText(itemView.getContext(), "Removed from favorites", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(itemView.getContext(),
                            task.getException().getLocalizedMessage()
                            , Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            });
        }


        void reset() {
            progressBar.setVisibility(View.INVISIBLE);
            playBtn.setVisibility(View.VISIBLE);
            pauseBtn.setVisibility(View.VISIBLE);
        }

        void handleClick(@NonNull PodcastHolder holder) {
            if (handlePodcast(holder)) {//if swapped
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.playBtn.setVisibility(View.INVISIBLE);
                holder.pauseBtn.setVisibility(View.INVISIBLE);
            }
        }
    }
}
