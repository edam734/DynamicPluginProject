import com.edam.pluginapi.Plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    static void main(String[] args) throws IOException {
        Path path = Paths.get("plugins");
        Set<Path> plugins = getPlugins(path);
        plugins.forEach(plugin -> System.out.println(plugin.getFileName()));
    }

    private static Set<Path> getPlugins(Path pathToDir) throws IOException {
        try (Stream<Path> stream = Files.list(pathToDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().toLowerCase().endsWith(".jar"))
                    .filter(path -> {
                        try {
                            return verifyJar(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());
        }
    }

    private static boolean verifyJar(Path jarPath) throws IOException {
        boolean isValid = false;
        try (JarFile jar = new JarFile(jarPath.toString())) {
            Manifest manifest = jar.getManifest();
            String pluginClassName = manifest.getMainAttributes().getValue("Plugin-Class");


            URL jarUrl = jarPath.toUri().toURL();
            Class<?> pluginClass = getPluginClass(jarUrl, pluginClassName);
            // implements the class com.edam.pluginapi.Plugin
            if (Plugin.class.isAssignableFrom(pluginClass)) {
                // checks if this class is instantiable
                isValid = isInstantiable(pluginClass);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return isValid;
    }

    private static Class<?> getPluginClass(URL jarUrl, String className) throws IOException,
            ClassNotFoundException {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl},
                Thread.currentThread().getContextClassLoader())) {
            return Class.forName(className, false, classLoader);
        }
    }

    private static boolean isInstantiable(Class<?> clazz) {
        // 1. Basic type validations
        if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
            return false;
        }
        // 2. Checks if the class is abstract
        int modifier = clazz.getModifiers();
        if (Modifier.isAbstract(modifier)) {
            return false;
        }

        try {
            // Attempts to get the empty constructor (without parameters)
            Constructor<?> constructor = clazz.getConstructor();
            // Ensures that the constructor is indeed public
            return Modifier.isPublic(constructor.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
