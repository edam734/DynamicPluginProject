package com.edam.dynamicloader;

import com.edam.pluginapi.Plugin;

import java.net.URLClassLoader;

/**
 * Holds a loaded plugin together with its class loader.
 * <p>
 * The class loader must remain open while the plugin is in use because
 * additional classes or resources from the plugin JAR may be loaded lazily.
 */
public record LoadedPlugin(Plugin plugin, URLClassLoader classLoader) implements AutoCloseable {
    @Override
    public void close() throws Exception {
        classLoader.close();
    }
}
