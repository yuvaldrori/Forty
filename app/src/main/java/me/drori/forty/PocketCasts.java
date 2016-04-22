package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class PocketCasts {

    public static final String PKG_NAME = "au.com.shiftyjelly.pocketcasts";
    private static final int PLAY = 362;
    private static final int PAUSE = 264;

    private final int mFlags;
    private String mPodcast;
    private String mEpisode;

    public PocketCasts(StatusBarNotification sbn) {
        // play or pause?
        mFlags = sbn.getNotification().flags;

        // details
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            mPodcast = extras.getString("android.title");
            mEpisode = extras.getString("android.text");
        }
    }

    public void run() {
        Event event = new Event(mPodcast, mEpisode);
        switch (mFlags) {
            case PLAY:
                event.add();
                break;
            case PAUSE:
                event.updateEndTime();
        }
    }
}
