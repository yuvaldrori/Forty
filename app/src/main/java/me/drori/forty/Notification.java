package me.drori.forty;

public class Notification {

    public static enum actions {
        START, STOP
    }

    private String application;
    private String title;
    private String text;
    private actions actions;
    private long time;

    public Notification(String application, String title, String text, Notification.actions actions, long time) {
        this.application = application;
        this.title = title;
        this.text = text;
        this.actions = actions;
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

    public Notification.actions getActions() {
        return actions;
    }

    public void setActions(Notification.actions actions) {
        this.actions = actions;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
