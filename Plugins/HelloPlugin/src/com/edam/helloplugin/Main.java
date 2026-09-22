package com.edam.helloplugin;

import com.edam.pluginapi.Plugin;

public class Main {

    static void main() {
        Plugin plugin = new HelloPlugin();
        plugin.execute();
    }
}