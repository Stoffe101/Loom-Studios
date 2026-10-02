package dev.loomstudios.client.sharing;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.AnimationChannel;
import dev.loomstudios.project.LoomCanvas;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectCode;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;

/**
 * Local/offline export surface for Loom Codes.
 */
public final class LoomShareExportAdapter {
    private static final Path ROOT = FabricLoader.getInstance()
            .getGameDir()
            .resolve("loom-studios")
            .resolve("exports");

    private LoomShareExportAdapter() {
    }

    public static Path root() {
        return ROOT;
    }

    public static Path exportProject(LoomProject project) throws IOException {
        Objects.requireNonNull(project, "project");
        Files.createDirectories(ROOT);

        Path target = uniqueTarget(
                sanitize(project.name()),
                ".loom"
        );
        Files.write(
                target,
                project.encode(),
                StandardOpenOption.CREATE_NEW
        );
        return target;
    }

    public static Path exportPortableCode(LoomProject project)
            throws IOException {
        Objects.requireNonNull(project, "project");
        Files.createDirectories(ROOT);

        Path target = uniqueTarget(
                sanitize(project.name()) + "-portable-code",
                ".txt"
        );
        Files.writeString(
                target,
                LoomProjectCode.encodePortable(project),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW
        );
        return target;
    }

    public static Path exportCapePng(LoomProject project)
            throws IOException {
        return exportTexture(
                project,
                AnimationChannel.CAPE,
                "cape"
        );
    }

    public static Path exportElytraPng(LoomProject project)
            throws IOException {
        return exportTexture(
                project,
                AnimationChannel.ELYTRA,
                "elytra"
        );
    }

    private static Path exportTexture(
            LoomProject project,
            AnimationChannel channel,
            String suffix
    ) throws IOException {
        Objects.requireNonNull(project, "project");
        Files.createDirectories(ROOT);

        LoomCanvas canvas = channel == AnimationChannel.CAPE
                ? project.cape()
                : project.elytra();

        int[] pixels = LoomTextureCompiler.compileAnimated(
                project,
                channel,
                0,
                0,
                false
        );

        Path target = uniqueTarget(
                sanitize(project.name()) + "-" + suffix,
                ".png"
        );

        try (NativeImage image = new NativeImage(
                NativeImage.Format.RGBA,
                canvas.width(),
                canvas.height(),
                false
        )) {
            for (int y = 0; y < canvas.height(); y++) {
                for (int x = 0; x < canvas.width(); x++) {
                    image.setPixel(
                            x,
                            y,
                            pixels[y * canvas.width() + x]
                    );
                }
            }

            image.writeToFile(target);
        }

        return target;
    }

    private static Path uniqueTarget(
            String base,
            String extension
    ) throws IOException {
        Path target = ROOT.resolve(base + extension);

        if (!Files.exists(target)) {
            return target;
        }

        for (int i = 2; i <= 9999; i++) {
            target = ROOT.resolve(
                    base + "-" + i + extension
            );
            if (!Files.exists(target)) {
                return target;
            }
        }

        throw new IOException(
                "Could not allocate unique Loom export file"
        );
    }

    private static String sanitize(String value) {
        String normalized = value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^[.-]+|[.-]+$", "");

        if (normalized.isBlank()) {
            return "loom-design";
        }

        return normalized.length() > 56
                ? normalized.substring(0, 56)
                : normalized;
    }
}
