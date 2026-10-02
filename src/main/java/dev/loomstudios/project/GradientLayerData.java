package dev.loomstudios.project;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record GradientLayerData(
        GradientType type,
        List<GradientStop> stops,
        LayerTransform transform,
        NormalizedRect clip,
        boolean repeat,
        boolean dither
) {
    public GradientLayerData {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(stops, "stops");
        Objects.requireNonNull(transform, "transform");
        Objects.requireNonNull(clip, "clip");

        if (stops.size() < 2 || stops.size() > 16) {
            throw new IllegalArgumentException(
                    "Gradient stop count out of range"
            );
        }

        stops = stops.stream()
                .peek(stop -> Objects.requireNonNull(stop, "stop"))
                .sorted(Comparator.comparingDouble(
                        GradientStop::position
                ))
                .toList();
    }

    public static GradientLayerData defaultLinear(
            int first,
            int second,
            NormalizedRect clip
    ) {
        return new GradientLayerData(
                GradientType.LINEAR,
                List.of(
                        new GradientStop(0.0, first),
                        new GradientStop(1.0, second)
                ),
                new LayerTransform(
                        clip.centerX(),
                        clip.centerY(),
                        clip.width(),
                        clip.height(),
                        0.0,
                        false,
                        false
                ),
                clip,
                false,
                false
        );
    }

    public GradientLayerData withType(GradientType next) {
        return new GradientLayerData(
                next, stops, transform, clip, repeat, dither
        );
    }

    public GradientLayerData withTransform(LayerTransform next) {
        return new GradientLayerData(
                type, stops, next, clip, repeat, dither
        );
    }

    public GradientLayerData withStops(List<GradientStop> next) {
        return new GradientLayerData(
                type, next, transform, clip, repeat, dither
        );
    }

    public GradientLayerData withRepeat(boolean next) {
        return new GradientLayerData(
                type, stops, transform, clip, next, dither
        );
    }

    public GradientLayerData withDither(boolean next) {
        return new GradientLayerData(
                type, stops, transform, clip, repeat, next
        );
    }
}
