package me.drori.forty;

public class Notification {

    public enum actions {
        START, STOP
    }

    private String application;
    private String title;
    private String text;
    private actions action;
    private long time;

    public Notification(String application, String title, String text, Notification.actions action, long time) {
        this.application = application;
        this.title = title;
        this.text = text;
        this.action = action;
        this.time = time;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Notification.actions getAction() {
        return action;
    }

    public void setAction(Notification.actions action) {
        this.action = action;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
