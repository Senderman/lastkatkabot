package com.senderman.anitrackerbot;

import java.io.File;

public interface AnimeDownloader {

    File download(String url) throws Exception;
}
