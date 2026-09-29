package com.edam.dynamicpluginloader.handler;

import com.edam.dynamicpluginloader.plugin.LoadedPlugin;
import com.edam.dynamicpluginloader.plugin.PluginLoader;
import com.edam.dynamicpluginloader.plugin.PluginRegistry;
import com.edam.dynamicpluginloader.watcher.event.AppEvent;
import com.edam.dynamicpluginloader.watcher.event.CommandEvent;
import com.edam.dynamicpluginloader.watcher.event.PluginEvent;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EventHandler {

    private final ScheduledExecutorService retryExecutor;
    private final Map<Path, ScheduledFuture<?>> retryTasks;
    private final PluginRegistry pluginRegistry;

    public EventHandler(ScheduledExecutorService retryExecutor, PluginRegistry pluginRegistry) {
        this.retryExecutor = retryExecutor;
        this.retryTasks = new ConcurrentHashMap<>();
        this.pluginRegistry = pluginRegistry;
    }

    public void handle(AppEvent event) {
        switch (event.getType()) {
            case PLUGIN -> handlePluginEvent((PluginEvent) event);
            case USER_INPUT -> handleUserInput((CommandEvent) event);
        }
    }

    private void handlePluginEvent(PluginEvent event) {
        switch (event.kind()) {
            case CREATED -> {
                Path jarPath = Paths.get("plugins/" + event.path().toString());
                try {
                    load(jarPath);
                } catch (UncheckedIOException e) {
                    System.err.println("JAR file not yet available. Try again...");
                    ScheduledFuture<?> loadDelayScheduled = retryExecutor.scheduleWithFixedDelay(
                            () -> retryLoad(jarPath), 500, 500, TimeUnit.MILLISECONDS);
                    retryTasks.put(jarPath, loadDelayScheduled);
                }
            }
/*            case MODIFIED -> {
                // TODO
            }
            case DELETED -> {
                // TODO
            }
            case OVERFLOW -> {
                // TODO
            }*/
        }
    }

    private void load(Path jarPath) {
        Optional<LoadedPlugin> loadedPlugin = PluginLoader.INSTANCE.loadUnchecked(jarPath);
        loadedPlugin.ifPresent(plugin -> {
            pluginRegistry.add(jarPath, plugin);
            ScheduledFuture<?> future = retryTasks.remove(jarPath);
            if (null != future) {
                future.cancel(false);
            }
        });
    }

    private void retryLoad(Path jarPath) {
        try {
            System.out.println("dentro do retry.");
            load(Paths.get(jarPath.toString()));
        } catch (UncheckedIOException e) {
            // Retry on next scheduled execution.
        }
    }

    private void handleUserInput(CommandEvent event) {
        // TODO
    }
}
