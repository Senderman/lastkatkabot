package com.senderman.anitrackerbot

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object AnimeDownloaders {
    private var anidubCookies: Map<String, String>? = null

    @Throws(IOException::class)
    private fun downloadFile(inp: InputStream): File {
        val buffer = ByteArray(4096)
        val file = File("anime.torrent")
        val fos = FileOutputStream(file)
        var length: Int
        while (inp.read(buffer).also { length = it } != -1) {
            fos.write(buffer, 0, length)
        }
        inp.close()
        fos.close()
        return file
    }

    @Throws(Exception::class)
    fun getAnidubTorrent(url: String): File { // login to anidub
        val conn = Jsoup.connect(url).method(Connection.Method.POST)
        if (anidubCookies != null) {
            conn.cookies(anidubCookies)
        } else {
            val account = Services.botConfig.anidata.split(":")
            val username = account[0]
            val password = account[1]
            conn.data("login_name", username, "login_password", password, "login", "submit")
        }
        val resp = conn.execute()
        if (anidubCookies == null) anidubCookies = resp.cookies()

        // download torrent after logging in
        val page = resp.parse()
        val torrentDiv = Jsoup.parse(page.toString()).selectFirst("div.torrent_H")
        var torrentLink = torrentDiv.selectFirst("a").attr("href")
        torrentLink = "https://tr.anidub.com$torrentLink"
        val torrent = Jsoup.connect(torrentLink)
            .method(Connection.Method.GET)
            .cookies(anidubCookies)
            .ignoreContentType(true)
            .execute()
            .bodyStream()
        return downloadFile(torrent)
    }

    @Throws(Exception::class)
    fun getAnistarTorrent(url: String): File {
        val doc = Jsoup.parse(URL(url), 10000)
        val torrentList = doc.selectFirst("div.list_torrent").select("div.torrent")
        // download all torrents to zip
        val zipFile = File("anime.zip")
        val zout = ZipOutputStream(FileOutputStream(zipFile))
        for (torrent in torrentList) {
            val name = torrent.selectFirst("div.info_d1").text() + ".torrent"
            val inp = URL("https://anistar.org" + torrent.selectFirst("a").attr("href")).openStream()
            var length: Int
            val buffer = ByteArray(4096)
            zout.putNextEntry(ZipEntry(name))
            while (inp.read(buffer).also { length = it } != -1) {
                zout.write(buffer, 0, length)
            }
            inp.close()
            zout.closeEntry()
        }
        zout.close()
        return zipFile
    }

    @Throws(Exception::class)
    fun getNyaasiTorrent(url: String): File {
        val doc = Jsoup.parse(URL(url), 10000)
        val torrentlink = "https://nyaa.si" + doc.selectFirst("div.panel-footer").selectFirst("a").attr("href")
        val u = URL(torrentlink)
        val inp = u.openStream()
        return downloadFile(inp)
    }
}