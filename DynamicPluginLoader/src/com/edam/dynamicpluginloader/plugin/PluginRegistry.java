package com.edam.dynamicpluginloader.plugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginRegistry {
    private final Map<Path, LoadedPlugin> loadedPlugins = new ConcurrentHashMap<>();

    /**
     * Adds the plugin to the registry. If the registry previously contained a mapping for the key,
     * the old plugin is replaced by the specified plugin and its associated resources are closed.
     *
     * @param jarName the path used as the registry key
     * @param plugin  the plugin to associate with the specified path
     * @see Map#put(Object, Object)
     */
    public void add(Path jarName, LoadedPlugin plugin) {
        LoadedPlugin existingPlugin = loadedPlugins.put(jarName, plugin);
        if (existingPlugin != null && existingPlugin != plugin) {
            try {
                existingPlugin.close();
            } catch (IOException e) {
                System.err.println("Could not close plugin: " + e.getMessage());
            }
        }
    }

    /**
     * Returns the plugin associated with the specified JAR path, or {@code null} if the registry
     * contains no mapping for that path.
     *
     * @param jarName the path whose associated plugin is to be returned
     * @return the plugin associated with the specified path, or {@code null} if no mapping exists
     * @see Map#get(Object)
     */
    public LoadedPlugin get(Path jarName) {
        return loadedPlugins.get(jarName);
    }

    /**
     * Removes the plugin associated with the specified JAR path from the registry. If a plugin is
     * found, its associated resources are closed before the plugin is returned.
     *
     * @param jarName the path whose associated plugin is to be removed
     * @return the removed plugin, or {@code null} if no mapping existed
     * @see Map#remove(Object)
     */
    public LoadedPlugin remove(Path jarName) {
        LoadedPlugin plugin = loadedPlugins.remove(jarName);

        if (plugin != null) {
            close(plugin);
        }
        return plugin;
    }

    private void close(LoadedPlugin plugin) {
        try {
            plugin.close();
        } catch (IOException e) {
            System.err.println("Could not close plugin: " + e.getMessage());
        }
    }

    /**
     * Returns an unmodifiable collection view of all plugins currently stored in the registry.
     * Changes made to the registry are reflected in the returned collection, but the collection
     * itself cannot be modified.
     *
     * @return an unmodifiable collection view of all loaded plugins
     * @see Map#values()
     * @see Collections#unmodifiableCollection(Collection)
     */
    public Collection<LoadedPlugin> getAll() {
        return Collections.unmodifiableCollection(loadedPlugins.values());
    }

    /**
     * Closes all loaded plugins and removes them from the registry.
     * Each plugin is closed before the registry is cleared.
     */
    public void closeAll() {
        loadedPlugins.values().forEach(this::close);
        loadedPlugins.clear();
    }

    /**
     * Returns the number of plugins currently stored in the registry.
     *
     * @return the number of loaded plugins
     * @see Map#size()
     */
    public int size() {
        return this.loadedPlugins.size();
    }
}
