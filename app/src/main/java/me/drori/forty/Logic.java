package me.drori.forty;

import java.util.ArrayList;
import java.util.List;

class Logic {

    public static final long SEPARATE_EVENT = 61 * 1000; // one minute in ms

    private final Event event;
    private final Notification notification;

    public Logic(Event event, Notification notification) {
        this.event = event;
        this.notification = notification;
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        if (notification.getAction().equals(Notification.actions.START)) { // start event

            if (
                // no event in last 24h
                    event == null
                            // not the same app
                            || (!event.getApplication().equals(notification.getApplication()) || event.getApplication().equals(Event.UNKNOWN_APPLICATION))
                    ) {
                events.add(new Event(notification));
                return events;
            }

            if (!new Event(notification).getDescription().equals(event.getDescription())) {
                if (event.getBegin() == event.getEnd()) {
                    event.setEnd(notification.getTime());
                    events.add(event);
                    events.add(new Event(notification));
                    return events;
                }
            }

            if (event.getBegin() != event.getEnd()) {
                if ((notification.getTime() - event.getEnd()) < SEPARATE_EVENT) {
                    event.setEnd(event.getBegin());
                    events.add(event);
                    return events;
                } else {
                    events.add(new Event(notification));
                    return events;
                }
            }
        } else { // stop event
            if (notification.getAction().equals(Notification.actions.STOP) && event != null && event.getBegin() == event.getEnd()) {
                event.setEnd(notification.getTime());
                events.add(event);
                return events;
            }
        }

        return events;
    }
}
