package me.drori.forty;

import java.util.ArrayList;
import java.util.List;

public class Logic {

    public static final long SEPARATE_EVENT = 61 * 1000; // one minute in ms

    private Event event;
    private Notification notification;

    public Logic(Event event, Notification notification) {
        this.event = event;
        this.notification = notification;
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<Event>();
        if (
                notification.getActions().equals(Notification.actions.START) && (
                        event == null
                                || !event.getApplication().equals(notification.getApplication())
                                || (!new Event(notification).getDescription().equals(event.getDescription()) && !(event.getBegin() == event.getEnd()))
                                || !((notification.getTime() - event.getEnd()) < SEPARATE_EVENT)
                )
                ) {
            events.add(new Event(notification));
            return events;
        }
        if (notification.getActions().equals(Notification.actions.START) && !new Event(notification).getDescription().equals(event.getDescription())) {
            if (event.getBegin() == event.getEnd()) {
                event.setEnd(notification.getTime());
                events.add(event);
                events.add(new Event(notification));
                return events;
            }
        }
        if (notification.getActions().equals(Notification.actions.STOP) && event.getBegin() == event.getEnd()) {
            event.setEnd(notification.getTime());
            events.add(event);
            return events;
        }
        if (notification.getActions().equals(Notification.actions.START)) {
            if ((notification.getTime() - event.getEnd()) < SEPARATE_EVENT) {
                event.setEnd(event.getBegin());
                events.add(event);
                return events;
            }
        }
        return events;
    }
}
