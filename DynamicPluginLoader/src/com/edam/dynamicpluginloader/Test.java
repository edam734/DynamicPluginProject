package com.edam.dynamicpluginloader;

import com.edam.dynamicpluginloader.handler.EventHandler;
import com.edam.dynamicpluginloader.plugin.PluginRegistry;
import com.edam.dynamicpluginloader.watcher.PluginWatcher;
import com.edam.dynamicpluginloader.watcher.event.AppEvent;

import java.io.IOException;
import java.util.concurrent.*;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<AppEvent> eventQueue = new LinkedBlockingQueue<>(1000);
        PluginWatcher pluginWatcher = new PluginWatcher("plugins/", eventQueue);

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            executorService.submit(pluginWatcher);

            ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();
            PluginRegistry pluginRegistry = new PluginRegistry();
            EventHandler eventHandler = new EventHandler(retryExecutor, pluginRegistry);
            while (true) {
                AppEvent event = eventQueue.take();
                eventHandler.handle(event);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}