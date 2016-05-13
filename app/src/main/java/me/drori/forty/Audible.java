package me.drori.forty;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

class Audible {

    public static final String PKG_NAME = "com.audible.application";
    private static final int PLAY = 2130837962;
    private static final int PAUSE = 2130837961;

    private int flags = 0;
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
            // play or pause?
            this.flags = extras.getInt("android.icon");
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
