package me.drori.forty;

public class Logic {

    public static final long SEPARATE_EVENT = 61 * 1000; // one minute in ms

    private Event event;
    private Notification notification;

    public Logic(Event event, Notification notification) {
        this.event = event;
        this.notification = notification;
    }

    public Event getEvent() {
        if (notification.getActions().equals(Notification.actions.START) && event == null || !event.getApplication().equals(notification.getApplication()) || !new Event(notification).getDescription().equals(event.getDescription())) {
            return new Event(notification);
        }
        if (notification.getActions().equals(Notification.actions.STOP) && event.getBegin() == event.getEnd()) {
            event.setEnd(notification.getTime());
            return event;
        }
        return event;
    }
}
