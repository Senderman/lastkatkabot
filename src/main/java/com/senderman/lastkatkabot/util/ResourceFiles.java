package com.senderman.lastkatkabot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class ResourceFiles {

    /**
     * List path to all resources in the specified resource directory
     *
     * @param path base path
     * @return list of resource paths
     * @throws IOException if unable to access resource path
     */
    public static List<String> listResourcePaths(String path) throws IOException {
        try (
                var in = ResourceFiles.class.getResourceAsStream(path);
                var br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in)))
        ) {
            return br.lines().toList();
        }
    }

}
