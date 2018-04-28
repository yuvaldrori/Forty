package me.drori.forty;

import java.text.MessageFormat;

public class Event {

    private static final long NEW_EVENT = -1;
    private static final String UNKNOWN_APPLICATION = "Unknown application";

    private long begin;
    private long end;
    private long id;
    private boolean allDay;
    private String application;
    private String title;
    private String description;

    public Event(long begin, long end, long id, boolean allDay, String podcast, String episode) {
        this.begin = begin;
        this.end = end;
        this.id = id;
        this.allDay = allDay;
        this.title = podcast;
        this.description = episode;
        String[] lines = episode.split("\n");
        if (lines.length > 1) {
            this.application = lines[lines.length - 1];
        } else {
            this.application = UNKNOWN_APPLICATION;
        }

    }

    public Event(Notification notification) {
        this.begin = notification.getTime();
        this.end = notification.getTime();
        this.id = NEW_EVENT;
        this.allDay = true;
        this.application = notification.getApplication();
        String podcast = notification.getPodcast();
        String episode = notification.getEpisode();
        if (podcast.equals("")) {
            this.title = episode;
        } else {
            this.title = MessageFormat.format("{0}, {1}", notification.getEpisode(), notification.getPodcast());
        }
        this.description = MessageFormat.format("{0}\n{1}", this.title, this.application);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (begin != event.begin) return false;
        if (end != event.end) return false;
        if (id != event.id) return false;
        if (allDay != event.allDay) return false;
        if (!application.equals(event.application)) return false;
        //noinspection SimplifiableIfStatement
        if (!title.equals(event.title)) return false;
        return description.equals(event.description);

    }

    @Override
    public int hashCode() {
        int result = (int) (begin ^ (begin >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (allDay ? 1 : 0);
        result = 31 * result + application.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

    public boolean descriptionEquals(Event event) {
        return this.description.equals(event.description);
    }

    public String getApplication() {
        return application;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
