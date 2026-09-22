import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
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
                    .filter(Main::verifyJar)
                    .collect(Collectors.toSet());
        }
    }

    private static boolean verifyJar(Path jar) {
        return true; // TODO
    }
}
