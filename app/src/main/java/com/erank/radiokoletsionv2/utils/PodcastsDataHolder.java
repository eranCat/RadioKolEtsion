package com.erank.radiokoletsionv2.utils;

import androidx.annotation.NonNull;

import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.utils.media_player.MediaPlayerHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodcastsDataHolder implements ValueEventListener
        /*,JsonParser.PodcastsLoadingListener*/  {

    public static final String PODCASTS_TABLE_NAME = "Podcasts";
    private static PodcastsDataHolder instance;
    private DatabaseReference podcastsRef;
    private Map<String, Podcast> podcastsMap;
    private FBPodcastsCallback listener;//JsonParser.PodcastsLoadingListener
    private List<Podcast> podcastsList;

    private PodcastsDataHolder() {
        podcastsRef = FirebaseDatabase.getInstance().getReference(PODCASTS_TABLE_NAME);

        refresh();

        podcastsRef.addValueEventListener(this);
    }

    public static PodcastsDataHolder getInstance(/*JsonParser.PodcastsLoadingListener*/FBPodcastsCallback listener) {
        if (instance == null)
            instance = new PodcastsDataHolder();
        instance.listener = listener;
        return instance;
    }

//    @Override
//    public void onLoading() {
//        if (listener != null) {
//            listener.onLoading();
//        }
//    }

//    @Override
//    public void onLoaded(Map<String, Podcast> map, List<Podcast> podcastsList) {
//        if (listener != null)
//            listener.onLoaded(map, podcastsList);
//
//        //todo load likes and favs ? or load when needed
//
//        this.podcastsMap = map;
//        this.podcastsList = podcastsList;
//
//        //update in firebase
//        FirebaseDatabase.getInstance()
//                .getReference(PODCASTS_TABLE_NAME).setValue(podcastsMap);
//    }

    public void refresh() {
//        new JsonParser(this).execute();

        podcastsRef.removeEventListener(this);
        podcastsRef.addValueEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {

            if (listener != null)
                listener.onLoading(dataSnapshot.getChildrenCount());

            if (podcastsList!=null) podcastsList.clear();
            else podcastsList = new ArrayList<>();

            if (podcastsMap != null) podcastsMap.clear();//Idk if necessary
            else podcastsMap = new HashMap<>();

            for (DataSnapshot podcastChild : dataSnapshot.getChildren()) {
                Podcast podcast = podcastChild.getValue(Podcast.class);
                podcastsList.add(podcast);
                podcastsMap.put(podcastChild.getKey(), podcast);
                if(listener!=null)
                    listener.onItemLoaded();
            }

            if (listener != null)
                listener.onLoaded(podcastsList, podcastsMap);

            MediaPlayerHolder.getInstance().setData(podcastsList);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    public Map<String, Podcast> getPodcastsMap() {
        return podcastsMap;
    }

    public List<Podcast> getPodcastsList() {
        return podcastsList;
    }

    public interface FBPodcastsCallback {
        default void onLoading(long dataCount){}
        default void onItemLoaded(){}

        //use this method or the other 2
        default void onLoaded(List<Podcast> podcastsList, Map<String, Podcast> podcastsMap){
            onLoaded(podcastsList);
            onLoaded(podcastsMap);
        }
        void onLoaded(List<Podcast> podcastsList);
        default void onLoaded(Map<String, Podcast> podcastsMap) {

        }
    }
}
