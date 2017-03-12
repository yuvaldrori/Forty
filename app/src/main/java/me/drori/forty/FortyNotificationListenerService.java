package me.drori.forty;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class FortyNotificationListenerService extends NotificationListenerService
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String TAG = "FORTY";
    public static final List<String> SUPPORTED_APPS = Arrays.asList(PocketCasts.PKG_NAME, AntennaPod.PKG_NAME, PodcastAddict.PKG_NAME);

    private Calendar calendar = null;

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    private boolean hasPermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(MainActivity.CALENDAR_PREFERENCE_LIST)) {
            this.calendar = null;
        }
    }

    public void run(StatusBarNotification sbn) {
        if (!hasPermissions()) {
            return;
        }

        if (this.calendar == null) {
            this.calendar = new Calendar(this);
        }

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
        List<Event> events;
        switch (pkgName) {
            case PocketCasts.PKG_NAME:
                notification = new PocketCasts(sbn).getNotification();
                break;
            case AntennaPod.PKG_NAME:
                notification = new AntennaPod(sbn).getNotification();
                break;
            case PodcastAddict.PKG_NAME:
                notification = new PodcastAddict(sbn).getNotification();
                break;
        }
        if (notification == null) {
            return;
        }
        events = this.calendar.getEvents();
        Logic logic = new Logic(events, notification);
        Event event = logic.getEvent();
        if (event != null) {
            calendar.addEvent(event);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        run(sbn);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        run(sbn);
    }
}
