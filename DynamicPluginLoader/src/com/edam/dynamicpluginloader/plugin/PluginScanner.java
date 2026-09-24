package com.edam.dynamicpluginloader.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginScanner {

    public static List<Path> findPlugins(Path dirPath) throws IOException {
        if (!Files.exists(dirPath)) {
            throw new IOException("Plugins directory does not exist: " + dirPath);
        }
        if (!Files.isDirectory(dirPath)) {
            throw new NotDirectoryException(dirPath.toString());
        }
        try (Stream<Path> stream = Files.list(dirPath)) {
            return stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().toLowerCase().endsWith(".jar"))
                    .collect(Collectors.toList());
        }
    }
}