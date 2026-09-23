package com.edam.dynamicloader;

import com.edam.pluginapi.Plugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public enum PluginLoader {
    INSTANCE;

    public Optional<LoadedPlugin> loadUnchecked(Path jarPath) {
        try {
            return load(jarPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load plugin JAR: " + jarPath, e);
        }
    }

    /**
     * Loads a plugin from the specified JAR file.
     * <p>
     * The JAR must declare a {@code Plugin-Class} entry in its manifest,
     * and the referenced class must implement {@link Plugin} and be instantiable.
     *
     * @param jarPath path to the plugin JAR
     * @return an {@link Optional} containing the loaded plugin if the JAR is valid,
     * or {@link Optional#empty()} if it does not contain a valid plugin
     * @throws IOException if the JAR cannot be opened or read
     */
    public Optional<LoadedPlugin> load(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toString())) {
            Manifest manifest = jar.getManifest();
            String pluginClassName = manifest.getMainAttributes().getValue("Plugin-Class");
            if (null == pluginClassName) {
                return Optional.empty();
            } else {
                URL jarUrl = jarPath.toUri().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl},
                        Thread.currentThread().getContextClassLoader());
                Class<?> pluginClass = Class.forName(pluginClassName, false, classLoader);
                if (verifyPlugin(pluginClass)) {
                    Plugin plugin = pluginClass.asSubclass(Plugin.class)
                            .getConstructor()
                            .newInstance();
                    LoadedPlugin loadedPlugin = new LoadedPlugin(plugin, classLoader);
                    return Optional.of(loadedPlugin);
                }
                return Optional.empty();
            }
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not instantiate plugin", e);
        }
    }

    private boolean verifyPlugin(Class<?> clazz) {
        if (Plugin.class.isAssignableFrom(clazz)) {
            return isInstantiable(clazz);
        }
        return false;
    }

    private boolean isInstantiable(Class<?> clazz) {
        // 1. Basic type validations
        if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
            return false;
        }
        // 2. Checks if the class is abstract
        int modifier = clazz.getModifiers();
        if (Modifier.isAbstract(modifier)) {
            return false;
        }

        try {
            // Attempts to get the empty constructor (without parameters)
            Constructor<?> constructor = clazz.getConstructor();
            // Ensures that the constructor is indeed public
            return Modifier.isPublic(constructor.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}