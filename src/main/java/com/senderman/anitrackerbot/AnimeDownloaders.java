package com.senderman.anitrackerbot;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class AnimeDownloaders {

    private static Map<String, String> anidubCookies = null;

    public static File getAnidubTorrent(String url) throws Exception {

        // login to anidub
        var conn = Jsoup.connect(url).method(Connection.Method.POST);
        if (anidubCookies != null) {
            conn.cookies(anidubCookies);
        } else {
            var username = System.getenv("aniuser");
            var password = System.getenv("anipass");
            conn.data("login_name", username, "login_password", password, "login", "submit");
        }
        var resp = conn.execute();
        if (anidubCookies == null)
            anidubCookies = resp.cookies();

        var page = resp.parse();
        var torrentDiv = Jsoup.parse(page.toString()).selectFirst("div.torrent_H");
        var torrentLink = torrentDiv.selectFirst("a").attr("href");
        torrentLink = "https://tr.anidub.com" + torrentLink;

        // download torrent after logging in
        var torrent = Jsoup.connect(torrentLink)
                .method(Connection.Method.GET)
                .cookies(anidubCookies)
                .ignoreContentType(true)
                .execute()
                .bodyStream();

        byte[] buffer = new byte[4096];
        var baos = new ByteArrayOutputStream();
        int length;
        while ((length = torrent.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        torrent.close();

        var file = new File(AnitrackerBot.torrentFile);
        var out = new FileOutputStream(file);
        baos.writeTo(out);
        out.close();
        baos.close();
        return file;

    }
}
