package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class PocketCasts {

    public static final String PKG_NAME = "au.com.shiftyjelly.pocketcasts";
    private static final int PLAY = 362;
    private static final int PAUSE = 264;

    private final int flags;
    private String podcast;
    private String episode;
    private final long time;

    public PocketCasts(StatusBarNotification sbn) {
        // play or pause?
        this.flags = sbn.getNotification().flags;

        // details
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            this.podcast = extras.getString("android.title");
            this.episode = extras.getString("android.text");
        }

        // notification time
        this.time = sbn.getPostTime();
    }

    public Notification getNotification() {
        Notification.actions action = null;
        switch (flags) {
            case PLAY:
                action = Notification.actions.START;
                break;
            case PAUSE:
                action = Notification.actions.STOP;
                break;
        }
        return new Notification(PKG_NAME, podcast, episode, action, time);
    }
}
