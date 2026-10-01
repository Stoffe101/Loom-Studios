package dev.loomstudios.client.project;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.LoomProject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Produces a small cached PNG from the project's compiled cape canvas.
 */
public final class ProjectThumbnailCache {
    private static final Path ROOT = FabricLoader.getInstance()
            .getGameDir()
            .resolve("loom-studios")
            .resolve("thumbnails");

    private ProjectThumbnailCache() {
    }

    public static Path ensure(LoomProject project, String contentHash)
            throws IOException {
        Files.createDirectories(ROOT);

        String suffix = contentHash.substring(0, 16);
        Path target = ROOT.resolve(project.projectId() + "-" + suffix + ".png");

        if (Files.isRegularFile(target)) {
            return target;
        }

        int[] pixels = LoomTextureCompiler.compile(
                project.cape(),
                0,
                false,
                false
        );

        try (NativeImage image = new NativeImage(
                NativeImage.Format.RGBA,
                project.cape().width(),
                project.cape().height(),
                false
        )) {
            for (int y = 0; y < project.cape().height(); y++) {
                for (int x = 0; x < project.cape().width(); x++) {
                    image.setPixel(
                            x,
                            y,
                            pixels[y * project.cape().width() + x]
                    );
                }
            }

            image.writeToFile(target);
        }

        return target;
    }

    public static Path root() {
        return ROOT;
    }
}
