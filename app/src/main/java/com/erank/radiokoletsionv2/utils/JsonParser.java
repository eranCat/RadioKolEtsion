package com.erank.radiokoletsionv2.utils;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;

import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.erank.radiokoletsionv2.utils.PodcastsDataHolder.PODCASTS_TABLE_NAME;

public class JsonParser extends AsyncTask<Void, Void, Map<String, Podcast>> {


    public static final String BASE_HTTP = "http://be.repoai.com:5080/WebRTCAppEE/streams/home/";
    private PodcastsLoadingListener listener;

    public JsonParser(PodcastsLoadingListener listener) {
        this.listener = listener;
    }

    //json parser:
    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected Map<String,Podcast> doInBackground(Void... voids) {

        listener.onLoading();

        StringBuilder data = new StringBuilder();
        Map<String, Podcast> podcastMap = new HashMap<>();
        try {
            //json url: the one we want to parse:
            URL jsonUrl = new URL("https://api.myjson.com/bins/cg2wu");
            //connection obj -> allowing us to open a connection to the web:
            HttpURLConnection httpURLConnection = (HttpURLConnection) jsonUrl.openConnection();
            //getting the connection into our input:
            InputStream inputStream = httpURLConnection.getInputStream();
            //bufferedReader:
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }


            JSONArray jsonArray = new JSONArray(data.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                //podcast is every podcast inside the jsonArrayList
                JSONObject podcast = jsonArray.getJSONObject(i);

                //list of PodCasts, audio is inserted --> NOT YET FUNCTIONAL AUDIO
                String vodName = podcast.getString("vodName");

                String name = vodName.replace("_", " ")
                        .replace(".mp4", "");

                Long timeStamp = podcast.getLong("creationDate");
                Date date = convertEpochToDateTime(timeStamp);

//                id of the podcast
                String vodid = podcast.getString("vodId");

                String audioUrl = BASE_HTTP + vodName;

                Podcast newPodcast = new Podcast(name, audioUrl, date, vodid);
                podcastMap.put(vodid, newPodcast);
            }

            DatabaseReference podRef = FirebaseDatabase.getInstance()
                    .getReference(PODCASTS_TABLE_NAME);
            podRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot podcast : dataSnapshot.getChildren()) {
                        if(podcastMap.containsKey(podcast.getKey())) {
                            long likesCount = podcast.child("likes").getChildrenCount();
                            podcastMap.get(podcast.getKey()).setLikesAmount(likesCount);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return podcastMap;
    }

    private Date convertEpochToDateTime(Long timeStamp) {

        Date date = new Date(timeStamp);

//        Instant instant = Instant.ofEpochMilli(timeStamp);
//        ZoneId zone = ZoneId.systemDefault();
//        return LocalDateTime.ofInstant(instant, zone);

        return date;
    }

    @Override
    protected void onPostExecute(Map<String, Podcast> map) {
        super.onPostExecute(map);
        List<Podcast> pocList = new ArrayList<>(map.values());
        listener.onLoaded(map, pocList);
    }

    public interface PodcastsLoadingListener {
        default void onLoading() {}
        void onLoaded(Map<String, Podcast> map, List<Podcast> podcastsList);
    }

}
