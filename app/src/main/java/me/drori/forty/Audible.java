package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class Audible {

    public static final String PKG_NAME = "com.audible.application";

    private String podcast;
    private String episode;
    private final long time;

    public Audible(StatusBarNotification sbn) {
        android.app.Notification notification = sbn.getNotification();
        // details
        Bundle extras = notification.extras;
        if (extras != null) {
            this.podcast = extras.getString("android.title");
            this.episode = extras.getString("android.text");
        }

        // notification time
        this.time = sbn.getPostTime();
    }

    public Notification getNotification() {
        return new Notification(PKG_NAME, podcast, episode, time);
    }
}
