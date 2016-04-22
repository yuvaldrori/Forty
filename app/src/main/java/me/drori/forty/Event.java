package me.drori.forty;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

class Event {

    private static final String[] EVENT_PROJECTION = new String[]{
            Events._ID,           // 0
            Events.DTSTART,       // 1
            Events.DTEND,         // 2
            Events.TITLE,         // 3
            Events.DESCRIPTION    // 4
    };
    // The indices for the projection array above.
    private static final int EVENT_PROJECTION_ID_INDEX = 0;
    private static final int EVENT_PROJECTION_BEGIN_INDEX = 1;
    private static final int EVENT_PROJECTION_END_INDEX = 2;
    private static final int EVENT_PROJECTION_TITLE_INDEX = 3;
    private static final int EVENT_PROJECTION_DESCRIPTION_INDEX = 4;
    private final Context mContext;
    private final String mTag;
    private long mId;
    private final String mTitle;
    private final String mDescription;
    private final long mBegin;
    private long mEnd;
    private final long mCalendarId;

    private Event(long id, String title, String description, long begin, long end) {
        mContext = FortyNotificationListenerService.getContext();
        mTag = FortyNotificationListenerService.getTag();

        mId = id;
        mTitle = title;
        mDescription = description;
        mBegin = begin;
        mEnd = end;
        mCalendarId = FortyNotificationListenerService.getCalendarId();

        Log.d(mTag, "Title: " + title
                + " description: " + description
                + " begin: " + begin
                + " end: " + end);
    }

    public Event(String title, String description) {
        mContext = FortyNotificationListenerService.getContext();
        mTag = FortyNotificationListenerService.getTag();

        mTitle = title;
        mDescription = description;
        mCalendarId = FortyNotificationListenerService.getCalendarId();
        mBegin = Calendar.getInstance().getTimeInMillis();

        Log.d(mTag, "Title: " + title
                + " description: " + description);
    }

    private long getBegin() {
        return mBegin;
    }

    private long getEnd() {
        return mEnd;
    }

    private Event getLastEvent() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Event ret = null;

        Cursor cur;
        ContentResolver cr = mContext.getContentResolver();
        // Specify the date range you want to search for recurring
        // event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.add(Calendar.HOUR, -24); // yesterday
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        long endMillis = endTime.getTimeInMillis();
        // Construct the query with the desired date range.
        Uri uri = Events.CONTENT_URI;
        String selection = "((" + Events.CALENDAR_ID + " = ? ) AND ("
                + Events.DTSTART + " BETWEEN ? AND ? ) AND ("
                + Events.TITLE + " = ? ) AND ("
                + Events.DESCRIPTION + " = ?))";
        String[] selectionArgs = new String[]{String.valueOf(mCalendarId),
                String.valueOf(startMillis), String.valueOf(endMillis),
                mTitle, mDescription,};
        // Submit the query
        cur = cr.query(uri,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                Events.DTSTART + " DESC");
        if (cur.moveToFirst()) {
            long id = cur.getLong(EVENT_PROJECTION_ID_INDEX);
            String title = cur.getString(EVENT_PROJECTION_TITLE_INDEX);
            String description = cur.getString(EVENT_PROJECTION_DESCRIPTION_INDEX);
            long begin = Long.parseLong(cur.getString(EVENT_PROJECTION_BEGIN_INDEX));
            long end = Long.parseLong(cur.getString(EVENT_PROJECTION_END_INDEX));
            ret = new Event(id, title, description, begin, end);
        }
        cur.close();
        return ret;
    }

    public void add() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Event last = getLastEvent();
        if (last != null) {
            long begin = last.getBegin();
            long end = last.getEnd();
            long diff = end - begin;
            long diff2 = mBegin - begin;
            if (diff < 1 * 1000 && diff2 < 60 * 1000) {
                Log.i(mTag, "No need to add new event.");
                return;
            }
        }
        long startMillis;
        long endMillis;
        Calendar beginTime = Calendar.getInstance();
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        String tz = TimeZone.getDefault().getID();
        values.put(Events.EVENT_TIMEZONE, tz);
        values.put(Events.TITLE, mTitle);
        values.put(Events.DESCRIPTION, mDescription);
        values.put(Events.CALENDAR_ID, mCalendarId);
        Uri uri = cr.insert(Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        mId = Long.parseLong(uri.getLastPathSegment());
    }

    public void updateEndTime() {
        Event last = getLastEvent();
        if (last == null) {
            // nothing to update
            Log.i(mTag, "No event to update end time.");
            return;
        } else {
            long begin = last.getBegin();
            long end = last.getEnd();
            long diff = end - begin;
            long diff2 = mBegin - begin;
            if (diff < 1 * 1000 && diff2 < 60 * 1000) {
                last.delete();
                Log.i(mTag, "Less than a minute of play gets deleted.");
                return;
            }
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        Uri updateUri;
        // The new title for the event
        values.put(Events.DTEND, endTime);
        updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, last.mId);
        int rows = cr.update(updateUri, values, null, null);
        Log.d(mTag, "Rows updated: " + rows);
    }

    private void delete() {
        ContentResolver cr = mContext.getContentResolver();
        Uri deleteUri;
        deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, mId);
        int rows = cr.delete(deleteUri, null, null);
        Log.d(mTag, "Rows deleted: " + rows);
    }
}
