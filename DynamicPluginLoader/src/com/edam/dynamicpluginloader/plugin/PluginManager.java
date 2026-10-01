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

    public void addPlugin(Path origin) {
        pluginExecutor.submit(() -> doAddPlugin(origin));
    }

    private void doAddPlugin(Path origin) {
        try {
            copyAndLoad(origin);
        } catch (UncheckedIOException | IOException e) {
            System.err.println("JAR file not yet available. Try again...");
            ScheduledFuture<?> loadDelayTask = retryExecutor.scheduleWithFixedDelay(
                    () -> retryCopyAndLoad(origin), 500, 500, TimeUnit.MILLISECONDS);
            retryTasks.put(origin, loadDelayTask);
        }
    }

    public void updatePlugin(Path jarPath) {
        pluginExecutor.submit(() -> doUpdatePlugin(jarPath));
    }

    private void doUpdatePlugin(Path jarPath) {
        LoadedPlugin plugin = pluginRegistry.get(jarPath);

        if (plugin != null) {
            try {
                String hash = FileHasher.calculateHash(jarPath);
                copyAndLoadIfModified(jarPath, plugin.hash(), hash);
            } catch (IOException e) {
                System.err.println("Modified JAR file not yet available. Try again...");
                scheduleCalculateHashRetry(jarPath, plugin.hash());
            }
        }
    }

    public void removePlugin(Path jarPath) throws IOException {
        if (!Files.exists(jarPath)) {
            pluginRegistry.remove(jarPath);
            pluginCache.delete(jarPath);
        }
    }

    private void copyAndLoad(Path origin) throws IOException {
        copyAndLoad(origin, () -> {
        });
    }

    private void copyAndLoad(Path origin, Runnable onSuccess) {
        try {
            pluginRegistry.remove(origin);

            Path cachedPath = pluginCache.copy(origin);
            load(origin, cachedPath, onSuccess);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not copy plugin to cache", e);
        }
    }

    private void load(Path origin, Path target, Runnable onSuccess) {
        Optional<LoadedPlugin> loadedPlugin = PluginLoader.INSTANCE.loadUnchecked(target);
        loadedPlugin.ifPresent(plugin -> {
            pluginRegistry.add(origin, plugin);
            onSuccess.run();
        });
    }

    private void cancelRetryTask(Path origin) {
        ScheduledFuture<?> future = retryTasks.remove(origin);
        if (null != future) {
            future.cancel(false);
        }
    }

    private void retryCopyAndLoad(Path origin) {
        try {
            copyAndLoad(Paths.get(origin.toString()), () -> cancelRetryTask(origin));
        } catch (UncheckedIOException e) {
            // Retry on next scheduled execution.
        }
    }

    private void copyAndLoadIfModified(Path jarPath, String oldHash, String newHash) throws
            IOException {
        copyAndLoadIfModified(jarPath, oldHash, newHash, () -> {
        });
    }

    private void copyAndLoadIfModified(Path jarPath, String oldHash, String newHash,
                                       Runnable onSuccess) throws IOException {
        if (oldHash != null && !oldHash.equals(newHash)) {
            copyAndLoad(jarPath, onSuccess);
        }
    }

    private void scheduleCalculateHashRetry(Path origin, String oldHash) {
        ScheduledFuture<?> hashRetryTask = retryExecutor.scheduleWithFixedDelay(
                () -> retryCalculateHash(origin, oldHash), 500, 500, TimeUnit.MILLISECONDS);
        retryTasks.put(origin, hashRetryTask);
    }

    private void retryCalculateHash(Path origin, String oldHash) {
        try {
            String newHash = FileHasher.calculateHash(origin);
            copyAndLoadIfModified(origin, oldHash, newHash, () -> cancelRetryTask(origin));
        } catch (IOException e) {
            // Retry on next scheduled execution.
        }
    }
}
