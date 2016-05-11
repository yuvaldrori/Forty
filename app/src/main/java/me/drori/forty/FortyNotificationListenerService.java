package me.drori.forty;

import android.app.Notification.Action;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;

public class FortyNotificationListenerService extends NotificationListenerService {

    public static final String TAG = "FORTY";
    private static final String CALENDAR_NAME = "Forty";

    private Calendar calendar;
    private static Context context;

    public static Context getContext() {
        return FortyNotificationListenerService.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        this.calendar = new Calendar(CALENDAR_NAME);
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
        String pkgName = sbn.getPackageName();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, sbn.toString());
            Bundle extras = sbn.getNotification().extras;
            if (extras != null) {
                Log.d(TAG, extras.toString());
            }
        }

        Notification notification = null;
        Event event;
        switch (pkgName) {
            case PocketCasts.PKG_NAME:
                // ignore downloads notifications
                Action[] actions = sbn.getNotification().actions;
                if (actions != null && actions.length == 3) {
                    notification = new PocketCasts(sbn).getNotification();
                }
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
