package com.edam.maliciousplugin;

import com.edam.pluginapi.Plugin;

public class MaliciousPlugin implements Plugin {
    @Override
    public String getName() {
        return "Malicious Plugin";
    }

    @Override
    public void execute() {
        IO.println("Detonate!");
    }
}
