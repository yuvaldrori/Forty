package me.drori.forty;

public class Notification {

    private String application;
    private String title;
    private String text;
    private long time;

    public Notification(String application, String title, String text, long time) {
        this.application = application;
        this.title = title;
        this.text = text;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
