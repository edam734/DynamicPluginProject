package com.edam.dynamicpluginloader.watcher.event;

import java.nio.file.Path;

public record PluginEvent(Path path, PluginEventKind kind) implements AppEvent {

    @Override
    public EventType getType() {
        return EventType.PLUGIN;
    }
}