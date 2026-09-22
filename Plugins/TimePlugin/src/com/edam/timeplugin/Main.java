package com.edam.timeplugin;

import com.edam.pluginapi.Plugin;

public class Main {

    static void main(String[] args) {
        Plugin timePlugin = new TimePlugin();
        timePlugin.execute();
    }
}
