package com.edam.dynamicpluginloader;

import com.edam.dynamicpluginloader.handler.EventHandler;
import com.edam.dynamicpluginloader.plugin.PluginCache;
import com.edam.dynamicpluginloader.plugin.PluginManager;
import com.edam.dynamicpluginloader.plugin.PluginRegistry;
import com.edam.dynamicpluginloader.watcher.PluginWatcher;
import com.edam.dynamicpluginloader.watcher.event.AppEvent;

import java.io.IOException;
import java.util.concurrent.*;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        PluginRegistry pluginRegistry = new PluginRegistry();
        BlockingQueue<AppEvent> eventQueue = new LinkedBlockingQueue<>(1000);
        PluginWatcher pluginWatcher = new PluginWatcher("plugins/", eventQueue);

        try (ExecutorService watchService = Executors.newSingleThreadExecutor();
             ExecutorService pluginExecutor = Executors.newSingleThreadExecutor();
             ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor()) {

            try {
                watchService.submit(pluginWatcher);

                PluginCache cache = new PluginCache("cache");
                cache.initialize();

                PluginManager pluginManager = new PluginManager(pluginExecutor, retryExecutor, cache,
                        pluginRegistry);
                EventHandler eventHandler = new EventHandler(pluginManager);
                while (true) {
                    AppEvent event = eventQueue.take();
                    eventHandler.handle(event);
                }
            } finally {
                watchService.shutdownNow();
                pluginExecutor.shutdownNow();
                retryExecutor.shutdownNow();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            pluginRegistry.closeAll();
        }
    }
}