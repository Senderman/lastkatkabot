package com.senderman.anitrackerbot

import org.jsoup.Jsoup
import java.net.URL

internal object AnimeParsers {
    @Throws(Exception::class)
    fun parseAnidub(url: String?): Anime {
        val doc = Jsoup.parse(URL(url), 10000)
        val title = doc.selectFirst("title").text()
        val titleOfAnime = title.replace(" \\[.*".toRegex(), "")
        val series = title.substring(title.lastIndexOf("[") + 1, title.lastIndexOf("]"))
        val sDone = series.replace("^(\\d+).*".toRegex(), "$1").toInt()
        val sTotal = series.replace(".* (\\d+).*$".toRegex(), "$1").toInt()
        val percentage = 100 * sDone / sTotal
        val seriesFinal = "$sDone/$sTotal ($percentage%)"
        val posterSpan = doc.selectFirst("span.poster")
        val img = posterSpan.selectFirst("img").attr("src")
        return Anime(titleOfAnime, seriesFinal, img, url!!)
    }

    @Throws(Exception::class)
    fun parseAnimerost(url: String?): Anime {
        val doc = Jsoup.parse(URL(url), 10000)
        val titleDiv = doc.selectFirst("div.shortstoryHead")
        val title = titleDiv.selectFirst("h1").text()
        val titleOfAnime = title.replace("\\[.*".toRegex(), "")
        val series = title.substring(title.indexOf("[") + 1, title.indexOf("]"))
        val sDone = series.replace(".*-(\\d+).*".toRegex(), "$1").toInt()
        val sTotal = series.replace(".* (\\d+).*$".toRegex(), "$1").toInt()
        val percentage = 100 * sDone / sTotal
        val seriesFinal = "$sDone/$sTotal ($percentage%)"
        val img = doc.selectFirst("img.imgRadius").attr("src")
        return Anime(titleOfAnime, seriesFinal, "https://animerost.org$img", url!!)
    }

    @Throws(Exception::class)
    fun parseGidfilm(url: String?): Anime {
        val doc = Jsoup.parse(URL(url), 10000)
        val title = doc.selectFirst("h1#anime-l").text()
        val series = doc.selectFirst("span#count-video").text()
        val img = doc.selectFirst("img#avatar").attr("src")
        return Anime(title, series, img, url!!)
    }

    @Throws(Exception::class)
    fun parseSMAnime(url: String?): Anime {
        val doc = Jsoup.parse(URL(url), 10000)
        val title = doc.selectFirst("h2.line-2").text().replace(" смотреть онлайн", "")
        val posterDiv = doc.selectFirst("div.m-catalog-item__poster")
        val posterLink = posterDiv.selectFirst("a")
        val img = posterLink.selectFirst("img").attr("src")
        return Anime(title, "Неизвестно", "https://smotretanime.ru$img", url!!)
    }

    @Throws(Exception::class)
    fun parseAnistar(url: String?): Anime {
        val doc = Jsoup.parse(URL(url), 10000)
        val title = doc.selectFirst("div.title_left").selectFirst("h1").text()
        val seriesUl = doc.selectFirst("ul.head")
        val series = seriesUl.text().replace(".*Серии: ".toRegex(), "")
        var img = doc.selectFirst("img.main-img").attr("src")
        img = "https://anistar.org$img"
        return Anime(title, series, img, url!!)
    }

    @Throws(Exception::class)
    fun parseNyaasi(url: String?): Anime {
        val doc = Jsoup.parse(URL(url), 10000)
        val title = doc.selectFirst("div.panel-heading").selectFirst("h3.panel-title").text()
        val img = "https://pbs.twimg.com/profile_images/865586129059201024/fH1dmIuo_400x400.jpg"
        return Anime(title, "N/A", img, url!!)
    }
}