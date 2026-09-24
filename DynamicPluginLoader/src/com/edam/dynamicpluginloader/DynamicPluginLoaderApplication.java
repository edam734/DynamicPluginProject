package com.edam.dynamicpluginloader;

import com.edam.dynamicpluginloader.plugin.LoadedPlugin;
import com.edam.dynamicpluginloader.plugin.PluginLoader;
import com.edam.dynamicpluginloader.plugin.PluginScanner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Entry point for the Dynamic Plugin Loader application.
 * <p>
 * The application discovers plugin JARs at runtime, validates their
 * declared plugin classes, loads them through dedicated class loaders,
 * and exposes them through the common Plugin API.
 */
public class DynamicPluginLoaderApplication {

    public static void main(String[] args) throws IOException {
        Path pluginsDir = Paths.get("plugins");
        List<Path> paths = PluginScanner.findPlugins(pluginsDir);
        List<LoadedPlugin> plugins = paths.stream()
                .map(PluginLoader.INSTANCE::loadUnchecked)
                .flatMap(Optional::stream)
                .toList();

        // let's execute
        for (LoadedPlugin loadedPlugin : plugins) {
            System.out.printf("%n-------------%n%n");
            try {
                try (loadedPlugin) {
                    loadedPlugin.plugin().execute();
                    System.out.println(
                            ">> " + loadedPlugin.plugin().getName() + " executed with Success!");
                }
            } catch (RuntimeException e) {
                System.err.println("Plugin execution failed: " +
                        loadedPlugin.plugin().getName() +
                        " - " +
                        e.getMessage());
            }
        }
    }
}