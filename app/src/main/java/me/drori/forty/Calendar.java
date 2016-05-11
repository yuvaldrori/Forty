package me.drori.forty;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

class Calendar {

    private static final long NO_CALENDAR = -1;

    private final String name;
    private final Context context;
    private final long calendarId;

    //region finding calendar:
    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDAR_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };
    // The indices for the projection array above.
    private static final int CALENDAR_PROJECTION_ID_INDEX = 0;
    private static final int CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    //endregion

    //region working with events:
    private static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events._ID,           // 0
            CalendarContract.Events.DTSTART,       // 1
            CalendarContract.Events.DTEND,         // 2
            CalendarContract.Events.TITLE,         // 3
            CalendarContract.Events.DESCRIPTION    // 4
    };
    // The indices for the projection array above.
    private static final int EVENT_PROJECTION_ID_INDEX = 0;
    private static final int EVENT_PROJECTION_BEGIN_INDEX = 1;
    private static final int EVENT_PROJECTION_END_INDEX = 2;
    private static final int EVENT_PROJECTION_TITLE_INDEX = 3;
    private static final int EVENT_PROJECTION_DESCRIPTION_INDEX = 4;
    //endregion


    public Calendar(String name) {
        this.name = name;
        this.context = FortyNotificationListenerService.getContext();
        this.calendarId = getCalendarId();
    }

    private List<String> getAccounts() {
        List<String> accountsList = new ArrayList<>();
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            accountsList.add(account.name);
        }
        return accountsList;
    }

    private long getCalendarId() {
        long id = NO_CALENDAR;
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        for (String account : getAccounts()) {
            String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?))";
            String[] selectionArgs = new String[]{account};
            cur = cr.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);

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

                if (Objects.equals(displayName, name)) {
                    id = calID;
                    break;
                }
            }
        }
        if (cur != null && !cur.isClosed()) {
            cur.close();
        }
        return id;
    }

    public void addEvent(Event event) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, event.getBegin());
        values.put(CalendarContract.Events.DTEND, event.getEnd());
        String tz = TimeZone.getDefault().getID();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, tz);
        values.put(CalendarContract.Events.TITLE, event.getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        try {
            cr.insert(CalendarContract.Events.CONTENT_URI, values);
        } catch (SecurityException ignored) {

        }
    }

    public void updateEvent(Event event) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri updateUri;
        values.put(CalendarContract.Events.DTSTART, event.getBegin());
        values.put(CalendarContract.Events.DTEND, event.getEnd());
        String tz = TimeZone.getDefault().getID();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, tz);
        values.put(CalendarContract.Events.TITLE, event.getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getId());
        cr.update(updateUri, values, null, null);
    }

    public Event getEvent() {
        Event event = null;

        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        // Specify the date range you want to search for recurring
        // event instances
        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        beginTime.add(java.util.Calendar.HOUR, -24); // yesterday
        long startMillis = beginTime.getTimeInMillis();
        java.util.Calendar endTime = java.util.Calendar.getInstance();
        long endMillis = endTime.getTimeInMillis();
        // Construct the query with the desired date range.
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ? ) AND ("
                + CalendarContract.Events.DTSTART + " BETWEEN ? AND ? ))";
        String[] selectionArgs = new String[]{String.valueOf(calendarId),
                String.valueOf(startMillis), String.valueOf(endMillis)};
        // Submit the query
        try {
            cur = cr.query(uri,
                    EVENT_PROJECTION,
                    selection,
                    selectionArgs,
                    CalendarContract.Events.DTSTART + " DESC");
            if (cur != null && cur.moveToFirst()) {
                long eventId = cur.getLong(EVENT_PROJECTION_ID_INDEX);
                String title = cur.getString(EVENT_PROJECTION_TITLE_INDEX);
                String description = cur.getString(EVENT_PROJECTION_DESCRIPTION_INDEX);
                long begin = Long.parseLong(cur.getString(EVENT_PROJECTION_BEGIN_INDEX));
                long end = Long.parseLong(cur.getString(EVENT_PROJECTION_END_INDEX));
                event = new Event(begin, end, eventId, title, description);
            }
        } catch (SecurityException e) {
            event = null;
        }
        if (cur != null && !cur.isClosed()) {
            cur.close();
        }
        return event;
    }
}
