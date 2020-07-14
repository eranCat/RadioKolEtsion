package com.erank.koletsionpods.utils.db;

import android.os.AsyncTask;

import com.erank.koletsionpods.utils.db.models.Podcast;
import com.erank.koletsionpods.utils.listeners.PodcastsLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PodcastsFromJsonAsyncTask extends AsyncTask<Void, Void, List<Podcast>> {


    private static final String BASE_HTTP = "https://be.repoai.com:5443/LiveApp/";
    private static final String ALL_PODCASTS = BASE_HTTP + "rest/broadcast/getVodList/0/100";
    private PodcastsLoadingListener listener;

    public PodcastsFromJsonAsyncTask(PodcastsLoadingListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<Podcast> doInBackground(Void... voids) {
        listener.onLoading();
        List<Podcast> podcastsList = new ArrayList<>();
        try {
            //json url: the one we want to parse:
            String json = getUrlBody(ALL_PODCASTS);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                //jsonPodcast is every jsonPodcast inside the jsonArrayList
                JSONObject jsonPodcast = jsonArray.getJSONObject(i);
                String vodName = jsonPodcast.getString("vodName");
                if (isStupidVid(vodName))
                    continue;

                podcastsList.add(new Podcast(jsonPodcast, BASE_HTTP));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return podcastsList;
    }

    @Override
    protected void onPostExecute(List<Podcast> podcasts) {
        listener.onLoaded(podcasts);
    }

    private boolean isStupidVid(String name) {
        String v1 = "b70eb3b15e061563728238230.mp4";
        String v2 = "d5e465f222571563728407982.mp4";

        return name.equals(v1) || name.equals(v2);
    }

    private String getUrlBody(String url) {
        Request request = new Request.Builder().url(url).build();

        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return body != null ? body.string() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
