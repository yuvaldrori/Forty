package me.drori.forty;

import java.text.MessageFormat;

public class Event {

    private long begin;
    private long end;
    private long id;
    private String application;
    private String title;
    private String description;

    public Event(long begin, long end, long id, String title, String description) {
        this.begin = begin;
        this.end = end;
        this.id = id;
        this.title = title;
        this.description = description;
        this.application = description.split("\n")[1];
    }

    public Event(Notification notification) {
        this.begin = notification.getTime();
        this.end = notification.getTime();
        this.id = -1;
        this.application = notification.getApplication();
        this.title = MessageFormat.format("{0} {1}", notification.getText(), notification.getTitle());
        this.description = MessageFormat.format("{0}\n{1}", this.title, this.application);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (begin != event.begin) return false;
        if (end != event.end) return false;
        if (!application.equals(event.application)) return false;
        if (!title.equals(event.title)) return false;
        return description.equals(event.description);

    }

    @Override
    public int hashCode() {
        int result = (int) (begin ^ (begin >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        result = 31 * result + application.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
