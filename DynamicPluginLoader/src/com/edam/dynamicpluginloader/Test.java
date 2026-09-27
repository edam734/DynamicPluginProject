package com.edam.dynamicpluginloader;

import com.edam.dynamicpluginloader.watcher.PluginWatcher;
import com.edam.dynamicpluginloader.watcher.event.AppEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<AppEvent> eventQueue = new LinkedBlockingQueue<>(1000);
        PluginWatcher pluginWatcher = new PluginWatcher("plugins/", eventQueue);

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            executorService.submit(pluginWatcher);

            while (true) {
                AppEvent event = eventQueue.take();
                System.out.println(event);
            }
        }
    }
}