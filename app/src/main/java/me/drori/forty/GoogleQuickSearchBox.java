package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class GoogleQuickSearchBox {

    public static final String PKG_NAME = "com.google.android.googlequicksearchbox";

    private String podcast;
    private String episode;
    private boolean mediaSession;
    private final long time;

    public GoogleQuickSearchBox(StatusBarNotification sbn) {
        // details
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            this.podcast = extras.getString("android.text");
            this.episode = extras.getString("android.title");
            mediaSession = extras.get("android.mediaSession") instanceof android.media.session.MediaSession.Token;
        }

        // notification time
        this.time = sbn.getPostTime();
    }

    public Notification getNotification() {
        // ignore downloads notifications
        if (!mediaSession) {
            return null;
        }

        return new Notification(PKG_NAME, podcast, episode, time);
    }
}
