package com.edam.dynamicpluginloader.watcher;

import com.edam.dynamicpluginloader.watcher.event.AppEvent;
import com.edam.dynamicpluginloader.watcher.event.PluginEvent;
import com.edam.dynamicpluginloader.watcher.event.PluginEventKind;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;

public class PluginWatcher implements Runnable {

    private final String dirName;
    private final BlockingQueue<AppEvent> eventQueue;

    public PluginWatcher(String dirName, BlockingQueue<AppEvent> eventQueue) {
        this.dirName = dirName;
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        try {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                registerDir(watchService, dirName);
                WatchKey watchKey;
                while ((watchKey = watchService.take()) != null) {
                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        Path fileName = (Path) event.context();
                        PluginEventKind pluginEventKind = mapKind(event.kind());
                        AppEvent newPluginEvent = new PluginEvent(fileName, pluginEventKind);
                        eventQueue.put(newPluginEvent);
                    }
                    // Mark the event as handled
                    if (!watchKey.reset()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PluginEventKind mapKind(WatchEvent.Kind<?> kind) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            return PluginEventKind.CREATED;
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            return PluginEventKind.DELETED;
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            return PluginEventKind.MODIFIED;
        } else {
            return PluginEventKind.OVERFLOW;
        }
    }

    private void registerDir(WatchService watchService, String dirName) throws IOException {
        Path pathToPluginsDir = Path.of(dirName);
        pathToPluginsDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
    }
}