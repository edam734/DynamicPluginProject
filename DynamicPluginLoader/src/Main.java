import com.edam.dynamicloader.LoadedPlugin;
import com.edam.dynamicloader.PluginLoader;
import com.edam.dynamicloader.PluginScanner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    static void main(String[] args) throws IOException {
        Path path = Paths.get("plugins");
        List<Path> paths = PluginScanner.findPlugins(path);
        Set<Optional<LoadedPlugin>> plugins = paths.stream()
                .map(PluginLoader.INSTANCE::loadUnchecked)
                .collect(Collectors.toSet());

        plugins.forEach(optional -> optional.ifPresent(
                LoadedPlugin -> System.out.println(LoadedPlugin.plugin().getName())));
    }
}