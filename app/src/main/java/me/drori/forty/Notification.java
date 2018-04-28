package me.drori.forty;

public class Notification {

    private String application;
    private String podcast;
    private String episode;
    private long time;

    public Notification(String application, String title, String text, long time) {
        this.application = application;
        this.podcast = title;
        this.episode = text;
        this.time = time;
    }

    public String getApplication() {
        return application;
    }

    public String getPodcast() {
        return podcast;
    }

    public String getEpisode() {
        return episode;
    }

    public long getTime() {
        return time;
    }

}
