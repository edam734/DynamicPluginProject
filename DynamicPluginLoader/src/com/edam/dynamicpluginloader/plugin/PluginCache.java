package com.edam.dynamicpluginloader.plugin;

import java.io.IOException;
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

    public Path copy(Path origin) throws IOException {
        Path target = cachePath.resolve(origin.getFileName());
        System.out.println("COPY START");
        Files.copy(origin, target, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("COPY END");
        return target;
    }

    public void delete(Path origin) throws IOException {
        Path target = cachePath.resolve(origin.getFileName());
        Files.delete(target);

    }
}
