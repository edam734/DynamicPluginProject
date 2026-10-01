package com.edam.dynamicpluginloader.plugin;

import com.edam.dynamicpluginloader.util.FileHasher;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class PluginManager {

    final ExecutorService pluginExecutor;
    ScheduledExecutorService retryExecutor;
    final PluginCache pluginCache;
    final PluginRegistry pluginRegistry;
    private final Map<Path, ScheduledFuture<?>> retryTasks;

    public PluginManager(ExecutorService pluginExecutor, ScheduledExecutorService retryExecutor,
                         PluginCache pluginCache, PluginRegistry pluginRegistry) {
        this.pluginExecutor = pluginExecutor;
        this.retryExecutor = retryExecutor;
        this.pluginCache = pluginCache;
        this.pluginRegistry = pluginRegistry;
        this.retryTasks = new ConcurrentHashMap<>();
    }

    public void addPlugin(Path jarPath) {
        try {
            load(jarPath);
        } catch (UncheckedIOException e) {
            System.err.println("JAR file not yet available. Try again...");
            ScheduledFuture<?> loadDelayScheduled = retryExecutor.scheduleWithFixedDelay(
                    () -> retryLoad(jarPath), 500, 500, TimeUnit.MILLISECONDS);
            retryTasks.put(jarPath, loadDelayScheduled);
        }
    }

    public void updatePlugin(Path jarPath) throws IOException {
        LoadedPlugin plugin = pluginRegistry.get(jarPath);

        if (plugin != null) {
            String hash = FileHasher.calculateHash(jarPath);
            if (plugin.hash() != null && !plugin.hash().equals(hash)) {
                load(jarPath);
            }
        }
    }

    public void removePlugin(Path jarPath) {
        boolean exists = Files.exists(jarPath);
        if (!exists) {
            pluginRegistry.remove(jarPath);
        }
    }

    private void load(Path jarPath) {
        load(jarPath, () -> {
        });
    }

    private void load(Path jarPath, Runnable onSuccess) {
        Optional<LoadedPlugin> loadedPlugin = PluginLoader.INSTANCE.loadUnchecked(jarPath);
        loadedPlugin.ifPresent(plugin -> {
            pluginRegistry.add(jarPath, plugin);
            onSuccess.run();
        });
    }

    private void cancelTaskRetry(Path jarPath) {
        ScheduledFuture<?> future = retryTasks.remove(jarPath);
        if (null != future) {
            future.cancel(false);
        }
    }

    private void retryLoad(Path jarPath) {
        try {
            System.out.println("dentro do retry.");
            load(Paths.get(jarPath.toString()), () -> cancelTaskRetry(jarPath));
        } catch (UncheckedIOException e) {
            // Retry on next scheduled execution.
        }
    }
}
