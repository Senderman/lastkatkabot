package com.senderman.anitrackerbot;

import java.util.Map;

public interface DBService {

    void saveAnime(int id, int userId, String url);

    String getAnimeUrl(int id, int userId);

    void deleteAnime(int id, int userId);

    void dropUser(int userId);

    int totalAnimes(int userId);

    boolean idExists(int id, int userId);

    boolean urlExists(String url, int userId);

    Map<Integer, String> getAllAnimes(int userId);
}
