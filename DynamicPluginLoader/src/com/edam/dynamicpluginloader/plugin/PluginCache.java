package com.edam.dynamicpluginloader.plugin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PluginCache {

    Path cachePath;

    public PluginCache(String cachePath) {
        this.cachePath = Path.of(cachePath);
    }

    public void initialize() throws IOException {
        if (!Files.exists(cachePath)) {
            Files.createDirectories(cachePath);
        }
    }

    public void copy(Path origin) throws IOException {
        InputStream inputStream = new BufferedInputStream(Files.newInputStream(origin));
        Files.copy(inputStream, cachePath.resolve(origin.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
