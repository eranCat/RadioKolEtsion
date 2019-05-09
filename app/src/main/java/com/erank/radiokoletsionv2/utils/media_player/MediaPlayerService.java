package com.erank.radiokoletsionv2.utils.media_player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.erank.radiokoletsionv2.R;
import com.erank.radiokoletsionv2.activities.MainActivity;
import com.erank.radiokoletsionv2.fragments.podcasts.Podcast;
import com.erank.radiokoletsionv2.receivers.MediaPlayerReceiver;
import com.erank.radiokoletsionv2.utils.AppManager;

import static com.erank.radiokoletsionv2.utils.media_player.MediaPlayerAction.*;

public class MediaPlayerService extends Service {

    private MediaPlayerHolder mediaPlayerHolder;
    private NotificationManagerCompat notificationManager;
    private MyCallback myCallback;
    private MediaPlayerReceiver playerReceiver = new MediaPlayerReceiver() {
        @Override
        public void onSwap() {
            Intent service = new Intent(MediaPlayerService.this, MediaPlayerService.class);
            service.setAction(ACTION_SWAP.name());
            MediaPlayerService.this.startService(service);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayerHolder = MediaPlayerHolder.getInstance();
        notificationManager = NotificationManagerCompat.from(this);


        IntentFilter filter = new IntentFilter();

        LocalBroadcastManager.getInstance(this).registerReceiver(playerReceiver,filter);
        return super.onStartCommand(intent, flags, startId);
    }

    private void buildNotification(NotificationCompat.Action action) {
        MediaSessionCompat.Token token = new MediaSessionCompat(
                getApplicationContext(), "session").getSessionToken();

        androidx.media.app.NotificationCompat.MediaStyle style =
                new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)//for cool fx
                        .setShowActionsInCompactView(0, 1, 2);

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(ACTION_RESET.toString());
        PendingIntent stopService = PendingIntent.getService(this, 1, intent, 0);


        Intent openMain = new Intent(this, MainActivity.class);
        openMain.putExtra("data", "fromService");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, openMain, 0);

        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
//        img = getCroppedBitmap(img);

        Podcast podcast = mediaPlayerHolder.getPodcast();
        String description = "Stuff";
        if (podcast != null) {
            description = podcast.getDescription();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_library_music)
                .setLargeIcon(img)
                .setContentTitle(description)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(stopService)
                .setPriority(NotificationCompat.PRIORITY_LOW)//support for lower version
                .setStyle(style);

        builder.addAction(generateAction(R.drawable.ic_skip_previous_black_24dp, "Previous",
                ACTION_PREVIOUS.toString()));
        builder.addAction(action);
        builder.addAction(generateAction(R.drawable.ic_skip_next_black_24dp, "Next", ACTION_NEXT.toString()));

        notificationManager.notify(1, builder.build());
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    class MyCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            super.onPlay();
            Log.e("MediaPlayerService", "onPlay");
            buildNotification(generateAction(R.drawable.ic_pause, "Pause", ACTION_PAUSE.toString()));
            mediaPlayerHolder.play();
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.e("MediaPlayerService", "onPause");
            buildNotification(generateAction(R.drawable.ic_play, "Play", ACTION_PLAY.toString()));
            mediaPlayerHolder.pause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.e("MediaPlayerService", "onSkipToNext");
            //Change media here
            buildNotification(generateAction(R.drawable.ic_pause, "Pause", ACTION_PAUSE.toString()));
            mediaPlayerHolder.playNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.e("MediaPlayerService", "onSkipToPrevious");
            //Change media here
            buildNotification(generateAction(R.drawable.ic_pause, "Pause", ACTION_PAUSE.toString()));
            mediaPlayerHolder.playPrevious();
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            Log.e("MediaPlayerService", "onFastForward");
            //Manipulate current media here
        }

        @Override
        public void onRewind() {
            super.onRewind();
            Log.e("MediaPlayerService", "onRewind");
            //Manipulate current media here
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.e("MediaPlayerService", "onStop");
            //Stop media player here
            notificationManager.cancel(1);
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            stopService(intent);
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    }
}
