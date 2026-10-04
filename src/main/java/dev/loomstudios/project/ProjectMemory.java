package dev.loomstudios.project;

import java.util.IdentityHashMap;

/** Counts immutable artwork without cloning buffers; shared layer snapshots count once. */
public final class ProjectMemory {
    public static final long MAX_ARTWORK_BYTES = 60L * 1024 * 1024;

    private ProjectMemory() {}

    public static long artworkBytes(LoomProject project) {
        return canvasBytes(project.cape(), new IdentityHashMap<>())
                + canvasBytes(project.elytra(), new IdentityHashMap<>());
    }

    public static long layerBytes(LoomLayer layer) {
        long bytes = 256L + layer.pixelCount() * 4L + layer.maskLength();
        if (layer.imageData() != null) {
            var data = layer.imageData();
            var seen = new IdentityHashMap<Object, Boolean>();
            seen.put(data.source(), true);
            bytes += (long) data.source().width() * data.source().height() * 4;
            for (var frame : data.frames())
                if (seen.put(frame, true) == null)
                    bytes += (long) frame.width() * frame.height() * 4;
        }
        return bytes;
    }

    static long canvasBytes(LoomCanvas canvas, IdentityHashMap<Object, Boolean> seen) {
        long bytes = 0;
        for (var layer : canvas.layers()) {
            if (seen.put(layer, true) != null) continue;
            bytes += 256L + layer.pixelCount() * 4L + layer.maskLength();
            if (layer.imageData() != null) {
                var data = layer.imageData();
                if (seen.put(data.source(), true) == null)
                    bytes += (long) data.source().width() * data.source().height() * 4;
                for (var frame : data.frames())
                    if (seen.put(frame, true) == null)
                        bytes += (long) frame.width() * frame.height() * 4;
            }
        }
        return bytes;
    }

    static long retainedBytes(Iterable<LoomProject> projects, LoomProject current) {
        var seen = new IdentityHashMap<Object, Boolean>();
        long total = canvasBytes(current.cape(), seen) + canvasBytes(current.elytra(), seen);
        for (var project : projects)
            total += canvasBytes(project.cape(), seen) + canvasBytes(project.elytra(), seen);
        return total;
    }
}
