package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class PodcastAddict {

    public static final String PKG_NAME = "com.bambuna.podcastaddict";

    private String podcast;
    private String episode;
    private int priority;
    private final long time;

    public PodcastAddict(StatusBarNotification sbn) {

        this.priority = sbn.getNotification().priority;

        // details
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            this.podcast = extras.getString("podcastName");
            if (this.podcast == null) {
                this.podcast = "";
            }
            this.episode = extras.getString("android.text");
        }

        // notification time
        this.time = sbn.getPostTime();
    }

    public Notification getNotification() {
        // ignore some notifications
        if (this.priority < 1) {
            return null;
        }
        return new Notification(PKG_NAME, podcast, episode, time);
    }
}
