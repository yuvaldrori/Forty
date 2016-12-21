package me.drori.forty;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

class Calendar {

    private static final long NO_CALENDAR = -1;

    private final Context context;
    private long calendarId = NO_CALENDAR;

    //region finding calendar:
    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDAR_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
    };
    // The indices for the projection array above.
    private static final int CALENDAR_PROJECTION_ID_INDEX = 0;
    private static final int CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 1;
    //endregion

    //region working with events:
    private static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events._ID,           // 0
            CalendarContract.Events.DTSTART,       // 1
            CalendarContract.Events.DTEND,         // 2
            CalendarContract.Events.TITLE,         // 3
            CalendarContract.Events.DESCRIPTION,   // 4
            CalendarContract.Events.ALL_DAY        // 5
    };
    // The indices for the projection array above.
    private static final int EVENT_PROJECTION_ID_INDEX = 0;
    private static final int EVENT_PROJECTION_BEGIN_INDEX = 1;
    private static final int EVENT_PROJECTION_END_INDEX = 2;
    private static final int EVENT_PROJECTION_TITLE_INDEX = 3;
    private static final int EVENT_PROJECTION_DESCRIPTION_INDEX = 4;
    private static final int EVENT_PROJECTION_ALL_DAY_INDEX = 5;
    //endregion


    public Calendar(Context context) {
        this.context = context;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref != null) {
            this.calendarId = Long.parseLong(sharedPref.getString(MainActivity.CALENDAR_PREFERENCE_LIST, String.valueOf(NO_CALENDAR)));
        }
    }

    private List<String> getAccounts() {
        List<String> accountsList = new ArrayList<>();
        try {
            Account[] accounts = AccountManager.get(context).getAccounts();
            for (Account account : accounts) {
                accountsList.add(account.name);
            }
        } catch (SecurityException ignored) {

        }
        return accountsList;
    }

    public List<Pair<String, String>> getCalendars() {
        List<Pair<String, String>> calendars = new ArrayList<>();
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        for (String account : getAccounts()) {
            String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
            + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " = ? ))";
            String[] selectionArgs = new String[]{account, Integer.toString(CalendarContract.Calendars.CAL_ACCESS_OWNER)};
            try {
                cur = cr.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);

                while (cur.moveToNext()) {
                    String calID;
                    String displayName;

                    // Get the field values
                    calID = String.valueOf(cur.getLong(CALENDAR_PROJECTION_ID_INDEX));
                    displayName = cur.getString(CALENDAR_PROJECTION_DISPLAY_NAME_INDEX);
                    Pair<String, String> tup = Pair.create(displayName, calID);
                    if (!calendars.contains(tup)) {
                        calendars.add(tup);
                    }
                }
            } catch (SecurityException ignored) {

            } finally {
                if (cur != null && !cur.isClosed()) {
                    cur.close();
                }
            }
        }
        return calendars;
    }

    public void addEvent(Event event) {
        ContentResolver cr = context.getContentResolver();
        java.util.Calendar utc = getMidnightUTC(java.util.Calendar.getInstance());
        long startMillis = utc.getTimeInMillis();
        utc.add(java.util.Calendar.HOUR, 24);
        long endMillis = utc.getTimeInMillis();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");
        values.put(CalendarContract.Events.TITLE, event.getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.ALL_DAY, 1);
        try {
            cr.insert(CalendarContract.Events.CONTENT_URI, values);
        } catch (SecurityException ignored) {

        }
    }

    private java.util.Calendar getMidnightUTC(java.util.Calendar now) {
        java.util.Calendar utc = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.set(now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH), now.get(java.util.Calendar.DAY_OF_MONTH));
        utc.set(java.util.Calendar.HOUR_OF_DAY, 0);
        utc.set(java.util.Calendar.MINUTE, 0);
        utc.set(java.util.Calendar.SECOND, 0);
        utc.set(java.util.Calendar.MILLISECOND, 0);
        return utc;
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        java.util.Calendar utc = getMidnightUTC(java.util.Calendar.getInstance());
        long startMillis = utc.getTimeInMillis();
        utc.add(java.util.Calendar.HOUR, 24);
        long endMillis = utc.getTimeInMillis();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ? ) AND ("
                + CalendarContract.Events.ALL_DAY + " = ? ) AND ("
                + CalendarContract.Events.DTSTART + " BETWEEN ? AND ? ))";
        String[] selectionArgs = new String[]{String.valueOf(calendarId), String.valueOf(1),
                String.valueOf(startMillis), String.valueOf(endMillis)};
        try {
            cur = cr.query(uri,
                    EVENT_PROJECTION,
                    selection,
                    selectionArgs,
                    CalendarContract.Events.DTSTART + " DESC");
            while (cur.moveToNext()) {
                long eventId = cur.getLong(EVENT_PROJECTION_ID_INDEX);
                String title = cur.getString(EVENT_PROJECTION_TITLE_INDEX);
                String description = cur.getString(EVENT_PROJECTION_DESCRIPTION_INDEX);
                long begin = Long.parseLong(cur.getString(EVENT_PROJECTION_BEGIN_INDEX));
                long end = Long.parseLong(cur.getString(EVENT_PROJECTION_END_INDEX));
                int allDay = Integer.parseInt(cur.getString(EVENT_PROJECTION_ALL_DAY_INDEX));
                events.add(new Event(begin, end, eventId, allDay == 1, title, description));
            }
        } catch (SecurityException ignored) {

        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
        }

        return events;
    }
}
