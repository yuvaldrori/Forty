package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class PodcastAddict {

    public static final String PKG_NAME = "com.bambuna.podcastaddict";

    private String podcast;
    private String episode;
    private boolean mediaSession;
    private final long time;

    public PodcastAddict(StatusBarNotification sbn) {

        // details
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            this.episode = extras.getString("android.title");
            this.podcast = extras.getString("android.text");
            mediaSession = extras.get("android.mediaSession") instanceof android.media.session.MediaSession.Token;
        }

        // notification time
        this.time = sbn.getPostTime();
    }

    public Notification getNotification() {
        // ignore some notifications
        if (!mediaSession) {
            return null;
        }
        return new Notification(PKG_NAME, podcast, episode, time);
    }
}
