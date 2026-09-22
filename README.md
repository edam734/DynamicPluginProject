# DynamicPluginLoader

<br>

A small Java project for exploring **dynamic class loading** and plugin-based architectures.

The application discovers plugin JAR files at runtime, loads their classes dynamically, and executes plugins through a common interface without knowing their concrete implementations at compile time.

<br>

## Goals

The project is intended to explore:

- Dynamic class loading
- `ClassLoader`
- `Class.forName()`
- Java Reflection
- Runtime loading of external JAR files
- Interface-based plugin design

<br>

## Plugin Directory

External plugins are placed in the `plugins/` directory:

```text
DynamicPluginLoader/
├── src/
├── plugins/
│   ├── plugin-one.jar
│   └── plugin-two.jar
└── README.md
```

<br>
