package me.drori.forty;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

public class FortyNotificationListenerService extends NotificationListenerService {

    public static final String TAG = "FORTY";
    private static final String CALENDAR_NAME = "Forty";

    private static Context context;

    public static Context getContext() {
        return FortyNotificationListenerService.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    private boolean hasPermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        act(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        act(sbn);
    }

    private void act(StatusBarNotification sbn) {
        if (!hasPermissions()) {
            return;
        }
        Calendar calendar = new Calendar(CALENDAR_NAME);
        String pkgName = sbn.getPackageName();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, sbn.toString());
            android.app.Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            if (extras != null) {
                Log.d(TAG, extras.toString());
            }
        }

        Notification notification = null;
        Event event;
        switch (pkgName) {
            case PocketCasts.PKG_NAME:
                notification = new PocketCasts(sbn).getNotification();
                break;
        }
        if (notification == null) {
            return;
        }
        event = calendar.getEvent();
        Logic logic = new Logic(event, notification);
        List<Event> events = logic.getEvents();
        for (Event e : events) {
            if (event.getId() == e.getId()) {
                calendar.updateEvent(e);
            } else {
                calendar.addEvent(e);
            }
        }
    }
}
