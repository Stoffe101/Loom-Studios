package dev.loomstudios.client.project;

import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.ProjectFileStore;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Minecraft game-directory binding for the pure project-core file store.
 */
public final class LocalProjectLibrary {
    private static final ProjectFileStore STORE = new ProjectFileStore(
            FabricLoader.getInstance()
                    .getGameDir()
                    .resolve("loom-studios")
                    .resolve("projects")
    );

    private LocalProjectLibrary() {
    }

    public static ProjectFileStore store() {
        return STORE;
    }

    public static Path root() {
        return STORE.root();
    }

    public static Path save(LoomProject project) throws IOException {
        return STORE.save(project);
    }

    public static LoomProject load(Path path) throws IOException {
        return STORE.load(path);
    }

    public static List<Path> list() throws IOException {
        return STORE.list();
    }
}
