package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class PodcastAddict {

    public static final String PKG_NAME = "com.bambuna.podcastaddict";

    private String podcast;
    private String episode;
    private final long time;
    private final android.app.Notification.Action[] actions;

    public PodcastAddict(StatusBarNotification sbn) {
        // actions
        actions = sbn.getNotification().actions;

        // details
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            // android.title has the app name instead of the podcast name
            this.podcast = extras.getString("android.text");
            this.episode = extras.getString("android.text");
        }

        // notification time
        this.time = sbn.getPostTime();
    }

    public Notification getNotification() {
        return new Notification(PKG_NAME, podcast, episode, time);
    }
}
