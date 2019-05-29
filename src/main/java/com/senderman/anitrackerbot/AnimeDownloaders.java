package com.senderman.anitrackerbot;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        var file = new File("anime.torrent");
        var out = new FileOutputStream(file);
        baos.writeTo(out);
        out.close();
        baos.close();
        return file;

    }

    public static File getAnistarTorrent(String url) throws Exception {
        var doc = Jsoup.parse(new URL(url), 10000);
        var torrentList = doc.selectFirst("div.list_torrent").select("div.torrent");
        Set<File> torrentFiles = new HashSet<>();
        var torrentDir = new File("torrents");
        torrentDir.mkdir();

        // download all torrents
        for (var torrent : torrentList) {
            var name = torrent.selectFirst("div.info_d1").text();
            var in = new URL("https://anistar.org" + torrent.selectFirst("a").attr("href")).openStream();
            var baos = new ByteArrayOutputStream();
            int length;
            byte[] buffer = new byte[4096];
            while ((length = in.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            in.close();
            var file = new File("torrents/" + name + ".torrent");
            var fos = new FileOutputStream(file);
            baos.writeTo(fos);
            fos.flush();
            fos.close();
            baos.close();
            torrentFiles.add(file);
        }

        // archive all files
        var zipFile = new File("anime.zip");
        var zout = new ZipOutputStream(new FileOutputStream(zipFile));
        for (var file : torrentFiles) {
            zout.putNextEntry(new ZipEntry(file.getName()));
            var fis = new FileInputStream(file);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            zout.write(buffer);
            fis.close();
            file.delete();
            zout.closeEntry();
        }
        zout.flush();
        zout.close();
        torrentDir.delete();
        return zipFile;
    }
}
