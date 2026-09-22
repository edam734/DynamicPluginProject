import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

public class Main {

    static void main(String[] args) throws IOException {
        Path path = Paths.get("plugins");
        getPlugins(path);
    }

    private static Set<String> getPlugins(Path pathToDir) throws IOException {
        try (Stream<Path> stream = Files.list(pathToDir)) {
//            stream.filter(file -> Files.isRegularFile(file) &&
//                    file.toString().toLowerCase().endsWith(".jar"))
        }

        return Set.of();
    }
}
