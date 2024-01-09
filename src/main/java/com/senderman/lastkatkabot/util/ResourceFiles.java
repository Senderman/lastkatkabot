package com.senderman.lastkatkabot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class ResourceFiles {
    public static List<String> getResourceFiles(String path) throws IOException {
        try (
                var in = ResourceFiles.class.getResourceAsStream(path);
                var br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in)))
        ) {
            return br.lines().toList();
        }
    }

    private static String extractName(String fileName) {
        int dotIndex = fileName.indexOf('.');
        return fileName.substring(0, dotIndex);
    }
}
