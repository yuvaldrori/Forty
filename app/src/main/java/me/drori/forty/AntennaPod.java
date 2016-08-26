package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class AntennaPod {

    public static final String PKG_NAME = "de.danoeh.antennapod";

    private String podcast;
    private String episode;
    private final long time;
    private final android.app.Notification.Action[] actions;

    public AntennaPod(StatusBarNotification sbn) {
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
        if (actions == null || actions.length != 4) {
            return null;
        }

        return new Notification(PKG_NAME, podcast, episode, time);
    }
}
