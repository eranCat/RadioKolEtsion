package com.erank.koletsionpods.utils.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.utils.db.models.Podcast;

public class SharingHelper {
    private static SharingHelper instance;

    private SharingHelper() {
    }

    public static SharingHelper getInstance() {
        return instance != null ? instance : (instance= new SharingHelper());
    }


    public void share(Context context, Podcast podcast) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        Resources rss = context.getResources();

        String msg = rss.getString(R.string.check_out_pod);
        intent.putExtra(Intent.EXTRA_SUBJECT,
                msg + podcast.getDescription());
        intent.putExtra(Intent.EXTRA_TEXT,
                podcast.getAudioUrl());//"bep://be_podcast" +
        String shareTxt = rss.getString(R.string.share_poscast);

        Intent i = Intent.createChooser(intent, shareTxt);
        context.startActivity(i);
    }
}
