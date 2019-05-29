package com.senderman.anitrackerbot;

import org.jsoup.Jsoup;

import java.net.URL;

public class AnimeParsers {

    public static Anime parseAnidub(String url) throws Exception {
        var doc = Jsoup.parse(new URL(url), 10000);
        var title = doc.selectFirst("title").text();
        var titleOfAnime = title.replaceAll(" \\[.*", "");

        var series = title.substring(title.indexOf("[") + 1, title.indexOf("]"));
        int sDone = Integer.parseInt(series.replaceAll("^(\\d+).*", "$1"));
        int sTotal = Integer.parseInt(series.replaceAll(".* (\\d+).*$", "$1"));
        int percentage = 100 * sDone / sTotal;
        var seriesFinal = sDone + "/" + sTotal + " (" + percentage + "%)";

        var posterSpan = doc.selectFirst("span.poster");
        var img = posterSpan.selectFirst("img").attr("src");
        return new Anime(titleOfAnime, seriesFinal, img, url);
    }

    public static Anime parseAnimerost(String url) throws Exception {
        var doc = Jsoup.parse(new URL(url), 10000);
        var titleDiv = doc.selectFirst("div.shortstoryHead");
        var title = titleDiv.selectFirst("h1").text();
        var titleOfAnime = title.replaceAll("\\[.*", "");

        var series = title.substring(title.indexOf("[") + 1, title.indexOf("]"));
        int sDone = Integer.parseInt(series.replaceAll(".*-(\\d+).*", "$1"));
        int sTotal = Integer.parseInt(series.replaceAll(".* (\\d+).*$", "$1"));
        int percentage = 100 * sDone / sTotal;
        var seriesFinal = sDone + "/" + sTotal + " (" + percentage + "%)";

        var img = doc.selectFirst("img.imgRadius").attr("src");
        return new Anime(titleOfAnime, seriesFinal, "https://animerost.org" + img, url);
    }

    public static Anime parseGidfilm(String url) throws Exception {
        var doc = Jsoup.parse(new URL(url), 10000);
        var title = doc.selectFirst("h1#anime-l").text();
        var series = doc.selectFirst("span#count-video").text();
        var img = doc.selectFirst("img#avatar").attr("src");
        return new Anime(title, series, img, url);
    }

    public static Anime parseSMAnime(String url) throws Exception {
        var doc = Jsoup.parse(new URL(url), 10000);
        var title = doc.selectFirst("h2.line-2").text().replace(" смотреть онлайн", "");

        var posterDiv = doc.selectFirst("div.m-catalog-item__poster");
        var posterLink = posterDiv.selectFirst("a");
        var img = posterLink.selectFirst("img").attr("src");
        return new Anime(title, "Неизвестно", "https://smotretanime.ru" + img, url);
    }

    public static Anime parseAnistar(String url) throws Exception {
        var doc = Jsoup.parse(new URL(url), 10000);
        var title = doc.selectFirst("div.title_left").selectFirst("h1").text();
        var seriesUl = doc.selectFirst("ul.head");
        var series = seriesUl.text().replaceAll(".*Серии: ", "");
        var img = doc.selectFirst("img.main-img").attr("src");
        img = "https://anistar.org" + img;
        return new Anime(title, series, img, url);
    }
}
