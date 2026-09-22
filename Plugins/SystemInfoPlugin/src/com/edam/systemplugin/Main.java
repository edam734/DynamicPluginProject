package com.edam.systemplugin;

import com.edam.pluginapi.Plugin;

public class Main {

    static void main() {
        Plugin plugin = new SystemPlugin();
        plugin.execute();
    }
}