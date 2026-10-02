package dev.loomstudios.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Deterministic color-reduction primitives for Smart Import.
 *
 * <p>Alpha is preserved from the source image. Palette colors contribute RGB
 * only so reducing/importing colors does not unexpectedly destroy source
 * transparency.</p>
 */
public final class ImageColorReduction {
    public static final int MAX_PALETTE_COLORS = 256;
    private static final int HISTOGRAM_BITS = 5;
    private static final int HISTOGRAM_LEVELS = 1 << HISTOGRAM_BITS;
    private static final int HISTOGRAM_SIZE =
            HISTOGRAM_LEVELS * HISTOGRAM_LEVELS * HISTOGRAM_LEVELS;

    private ImageColorReduction() {
    }

    public static PixelImage reduceColors(
            PixelImage source,
            int maxColors
    ) {
        Objects.requireNonNull(source, "source");
        validateColorCount(maxColors);

        if (hasAtMostColors(source, maxColors)) {
            return source;
        }

        List<Integer> palette = extractPalette(source, maxColors);
        return mapToPalette(source, palette);
    }

    public static List<Integer> extractPalette(
            PixelImage source,
            int maxColors
    ) {
        Objects.requireNonNull(source, "source");
        validateColorCount(maxColors);

        Histogram histogram = histogram(source);
        if (histogram.points.isEmpty()) {
            return List.of();
        }

        List<ColorBox> boxes = new ArrayList<>();
        boxes.add(new ColorBox(histogram.points));

        while (boxes.size() < maxColors) {
            int splitIndex = selectBoxToSplit(boxes);
            if (splitIndex < 0) {
                break;
            }

            ColorBox selected = boxes.remove(splitIndex);
            Split split = selected.split();
            if (split == null) {
                boxes.add(selected);
                break;
            }

            boxes.add(split.left);
            boxes.add(split.right);
        }

        boxes.sort(
                Comparator.comparingInt(ColorBox::minimumBinIndex)
        );

        Set<Integer> unique = new LinkedHashSet<>();
        for (ColorBox box : boxes) {
            unique.add(box.averageArgb());
        }

        return List.copyOf(unique);
    }

    public static PixelImage mapToPalette(
            PixelImage source,
            List<Integer> palette
    ) {
        Objects.requireNonNull(source, "source");
        validatePalette(palette);

        int[] input = source.pixels();
        int[] output = new int[input.length];

        for (int i = 0; i < input.length; i++) {
            int argb = input[i];
            int alpha = (argb >>> 24) & 0xFF;

            if (alpha == 0) {
                output[i] = argb;
                continue;
            }

            int nearest = nearestPaletteColor(argb, palette);
            output[i] = (alpha << 24) | (nearest & 0x00FFFFFF);
        }

        return new PixelImage(source.width(), source.height(), output);
    }

    public static PixelImage posterize(
            PixelImage source,
            int levelsPerChannel
    ) {
        Objects.requireNonNull(source, "source");

        if (levelsPerChannel < 2 || levelsPerChannel > 256) {
            throw new IllegalArgumentException(
                    "Posterize levels must be between 2 and 256"
            );
        }

        int[] input = source.pixels();
        int[] output = new int[input.length];

        for (int i = 0; i < input.length; i++) {
            int argb = input[i];
            int alpha = (argb >>> 24) & 0xFF;

            if (alpha == 0) {
                output[i] = argb;
                continue;
            }

            int red = posterizeChannel(
                    (argb >>> 16) & 0xFF,
                    levelsPerChannel
            );
            int green = posterizeChannel(
                    (argb >>> 8) & 0xFF,
                    levelsPerChannel
            );
            int blue = posterizeChannel(
                    argb & 0xFF,
                    levelsPerChannel
            );

            output[i] = (alpha << 24)
                    | (red << 16)
                    | (green << 8)
                    | blue;
        }

        return new PixelImage(source.width(), source.height(), output);
    }

    public static PixelImage monochrome(PixelImage source) {
        Objects.requireNonNull(source, "source");

        int[] input = source.pixels();
        int[] output = new int[input.length];

        for (int i = 0; i < input.length; i++) {
            int argb = input[i];
            int alpha = (argb >>> 24) & 0xFF;

            if (alpha == 0) {
                output[i] = argb;
                continue;
            }

            int red = (argb >>> 16) & 0xFF;
            int green = (argb >>> 8) & 0xFF;
            int blue = argb & 0xFF;
            int luminance = Math.max(
                    0,
                    Math.min(
                            255,
                            (int)Math.round(
                                    red * 0.2126
                                            + green * 0.7152
                                            + blue * 0.0722
                            )
                    )
            );

            output[i] = (alpha << 24)
                    | (luminance << 16)
                    | (luminance << 8)
                    | luminance;
        }

        return new PixelImage(source.width(), source.height(), output);
    }

    static int nearestPaletteColor(
            int argb,
            List<Integer> palette
    ) {
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;

        int best = palette.getFirst();
        long bestDistance = Long.MAX_VALUE;

        for (int color : palette) {
            int dr = red - ((color >>> 16) & 0xFF);
            int dg = green - ((color >>> 8) & 0xFF);
            int db = blue - (color & 0xFF);
            long distance =
                    (long)dr * dr
                            + (long)dg * dg
                            + (long)db * db;

            if (distance < bestDistance) {
                bestDistance = distance;
                best = color;
            }
        }

        return best;
    }

    static void validatePalette(List<Integer> palette) {
        Objects.requireNonNull(palette, "palette");

        if (palette.isEmpty()
                || palette.size() > MAX_PALETTE_COLORS) {
            throw new IllegalArgumentException(
                    "Palette must contain 1 to "
                            + MAX_PALETTE_COLORS
                            + " colors"
            );
        }

        for (Integer color : palette) {
            if (color == null) {
                throw new IllegalArgumentException(
                        "Palette color cannot be null"
                );
            }
        }
    }

    private static boolean hasAtMostColors(
            PixelImage source,
            int maxColors
    ) {
        Set<Integer> colors = new HashSet<>(maxColors + 1);

        for (int argb : source.pixels()) {
            if (((argb >>> 24) & 0xFF) == 0) {
                continue;
            }

            colors.add(argb & 0x00FFFFFF);
            if (colors.size() > maxColors) {
                return false;
            }
        }

        return true;
    }

    private static void validateColorCount(int maxColors) {
        if (maxColors < 1 || maxColors > MAX_PALETTE_COLORS) {
            throw new IllegalArgumentException(
                    "Color count must be between 1 and "
                            + MAX_PALETTE_COLORS
            );
        }
    }

    private static int posterizeChannel(
            int value,
            int levels
    ) {
        int index = (int)Math.round(
                value * (levels - 1) / 255.0
        );

        return (int)Math.round(
                index * 255.0 / (levels - 1)
        );
    }

    private static Histogram histogram(PixelImage source) {
        int[] counts = new int[HISTOGRAM_SIZE];
        long[] sumRed = new long[HISTOGRAM_SIZE];
        long[] sumGreen = new long[HISTOGRAM_SIZE];
        long[] sumBlue = new long[HISTOGRAM_SIZE];

        for (int argb : source.pixels()) {
            if (((argb >>> 24) & 0xFF) == 0) {
                continue;
            }

            int red = (argb >>> 16) & 0xFF;
            int green = (argb >>> 8) & 0xFF;
            int blue = argb & 0xFF;
            int index = histogramIndex(red, green, blue);

            counts[index]++;
            sumRed[index] += red;
            sumGreen[index] += green;
            sumBlue[index] += blue;
        }

        List<ColorPoint> points = new ArrayList<>();

        for (int index = 0; index < HISTOGRAM_SIZE; index++) {
            int count = counts[index];
            if (count == 0) {
                continue;
            }

            points.add(new ColorPoint(
                    index,
                    count,
                    sumRed[index],
                    sumGreen[index],
                    sumBlue[index]
            ));
        }

        return new Histogram(List.copyOf(points));
    }

    private static int histogramIndex(
            int red,
            int green,
            int blue
    ) {
        int r = red >>> (8 - HISTOGRAM_BITS);
        int g = green >>> (8 - HISTOGRAM_BITS);
        int b = blue >>> (8 - HISTOGRAM_BITS);

        return (r << (HISTOGRAM_BITS * 2))
                | (g << HISTOGRAM_BITS)
                | b;
    }

    private static int selectBoxToSplit(
            List<ColorBox> boxes
    ) {
        int selectedIndex = -1;
        long selectedScore = Long.MIN_VALUE;
        int selectedPointCount = -1;

        for (int i = 0; i < boxes.size(); i++) {
            ColorBox box = boxes.get(i);
            if (!box.canSplit()) {
                continue;
            }

            long score = (long)box.maximumRange()
                    * box.totalCount();

            if (score > selectedScore
                    || (score == selectedScore
                    && box.points.size() > selectedPointCount)) {
                selectedScore = score;
                selectedPointCount = box.points.size();
                selectedIndex = i;
            }
        }

        return selectedIndex;
    }

    private record Histogram(List<ColorPoint> points) {
    }

    private record ColorPoint(
            int binIndex,
            int count,
            long sumRed,
            long sumGreen,
            long sumBlue
    ) {
        int red() {
            return (int)(sumRed / count);
        }

        int green() {
            return (int)(sumGreen / count);
        }

        int blue() {
            return (int)(sumBlue / count);
        }
    }

    private static final class ColorBox {
        private final List<ColorPoint> points;
        private final int minRed;
        private final int maxRed;
        private final int minGreen;
        private final int maxGreen;
        private final int minBlue;
        private final int maxBlue;
        private final long totalCount;

        private ColorBox(List<ColorPoint> points) {
            if (points.isEmpty()) {
                throw new IllegalArgumentException(
                        "Color box cannot be empty"
                );
            }

            this.points = List.copyOf(points);

            int minR = 255;
            int maxR = 0;
            int minG = 255;
            int maxG = 0;
            int minB = 255;
            int maxB = 0;
            long count = 0L;

            for (ColorPoint point : points) {
                minR = Math.min(minR, point.red());
                maxR = Math.max(maxR, point.red());
                minG = Math.min(minG, point.green());
                maxG = Math.max(maxG, point.green());
                minB = Math.min(minB, point.blue());
                maxB = Math.max(maxB, point.blue());
                count += point.count();
            }

            this.minRed = minR;
            this.maxRed = maxR;
            this.minGreen = minG;
            this.maxGreen = maxG;
            this.minBlue = minB;
            this.maxBlue = maxB;
            this.totalCount = count;
        }

        boolean canSplit() {
            return points.size() > 1 && maximumRange() > 0;
        }

        long totalCount() {
            return totalCount;
        }

        int maximumRange() {
            return Math.max(
                    maxRed - minRed,
                    Math.max(
                            maxGreen - minGreen,
                            maxBlue - minBlue
                    )
            );
        }

        int minimumBinIndex() {
            return points.stream()
                    .mapToInt(ColorPoint::binIndex)
                    .min()
                    .orElseThrow();
        }

        int averageArgb() {
            long red = 0L;
            long green = 0L;
            long blue = 0L;

            for (ColorPoint point : points) {
                red += point.sumRed();
                green += point.sumGreen();
                blue += point.sumBlue();
            }

            int r = (int)Math.round(red / (double)totalCount);
            int g = (int)Math.round(green / (double)totalCount);
            int b = (int)Math.round(blue / (double)totalCount);

            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        Split split() {
            if (!canSplit()) {
                return null;
            }

            Channel channel = splitChannel();
            List<ColorPoint> sorted = new ArrayList<>(points);
            sorted.sort(
                    Comparator.comparingInt(
                            point -> channel.value(point)
                    ).thenComparingInt(ColorPoint::binIndex)
            );

            long midpoint = Math.max(1L, totalCount / 2L);
            long cumulative = 0L;
            int splitAfter = -1;

            for (int i = 0; i < sorted.size() - 1; i++) {
                cumulative += sorted.get(i).count();
                if (cumulative >= midpoint) {
                    splitAfter = i;
                    break;
                }
            }

            if (splitAfter < 0) {
                splitAfter = sorted.size() / 2 - 1;
            }

            if (splitAfter < 0 || splitAfter >= sorted.size() - 1) {
                return null;
            }

            return new Split(
                    new ColorBox(
                            new ArrayList<>(
                                    sorted.subList(0, splitAfter + 1)
                            )
                    ),
                    new ColorBox(
                            new ArrayList<>(
                                    sorted.subList(
                                            splitAfter + 1,
                                            sorted.size()
                                    )
                            )
                    )
            );
        }

        private Channel splitChannel() {
            int redRange = maxRed - minRed;
            int greenRange = maxGreen - minGreen;
            int blueRange = maxBlue - minBlue;

            if (redRange >= greenRange && redRange >= blueRange) {
                return Channel.RED;
            }
            if (greenRange >= blueRange) {
                return Channel.GREEN;
            }
            return Channel.BLUE;
        }
    }

    private enum Channel {
        RED {
            @Override
            int value(ColorPoint point) {
                return point.red();
            }
        },
        GREEN {
            @Override
            int value(ColorPoint point) {
                return point.green();
            }
        },
        BLUE {
            @Override
            int value(ColorPoint point) {
                return point.blue();
            }
        };

        abstract int value(ColorPoint point);
    }

    private record Split(ColorBox left, ColorBox right) {
    }
}
