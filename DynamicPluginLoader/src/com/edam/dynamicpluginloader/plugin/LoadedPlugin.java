package com.edam.dynamicpluginloader.plugin;

import com.edam.pluginapi.Plugin;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLClassLoader;

/**
 * Holds a loaded plugin together with its class loader.
 * <p>
 * The class loader must remain open while the plugin is in use because
 * additional classes or resources from the plugin JAR may be loaded lazily.
 */
public record LoadedPlugin(Plugin plugin, URLClassLoader classLoader) implements Closeable {
    @Override
    public void close() throws IOException {
        classLoader.close();
    }
}
