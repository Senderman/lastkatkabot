package com.senderman.anitrackerbot;

public class Anime {
    private final String title;
    private final String series;
    private final String img;
    private final String url;

    public Anime(String title, String series, String img, String url) {
        this.title = title;
        this.series = series;
        this.img = img;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getSeries() {
        return series;
    }

    public String getImg() {
        return img;
    }

    public String getUrl() {
        return url;
    }
}
