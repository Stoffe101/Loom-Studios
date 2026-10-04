package dev.loomstudios.image;

import dev.loomstudios.project.ImageLayerData;
import dev.loomstudios.project.LoomProjectCodec;

import org.w3c.dom.Node;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

/** Header-first bounded ImageIO import. GIF frames honor offsets and disposal modes. */
public final class ImageImportReader {
    public static final long MAX_COMPRESSED_BYTES = 32L * 1024 * 1024;
    private static final long MAX_SOURCE_FRAME_PIXELS = 32L * 1024 * 1024;

    private ImageImportReader() {}

    public record Result(
            PixelImage original,
            PixelImage embedded,
            List<PixelImage> frames,
            List<Integer> frameTicks) {}

    public static Result load(Path path) throws IOException {
        if (!Files.isRegularFile(path)
                || Files.size(path) < 1
                || Files.size(path) > MAX_COMPRESSED_BYTES)
            throw new IOException("Image must be a file under 32 MiB");
        try (ImageInputStream stream = ImageIO.createImageInputStream(path.toFile())) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext())
                throw new IOException("Unsupported image. Use PNG, JPEG, GIF, BMP, TIFF or WBMP");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, false, false);
                boolean gif = reader.getFormatName().equalsIgnoreCase("gif");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (gif && reader.getStreamMetadata() != null) {
                    Node descriptor =
                            find(
                                    reader.getStreamMetadata()
                                            .getAsTree("javax_imageio_gif_stream_1.0"),
                                    "LogicalScreenDescriptor");
                    if (descriptor != null) {
                        width = number(descriptor, "logicalScreenWidth", width);
                        height = number(descriptor, "logicalScreenHeight", height);
                    }
                }
                checkDimensions(width, height);
                if (!gif) {
                    var image = reader.read(0);
                    var original = convert(image);
                    return new Result(original, shrink(original), List.of(), List.of());
                }
                int[] canvas = new int[width * height];
                PixelImage original = null;
                var frames = new ArrayList<PixelImage>();
                var ticks = new ArrayList<Integer>();
                long embeddedPixels = 0;
                for (int i = 0; i <= ImageLayerData.MAX_FRAMES; i++) {
                    try {
                        reader.getWidth(i);
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                    if (i == ImageLayerData.MAX_FRAMES
                            || (long) (i + 1) * width * height > MAX_SOURCE_FRAME_PIXELS)
                        throw new IOException("GIF exceeds 64 frames or source memory budget");
                    Node
                            metadata =
                                    reader.getImageMetadata(i)
                                            .getAsTree("javax_imageio_gif_image_1.0"),
                            descriptor = find(metadata, "ImageDescriptor"),
                            control = find(metadata, "GraphicControlExtension");
                    int x = number(descriptor, "imageLeftPosition", 0),
                            y = number(descriptor, "imageTopPosition", 0),
                            w = reader.getWidth(i),
                            h = reader.getHeight(i);
                    checkDimensions(w, h);
                    if (x < 0 || y < 0 || (long) x + w > width || (long) y + h > height)
                        throw new IOException("GIF frame lies outside logical canvas");
                    String disposal = attribute(control, "disposalMethod", "none");
                    int[] previous = disposal.equals("restoreToPrevious") ? canvas.clone() : null;
                    BufferedImage image = reader.read(i);
                    int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);
                    for (int py = 0; py < h; py++)
                        for (int px = 0; px < w; px++) {
                            int c = pixels[py * w + px];
                            if ((c >>> 24) != 0) canvas[(y + py) * width + x + px] = c;
                        }
                    PixelImage composed = new PixelImage(width, height, canvas);
                    if (original == null) original = composed;
                    PixelImage embedded = shrink(composed);
                    embeddedPixels += (long) embedded.width() * embedded.height();
                    if (embeddedPixels > ImageLayerData.MAX_FRAME_PIXELS)
                        throw new IOException(
                                "GIF exceeds decoded frame budget; reduce its size or frame count");
                    frames.add(embedded);
                    ticks.add(
                            Math.max(
                                    1,
                                    Math.min(
                                            1200,
                                            Math.round(number(control, "delayTime", 10) / 5f))));
                    if (disposal.equals("restoreToPrevious")) canvas = previous;
                    else if (disposal.equals("restoreToBackgroundColor"))
                        for (int py = 0; py < h; py++)
                            Arrays.fill(canvas, (y + py) * width + x, (y + py) * width + x + w, 0);
                }
                if (original == null) throw new IOException("GIF has no readable frames");
                return new Result(
                        original,
                        frames.getFirst(),
                        frames.size() > 1 ? List.copyOf(frames) : List.of(),
                        frames.size() > 1 ? List.copyOf(ticks) : List.of());
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid image dimensions or content", e);
        }
    }

    private static void checkDimensions(int w, int h) throws IOException {
        if (w < 1
                || h < 1
                || w > PixelImage.MAX_DIMENSION
                || h > PixelImage.MAX_DIMENSION
                || (long) w * h > PixelImage.MAX_PIXELS)
            throw new IOException("Image exceeds 4096 px / 16 megapixel limit");
    }

    private static PixelImage convert(BufferedImage image) {
        return new PixelImage(
                image.getWidth(),
                image.getHeight(),
                image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
    }

    private static PixelImage shrink(PixelImage source) {
        int max = LoomProjectCodec.MAX_EMBEDDED_IMAGE_DIMENSION;
        if (source.width() <= max && source.height() <= max) return source;
        double factor = Math.min(max / (double) source.width(), max / (double) source.height());
        return ImageTransforms.resizeNearest(
                source,
                Math.max(1, (int) Math.round(source.width() * factor)),
                Math.max(1, (int) Math.round(source.height() * factor)));
    }

    private static Node find(Node node, String name) {
        if (node == null) return null;
        if (node.getNodeName().equals(name)) return node;
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            Node found = find(child, name);
            if (found != null) return found;
        }
        return null;
    }

    private static String attribute(Node n, String key, String fallback) {
        return n == null || n.getAttributes().getNamedItem(key) == null
                ? fallback
                : n.getAttributes().getNamedItem(key).getNodeValue();
    }

    private static int number(Node n, String key, int fallback) {
        return Integer.parseInt(attribute(n, key, Integer.toString(fallback)));
    }
}
