package me.drori.forty;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification.Action;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FortyNotificationListenerService extends NotificationListenerService {

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDAR_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT                  // 3
    };
    // The indices for the projection array above.
    private static final int CALENDAR_PROJECTION_ID_INDEX = 0;
    private static final int CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private static final String TAG = "FORTY";

    private static final String CALENDAR_NAME = "Forty";

    private static Context mContext;
    private static long mCalendarId;

    public static Context getContext() {
        return FortyNotificationListenerService.mContext;
    }

    public static String getTag() {
        return FortyNotificationListenerService.TAG;
    }

    public static long getCalendarId() {
        return FortyNotificationListenerService.mCalendarId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created");
        FortyNotificationListenerService.mContext = this;
        mCalendarId = searchCalendarId();
        Log.d(TAG, "Got CalendarId");
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
        switch (pkgName) {
            case PocketCasts.PKG_NAME:
                // ignore downloads notifications
                Action[] actions = sbn.getNotification().actions;
                if (actions != null && actions.length == 3) {
                    PocketCasts pc = new PocketCasts(sbn);
                    pc.run();
                }
                break;
        }
    }

    private List<String> getAccounts() {
        List<String> accountsList = new ArrayList<String>();
        try {
            Account[] accounts = AccountManager.get(this).getAccounts();
            for (Account account : accounts) {
                Log.d(TAG, account.name);
                accountsList.add(account.name);
            }
        } catch (Exception e) {
            Log.i("Exception", "Exception:" + e);
        }
        return accountsList;
    }

    private long searchCalendarId() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return -1;
        }
        long ret = -1;
        // Run query
        Cursor cur = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        for (String account : getAccounts()) {
            String selection = "((" + Calendars.ACCOUNT_NAME + " = ?))";
            String[] selectionArgs = new String[]{account};
            // Submit the query and get a Cursor object back.
            cur = cr.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);//, selection, selectionArgs, null);

            while (cur.moveToNext()) {
                long calID;
                String displayName;
                String accountName;
                String ownerName;

                // Get the field values
                calID = cur.getLong(CALENDAR_PROJECTION_ID_INDEX);
                displayName = cur.getString(CALENDAR_PROJECTION_DISPLAY_NAME_INDEX);
                accountName = cur.getString(CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX);
                ownerName = cur.getString(CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX);

                Log.i(TAG, "displayName = " + displayName + " accountName = " + accountName + " ownerName = " + ownerName);

                // Do something with the values...

                if (Objects.equals(displayName, CALENDAR_NAME)) {
                    ret = calID;
                    break;
                }
            }
        }
        cur.close();
        return ret;
    }
}
