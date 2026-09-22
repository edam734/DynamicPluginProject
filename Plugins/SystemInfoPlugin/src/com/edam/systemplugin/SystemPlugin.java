package com.edam.systemplugin;

import com.edam.pluginapi.Plugin;

public class SystemPlugin implements Plugin {
    @Override
    public String getName() {
        return "System Plugin";
    }

    @Override
    public void execute() {
        IO.println("=== OPERATING SYSTEM ===");
        IO.println("SO: " + System.getProperty("os.name"));
        IO.println("SO version: " + System.getProperty("os.version"));
        IO.println("Architecture: " + System.getProperty("os.arch"));
        IO.println("Current User: " + System.getProperty("user.name"));
        IO.println("Home: " + System.getProperty("user.home"));
        IO.println("Working Directory: " + System.getProperty("user.dir"));

        // 2. JVM; info
        IO.println("\n=== JAVA ENVIRONMENT ===");
        IO.println("Java version: " + System.getProperty("java.version"));
        IO.println("JDK vendor: " + System.getProperty("java.vendor"));
        IO.println("Installation folder: " + System.getProperty("java.home"));

        // 3. Memory and Hardware resources
        Runtime runtime = Runtime.getRuntime();
        int processors = runtime.availableProcessors();
        long memoryMax = runtime.maxMemory() / (1024 * 1024); // Convertido para MB
        long memoryTotal = runtime.totalMemory() / (1024 * 1024);
        long memoryFree = runtime.freeMemory() / (1024 * 1024);

        IO.println("\n=== HARDWARE AND RESOURCES ===");
        IO.println("Available CPU cores: " + processors);
        IO.println("Maximum allocatable memory to JVM: " + memoryMax + " MB");
        IO.println("Total memory currently allocated: " + memoryTotal + " MB");
        IO.println("Free memory within the JVM: " + memoryFree + " MB");

        // 4. CURRENT PROCESS DATA (ProcessHandle - Java 9+)
        ProcessHandle currentProcess = ProcessHandle.current();
        IO.println("\n=== CURRENT PROCESS ===");
        IO.println("Process ID (PID): " + currentProcess.pid());
        currentProcess.info().command().ifPresent(cmd -> IO.println("Command executed: " + cmd));
        currentProcess.info()
                .startInstant()
                .ifPresent(instante -> IO.println("Started at: " + instante));
    }
}
