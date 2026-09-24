package com.edam.dynamicpluginloader.plugin;

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
import java.util.zip.ZipException;

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
                System.err.println("Field 'Plugin-Class' doesn't have a valid path to class Plugin: " + jarPath.getFileName());
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
                System.err.println("Ignoring invalid plugin: " + jarPath.getFileName());
            }
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            System.err.println("Class Plugin doesn't exist: " + jarPath.getFileName());
            return Optional.empty();
        } catch (ZipException e) {
            System.err.println("Invalid JAR: " + jarPath.getFileName());
            return Optional.empty();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not instantiate plugin", e);
        }
    }

    private boolean verifyPlugin(Class<?> clazz) {
        return Plugin.class.isAssignableFrom(clazz) && isInstantiable(clazz);
    }

    // Do Basic type validations
    // Checks if the class is abstract
    // Attempts to get the empty constructor (without parameters)
    // Ensures that the constructor is indeed public
    private boolean isInstantiable(Class<?> clazz) {
        if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
            return false;
        }
        int modifier = clazz.getModifiers();
        if (Modifier.isAbstract(modifier)) {
            return false;
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return Modifier.isPublic(constructor.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}