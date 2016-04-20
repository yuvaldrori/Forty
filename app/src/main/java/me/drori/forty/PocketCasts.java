package me.drori.forty;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.service.notification.StatusBarNotification;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by yuvald on 17-Apr-16.
 */
public class PocketCasts {

    public static final String PKG_NAME = "au.com.shiftyjelly.pocketcasts";
    private static final int PLAY = 362;
    private static final int PAUSE = 264;

    private int mFlags;
    private String mPodcast;
    private String mEpisode;
    private String mAction;
    private String mCaller;
    private Context mContext;

    public PocketCasts(Context context, StatusBarNotification sbn, String caller) {
        mContext = context;
        // play or pause?
        mFlags = sbn.getNotification().flags;

        switch (mFlags) {
            case PLAY:
                mAction = "Play";
                break;
            case PAUSE:
                mAction = "Pause";
                break;
            default:
                mAction = "";
                break;
        }

        mCaller = caller;

        // details
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            mPodcast = extras.getString("android.title");
            mEpisode = extras.getString("android.text");
        }
    }

    private long addEvent() {
        long calID = 3;
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, mPodcast);
        values.put(CalendarContract.Events.DESCRIPTION, mEpisode);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

    // get the event ID that is the last element in the Uri
        return Long.parseLong(uri.getLastPathSegment());
    }

    public void run() {
        Log.i(FortyNotificationListenerService.TAG, "caller: " + mCaller + " action: " + mAction + " podcast: " + mPodcast + " episode: " + mEpisode);
    }
}
