package com.edam.timeplugin;

import com.edam.pluginapi.Plugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimePlugin implements Plugin {
    @Override
    public String getName() {
        return "Time Plugin";
    }

    @Override
    public void execute() {
        LocalDateTime myDateTime = LocalDateTime.now();
        DateTimeFormatter formatterUS = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.US);
        String formattedDate = myDateTime.format(formatterUS);
        IO.println(formattedDate);
    }
}
