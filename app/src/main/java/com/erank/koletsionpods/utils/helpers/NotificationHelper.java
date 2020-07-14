package com.erank.koletsionpods.utils.helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.app.NotificationCompat;

import com.erank.koletsionpods.MyApplication;
import com.erank.koletsionpods.R;
import com.erank.koletsionpods.activities.SplashScreenActivity;
import com.erank.koletsionpods.utils.db.models.Podcast;
import com.erank.koletsionpods.utils.media_player.MediaPlayerHelper;
import com.erank.koletsionpods.utils.media_player.NotificationActionService;
import com.erank.koletsionpods.receivers.NotificationActionBroadcaster;
import com.erank.koletsionpods.utils.enums.MPServiceStates;

import static androidx.core.app.NotificationCompat.Builder;
import static androidx.core.app.NotificationCompat.PRIORITY_LOW;
import static androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.erank.koletsionpods.utils.enums.MPServiceStates.ACTION_FORWARD;
import static com.erank.koletsionpods.utils.enums.MPServiceStates.ACTION_NEXT;
import static com.erank.koletsionpods.utils.enums.MPServiceStates.ACTION_PAUSE;
import static com.erank.koletsionpods.utils.enums.MPServiceStates.ACTION_PLAY;
import static com.erank.koletsionpods.utils.enums.MPServiceStates.ACTION_PREVIOUS;
import static com.erank.koletsionpods.utils.enums.MPServiceStates.ACTION_REWIND;


public class NotificationHelper {

    public static final String EXTRA_NOTIFICATION = "openedFromNotification";
    public static final String NOTIFICATION_DELETED_ACTION = "notification_dismissed";
    private static NotificationHelper instance;
    private final MediaPlayerHelper mediaPlayerHelper;
    private final PendingIntent deleteIntent;
    private NotificationManagerCompat notificationManager;
    private Action previousAction, nextAction, rewindAction, forwardAction;
    private NotificationCompat.MediaStyle mediaStyle;
    private int tintColor;
    private Bitmap artwork;

    private boolean isPriorNougatVersion;

    private NotificationHelper(Context context) {
        notificationManager = NotificationManagerCompat.from(context);

        Intent intent = new Intent(NOTIFICATION_DELETED_ACTION);
        deleteIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        previousAction = generateAction(context, R.drawable.ic_skip_previous, ACTION_PREVIOUS);
        nextAction = generateAction(context, R.drawable.ic_skip_next, ACTION_NEXT);
        rewindAction = generateAction(context, R.drawable.ic_rewind_30, ACTION_REWIND);
        forwardAction = generateAction(context, R.drawable.ic_forward_30, ACTION_FORWARD);

        MediaSessionCompat mediaSession = new MediaSessionCompat(context, "session");
        mediaStyle = new NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())//for cool fx
                .setShowActionsInCompactView(1, 2, 3);

        tintColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        artwork = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
//        artwork = getCroppedBitmap(artwork);

        mediaPlayerHelper = MediaPlayerHelper.getInstance();
    }

    public static NotificationHelper getInstance(Context context) {
        return instance != null ? instance : (instance = new NotificationHelper(context));
    }

    public void cancelAll() {
        notificationManager.cancelAll();
    }

    public void notify(Context context) {

        Podcast podcast = mediaPlayerHelper.getCurrentPodcast();

        if (podcast == null) return;

        Intent openAppIntent =
                new Intent(context, SplashScreenActivity.class)
                        .putExtra(EXTRA_NOTIFICATION, true);

        PendingIntent openAppPIntent = PendingIntent
                .getActivity(context, 1, openAppIntent, 0);

        Action togglePlayPause = generatePlayPauseAction(context, podcast.isPlaying());

        Builder builder = new Builder(context, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_headset)
                .setLargeIcon(artwork)
                .setDeleteIntent(deleteIntent)
                .setColor(tintColor)
                .setPriority(PRIORITY_LOW)
                .setVisibility(VISIBILITY_PUBLIC)
                .setContentIntent(openAppPIntent)
                .setContentTitle(podcast.getDescription())
                .setOnlyAlertOnce(true);

        switch (podcast.state) {
            case DEFAULT:
            case LOADING:
            case PREPARED:
                builder.setProgress(0, 0, true);
                break;
            case PLAYING:
            case PAUSED:
                addActions(builder, togglePlayPause)
                        .setProgress(0, 0, false);
                break;
        }

        context.startService(new Intent(context, NotificationActionService.class));
        notificationManager.notify(1, builder.build());
    }

    private Builder addActions(Builder builder, Action togglePlayPause) {
        return builder.setStyle(mediaStyle)//set media style
                .addAction(rewindAction)
                .addAction(previousAction)
                .addAction(togglePlayPause)
                .addAction(nextAction)
                .addAction(forwardAction);
    }

    private Action generatePlayPauseAction(Context context, boolean isPlaying) {
        int icon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
        MPServiceStates action = isPlaying ? ACTION_PAUSE : ACTION_PLAY;
        return generateAction(context, icon, action);
    }


    private Action generateAction(Context context, @DrawableRes int icon, MPServiceStates state) {

        Intent intent =
                new Intent(context, NotificationActionBroadcaster.class)
                        .setAction(state.name());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new Action.Builder(icon, state.value, pendingIntent).build();
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
}