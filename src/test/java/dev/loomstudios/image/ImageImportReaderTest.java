package dev.loomstudios.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

class ImageImportReaderTest {
    @TempDir Path directory;

    @Test
    void detectsRealContentAcrossFormatsWithoutTrustingExtension() throws Exception {
        for (String format : List.of("png", "jpg", "jpeg", "bmp", "gif", "tiff")) {
            var image = new BufferedImage(7, 5, BufferedImage.TYPE_INT_RGB);
            image.setRGB(3, 2, 0xFF55CC22);
            Path file = directory.resolve(format + ".data");
            assertTrue(ImageIO.write(image, format, file.toFile()));
            var result = ImageImportReader.load(file);
            assertEquals(7, result.original().width());
            assertEquals(5, result.original().height());
            assertEquals(7, result.embedded().width());
        }
    }

    @Test
    void oversizedHeaderAndUnknownContentAreRejected() throws Exception {
        Path bad = directory.resolve("fake.jpg");
        Files.writeString(bad, "not an image");
        assertThrows(java.io.IOException.class, () -> ImageImportReader.load(bad));
        var image = new BufferedImage(4097, 1, BufferedImage.TYPE_INT_RGB);
        Path large = directory.resolve("oversize.png");
        ImageIO.write(image, "png", large.toFile());
        assertThrows(java.io.IOException.class, () -> ImageImportReader.load(large));
    }

    @Test
    void gifFramesHonorOffsetsTimingAndDisposal() throws Exception {
        Path file = directory.resolve("animated.gif");
        var writer = ImageIO.getImageWritersByFormatName("gif").next();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(file.toFile())) {
            writer.setOutput(stream);
            writer.prepareWriteSequence(null);
            frame(writer, 3, 2, 0, 0, 0xFFFF0000, 10, "doNotDispose");
            frame(writer, 1, 1, 1, 0, 0xFF0000FF, 25, "restoreToPrevious");
            frame(writer, 1, 1, 0, 1, 0xFF00FF00, 5, "doNotDispose");
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        var loaded = ImageImportReader.load(file);
        assertEquals(3, loaded.frames().size());
        assertEquals(List.of(2, 5, 1), loaded.frameTicks());
        assertEquals(0xFF0000FF, loaded.frames().get(1).pixelAt(1, 0));
        assertEquals(0xFFFF0000, loaded.frames().get(2).pixelAt(1, 0));
        assertEquals(0xFF00FF00, loaded.frames().get(2).pixelAt(0, 1));
    }

    private void frame(
            ImageWriter writer, int w, int h, int x, int y, int color, int delay, String disposal)
            throws Exception {
        var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int py = 0; py < h; py++) for (int px = 0; px < w; px++) image.setRGB(px, py, color);
        var type = ImageTypeSpecifier.createFromRenderedImage(image);
        var meta = writer.getDefaultImageMetadata(type, null);
        var root = (IIOMetadataNode) meta.getAsTree("javax_imageio_gif_image_1.0");
        var descriptor = (IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0);
        descriptor.setAttribute("imageLeftPosition", Integer.toString(x));
        descriptor.setAttribute("imageTopPosition", Integer.toString(y));
        var control =
                (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
        control.setAttribute("delayTime", Integer.toString(delay));
        control.setAttribute("disposalMethod", disposal);
        meta.setFromTree("javax_imageio_gif_image_1.0", root);
        writer.writeToSequence(new IIOImage(image, null, meta), null);
    }

    @Test
    void localBackgroundRemovalKeepsDisconnectedDetailsAndUsesTolerance() {
        var source =
                new PixelImage(
                        3,
                        2,
                        new int[] {
                            0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFFF8F8F8, 0xFF000000, 0xFFFFFFFF
                        });
        var local = new BackgroundRemoval(true, 0xFFFFFFFF, 10, true, 0, 0).apply(source);
        assertEquals(0, local.pixelAt(0, 0) >>> 24);
        assertEquals(0, local.pixelAt(0, 1) >>> 24);
        assertEquals(255, local.pixelAt(2, 0) >>> 24);
        var global = new BackgroundRemoval(true, 0xFFFFFFFF, 10, false, 0, 0).apply(source);
        assertEquals(0, global.pixelAt(2, 0) >>> 24);
        assertEquals(255, source.pixelAt(0, 0) >>> 24);
    }

    @Test
    void tintAndAdjustmentsRetainBackgroundSettingsAndSource() {
        var source = new PixelImage(2, 1, new int[] {0xFFFFFFFF, 0xFF808080});
        var settings =
                ImageProcessingSettings.defaults()
                        .withBackground(new BackgroundRemoval(true, 0xFFFFFFFF, 0, false, 0, 0))
                        .withTint(0xFFFF0000, 1)
                        .withAdjustments(0, 0, 0)
                        .withMode(ImageProcessingMode.DIRECT);
        var output = ImageProcessingPipeline.apply(source, settings);
        assertEquals(0, output.pixelAt(0, 0) >>> 24);
        assertEquals(0xFF800000, output.pixelAt(1, 0));
        assertEquals(0xFF808080, source.pixelAt(1, 0));
        assertTrue(settings.withPalette(List.of(0xFF111111)).background().enabled());
    }
}
