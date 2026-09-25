# DynamicPluginLoader

<br>

A small Java project that demonstrates **runtime plugin discovery, validation, dynamic class loading, and execution** using external JAR files.

The application scans a `plugins/` directory, identifies candidate JARs, validates their declared plugin class, loads valid plugins with dedicated class loaders, instantiates them through a shared API, and executes them.

<br>

## Requirements

- Java 25
- No Maven or external frameworks

<br>

## Project Structure

```text
DynamicPluginProject/
├── DynamicPluginLoader/
├── PluginAPI/
└── Plugins/
    ├── HelloPlugin/
    ├── TimePlugin/
    ├── SystemInfoPlugin/
    └── test/invalid plugin applications
```

The runtime plugin JARs are placed in:

```text
DynamicPluginLoader/plugins/
```

<br>

## Plugin API

All valid plugins implement the shared `Plugin` interface provided by `PluginAPI`.

Each plugin JAR declares its implementation class in `META-INF/MANIFEST.MF`:

```text
Plugin-Class: com.example.MyPlugin
```

The loader uses this entry to determine which class represents the plugin.

<br>

## How It Works

The application follows this flow:

```text
plugins/
   ↓
PluginScanner
   ↓
discover .jar files
   ↓
PluginLoader
   ↓
read MANIFEST.MF
   ↓
load Plugin-Class
   ↓
validate class
   ↓
instantiate plugin
   ↓
LoadedPlugin
   ↓
execute
```

<br>

### PluginScanner

`PluginScanner` is responsible only for discovering candidate JAR files in the configured plugins directory.

<br>

### PluginLoader

`PluginLoader` validates and loads one JAR at a time.

A plugin is considered valid when:

- the file is a readable JAR;
- its manifest contains a `Plugin-Class` entry;
- the declared class exists;
- the class implements the shared `Plugin` interface;
- the class is concrete and instantiable.

Classes are initially loaded without initialization so that static initialization blocks are not executed during validation.

<br>

### LoadedPlugin

A `LoadedPlugin` keeps together:

- the instantiated `Plugin`;
- the `URLClassLoader` that loaded it.

The class loader must remain alive while the plugin is in use because additional classes or resources from the plugin JAR may be loaded lazily.

<br>

## Error Handling

The application handles several invalid or failure cases.

Examples include:

- missing `plugins/` directory;
- unreadable or malformed JAR files;
- missing `Plugin-Class` manifest entry;
- declared classes that do not implement `Plugin`;
- classes that cannot be instantiated;
- runtime failures while executing a plugin.

Invalid plugins are ignored without preventing valid plugins from being loaded and executed.

<br>

## Example Plugins

The repository currently contains simple example plugins such as:

- `HelloPlugin`
- `TimePlugin`
- `SystemInfoPlugin`

Additional deliberately invalid plugin applications are also included to test validation behavior.

<br>

## Running

Build the example plugin JARs and place them in:

```text
DynamicPluginLoader/plugins/
```

Then run the `DynamicPluginLoaderApplication` class.

The application will discover, validate, load, and execute the valid plugins found in that directory.

<br>

## Future Work

The next major feature will be **runtime plugin discovery**.

Currently, plugins are discovered when the application performs its initial scan. In a future version, the application will monitor the `plugins/` directory during its entire lifecycle.

This will allow a new plugin JAR to be copied into the directory while the application is already running. The application will then:

```text
detect new JAR
   ↓
validate it
   ↓
load it with a new class loader
   ↓
instantiate the plugin
   ↓
execute it
```

This will likely be implemented using Java's file-system monitoring facilities, such as `WatchService`.

Further improvements may include safer plugin lifecycle management, plugin unloading/reloading, and verification of trusted plugin JARs.

<br>

# Fase 2
## Event-driven application lifecycle

The application has a continuous lifecycle, waiting for different types of events instead of executing only a short sequence in `main`.

To achieve this, different tasks can run through an `ExecutorService`:

* `PluginWatcher` — monitors the plugins directory using `WatchService`.
* `CommandListener` — waits for user commands.

These tasks essentially act as **event producers**. When they detect something, they place an event into a `BlockingQueue`, which acts as a thread-safe communication channel between the different threads.

```text
PluginWatcher ─── PluginAddedEvent ──┐
                                     ├──► BlockingQueue ───► Event Handler
CommandListener ── CommandEvent ─────┘
```

The thread consuming the `BlockingQueue` can block on:

```java
eventQueue.take();
```

While there are no events, the thread waits without unnecessarily consuming CPU. When an event arrives, it is processed and the application goes back to waiting for the next one.

Threads responsible for detecting events should do as little work as possible so that they can quickly return to their listening role.

With `WatchService`, even if a second JAR is placed in the directory while `PluginWatcher` is publishing the first event to the `BlockingQueue`, `WatchService` keeps the new events pending so they can be processed afterwards.

There are, however, two cases to keep in mind:

* `OVERFLOW`: if too many changes occur within a short period of time, `WatchService` may no longer be able to report every event precisely. In that case, the application should rescan the directory contents.
* A creation event may be triggered while a JAR is still being copied. Before loading the plugin, it is advisable to make sure that the file is fully available.

This architecture separates **event detection**, **inter-thread communication**, and **processing**, allowing the application to remain active and continuously react to new commands and changes in the plugins directory.

<br>
