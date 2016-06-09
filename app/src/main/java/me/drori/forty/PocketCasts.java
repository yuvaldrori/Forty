package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class PocketCasts {

    public static final String PKG_NAME = "au.com.shiftyjelly.pocketcasts";

    private String podcast;
    private String episode;
    private final long time;
    private final android.app.Notification.Action[] actions;

    public PocketCasts(StatusBarNotification sbn) {
        // actions
        actions = sbn.getNotification().actions;

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
        // ignore downloads notifications
        if (actions == null || actions.length != 3) {
            return null;
        }

        return new Notification(PKG_NAME, podcast, episode, time);
    }
}
