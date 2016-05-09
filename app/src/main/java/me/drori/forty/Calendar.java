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

import java.text.MessageFormat;
import java.util.Objects;
import java.util.TimeZone;

class Calendar {

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
    private static final long MINIMAL_LISTENING_TIME = 60 * 1000; // one minute in ms
    private static final long SEPARATE_EVENT = MINIMAL_LISTENING_TIME;
    private static final long ZERO_LENGTH_EVENT = 1 * 1000; // one second in ms

    private Calendar(long id, String title, String description, long begin, long end) {
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

    public Calendar(String title, String description) {
        mContext = FortyNotificationListenerService.getContext();
        mTag = FortyNotificationListenerService.getTag();

        mTitle = title;
        mDescription = description;
        mCalendarId = FortyNotificationListenerService.getCalendarId();
        long now = java.util.Calendar.getInstance().getTimeInMillis();
        mBegin = now;
        mEnd = now;
    }

    private long getDuration() {
        return mEnd - mBegin;
    }

    private boolean isZeroLengthEvent() {
        return (getDuration() < ZERO_LENGTH_EVENT);
    }

    private boolean isMinimal() {
        return (getDuration() < MINIMAL_LISTENING_TIME);
    }

    private boolean isSeparateEvents(Calendar calendar) {
        long timeBetweenEvents = mBegin - calendar.mEnd;
        if (timeBetweenEvents < 0) {
            timeBetweenEvents = calendar.mBegin - mEnd;
        }
        return (timeBetweenEvents > SEPARATE_EVENT);
    }

    private Calendar getLastEvent() throws SecurityException {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Don't have READ_CALENDAR permission.");
        }

        Cursor cur;
        ContentResolver cr = mContext.getContentResolver();
        // Specify the date range you want to search for recurring
        // event instances
        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        beginTime.add(java.util.Calendar.HOUR, -24); // yesterday
        long startMillis = beginTime.getTimeInMillis();
        java.util.Calendar endTime = java.util.Calendar.getInstance();
        long endMillis = endTime.getTimeInMillis();
        // Construct the query with the desired date range.
        Uri uri = Events.CONTENT_URI;
        String selection = "((" + Events.CALENDAR_ID + " = ? ) AND ("
                + Events.DTSTART + " BETWEEN ? AND ? ))";
        String[] selectionArgs = new String[]{String.valueOf(mCalendarId),
                String.valueOf(startMillis), String.valueOf(endMillis)};
        // Submit the query
        cur = cr.query(uri,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                Events.DTSTART + " DESC");
        if (cur != null && cur.moveToFirst()) {
            long id = cur.getLong(EVENT_PROJECTION_ID_INDEX);
            String title = cur.getString(EVENT_PROJECTION_TITLE_INDEX);
            String description = cur.getString(EVENT_PROJECTION_DESCRIPTION_INDEX);
            long begin = Long.parseLong(cur.getString(EVENT_PROJECTION_BEGIN_INDEX));
            long end = Long.parseLong(cur.getString(EVENT_PROJECTION_END_INDEX));
            cur.close();
            return new Calendar(id, title, description, begin, end);
        }
        return null;
    }

    private void insertEvent() throws SecurityException {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Don't have WRITE_CALENDAR permission.");
        }
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, mBegin);
        values.put(Events.DTEND, mEnd);
        String tz = TimeZone.getDefault().getID();
        values.put(Events.EVENT_TIMEZONE, tz);
        values.put(Events.TITLE, mTitle);
        values.put(Events.DESCRIPTION, mDescription);
        values.put(Events.CALENDAR_ID, mCalendarId);
        Uri uri = cr.insert(Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        mId = Long.parseLong(uri != null ? uri.getLastPathSegment() : "-1");
    }

    public void add() {
        Calendar last = null;
        try {
            last = getLastEvent();
        } catch (SecurityException e) {
            Log.e(mTag, e.getMessage());
        }
        // If no event in last 24H, just add current event.
        if (last == null) {
            String message = MessageFormat.format("" +
                    "No previous events in last 24H, added new event with title: {}\n"
                    + "begin time: {}", mTitle, mBegin);
            Log.d(mTag, message);
            insertEvent();
            return;
        }

        if (isSeparateEvents(last)) { // events are "far"(SEPARATE_EVENT) apart add new event
            String message = MessageFormat.format("" +
                    "Previous event \"far\" apart, added new event with title: {}\n"
                    + "begin time: {}", mTitle, mBegin);
            Log.d(mTag, message);
            insertEvent();
            return;
        } else { // events are less than SEPARATE_EVENT apart
            if (Objects.equals(mTitle, last.mTitle)) { // same event
                if (last.isZeroLengthEvent()) {
                    Log.d(mTag, "same event, last is zero - no need to do anything.");
                    return;
                } else {
                    String message = MessageFormat.format("" +
                            "Same event with title: {} just now.\n"
                            + "Extending it with end time: {}", mTitle, mBegin);
                    Log.d(mTag, message);
                    last.writeEndTime(mBegin);
                    return;
                }
            } else { // different events
                if (last.isMinimal()) { // less than MINIMAL_LISTENING_TIME
                    String message = MessageFormat.format("" +
                            "Last event with title: {} is minimal - deleting.\n"
                            + "Adding new event with new title: {}\n"
                            + "begin time: {}", last.mTitle, mTitle, mBegin);
                    Log.d(mTag, message);
                    last.delete();
                    insertEvent();
                    return;
                } else {
                    String message = MessageFormat.format(
                            "Closing last event with title: {} with end time: {}.\n"
                                    + "Adding new event with new title: {}, begin time: {}"
                            , last.mTitle, mBegin, mTitle, mBegin);
                    Log.d(mTag, message);
                    last.writeEndTime(mBegin);
                    insertEvent();
                    return;
                }
            }
        }
    }

    public void close() {
        Calendar last = null;
        try {
            last = getLastEvent();
        } catch (SecurityException e) {
            Log.e(mTag, e.getMessage());
        }
        // If no event in last 24H, nothing to do.
        if (last == null) {
            Log.d(mTag, "No event in the last 24H, nothing to close.");
            return;
        }

        if (Objects.equals(mTitle, last.mTitle)) { // Last event is the same
            if (last.isZeroLengthEvent()) {
                String message = MessageFormat.format(
                        "Closing last event with title: {} with end time: {}."
                        , last.mTitle, mBegin);
                Log.d(mTag, message);
                last.writeEndTime(mBegin);
                return;
            }
        }
    }

    private void writeEndTime(long endTime) throws SecurityException {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Don't have WRITE_CALENDAR permission.");
        }
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        Uri updateUri;
        // The new title for the event
        values.put(Events.DTEND, endTime);
        updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, mId);
        int rows = cr.update(updateUri, values, null, null);
        Log.d(mTag, "Rows updated: " + rows);
    }

    private void delete() throws SecurityException {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Don't have WRITE_CALENDAR permission.");
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri deleteUri;
        deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, mId);
        int rows = cr.delete(deleteUri, null, null);
        Log.d(mTag, "Rows deleted: " + rows);
    }
}
