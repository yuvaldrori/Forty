package me.drori.forty;

import android.app.Notification.Action;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by yuvald on 16-Apr-16.
 */
public class FortyNotificationListenerService extends NotificationListenerService {

    public static final String TAG = "FORTY";
    private static final String POSTED = "posted";
    private static final String REMOVED = "removed";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        act(sbn, POSTED);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        act(sbn, REMOVED);
    }

    private void act(StatusBarNotification sbn, String caller) {
        Context context = this;
        String pkgName = sbn.getPackageName();
        switch (pkgName) {
            case PocketCasts.PKG_NAME:
                // ignore downloads notifications
                Action[] actions = sbn.getNotification().actions;
                if (actions != null && actions.length == 3) {
                    PocketCasts pc = new PocketCasts(this, sbn, caller);
                    pc.run();
                }
                break;
        }
    }
}
