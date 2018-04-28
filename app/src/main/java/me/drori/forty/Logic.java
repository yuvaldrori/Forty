package me.drori.forty;

import java.util.ArrayList;
import java.util.List;

class Logic {

    private List<Event> events;
    private final Notification notification;

    public Logic(List<Event> events, Notification notification) {
        this.events = events;
        this.notification = notification;
    }

    public Event getEvent() {

        for (Event e : this.events) {
            if (e.descriptionEquals(new Event(notification))) {
                return null;
            }
        }

        return new Event(notification);
    }
}
