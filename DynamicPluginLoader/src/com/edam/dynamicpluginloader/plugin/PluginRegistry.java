package com.edam.dynamicpluginloader.plugin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginRegistry {
    private final Map<Path, LoadedPlugin> loadedPlugins = new ConcurrentHashMap<>();

    public void add(Path jarName, LoadedPlugin plugin) {
        loadedPlugins.put(jarName, plugin);
    }

    public LoadedPlugin get(Path jarName) {
        return loadedPlugins.get(jarName);
    }

    public LoadedPlugin remove(Path jarName) {
        return loadedPlugins.remove(jarName);
    }

    public Collection<LoadedPlugin> getAll() {
        return loadedPlugins.values();
    }

    public int size() {
        return this.loadedPlugins.size();
    }
}
