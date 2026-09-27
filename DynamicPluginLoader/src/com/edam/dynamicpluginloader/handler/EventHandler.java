package com.edam.dynamicpluginloader.handler;

import com.edam.dynamicpluginloader.plugin.LoadedPlugin;
import com.edam.dynamicpluginloader.plugin.PluginLoader;
import com.edam.dynamicpluginloader.plugin.PluginRegistry;
import com.edam.dynamicpluginloader.watcher.event.AppEvent;
import com.edam.dynamicpluginloader.watcher.event.CommandEvent;
import com.edam.dynamicpluginloader.watcher.event.PluginEvent;

import java.nio.file.Paths;
import java.util.Optional;

public class EventHandler {

    private final PluginRegistry pluginRegistry;

    public EventHandler(PluginRegistry pluginRegistry) {
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
                Optional<LoadedPlugin> loadedPlugin = PluginLoader.INSTANCE.loadUnchecked(
                        Paths.get("plugins/" + event.path().toString()));

                loadedPlugin.ifPresent(plugin -> pluginRegistry.add(event.path(), plugin));
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

    private void handleUserInput(CommandEvent event) {
        // TODO
    }
}
