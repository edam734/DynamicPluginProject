package com.edam.maliciousplugin;

import com.edam.pluginapi.Plugin;

public class Main {

    static void main(String[] args) {
        Plugin maliciousPlugin = new MaliciousPlugin();
        maliciousPlugin.execute();
    }
}
