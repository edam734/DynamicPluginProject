package com.edam.dynamicpluginloader.watcher.event;

public record CommandEvent(String input) implements AppEvent {

    @Override
    public EventType getType() {
        return EventType.USER_INPUT;
    }

}