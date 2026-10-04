package dev.loomstudios.client.importing;

import dev.loomstudios.image.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Compatibility facade; format detection and bounded decoding live in the testable core. */
public final class PngImportAdapter {
    public static final long MAX_COMPRESSED_BYTES=ImageImportReader.MAX_COMPRESSED_BYTES;
    private PngImportAdapter(){}
    public static LoadedImage load(Path path)throws IOException{var result=ImageImportReader.load(path);return new LoadedImage(path.toAbsolutePath().normalize(),result.original(),result.embedded(),result.frames(),result.frameTicks());}
    public record LoadedImage(Path sourcePath,PixelImage original,PixelImage embedded,List<PixelImage> frames,List<Integer> frameTicks){
        public LoadedImage(Path sourcePath,PixelImage original,PixelImage embedded){this(sourcePath,original,embedded,List.of(),List.of());}
        public LoadedImage{java.util.Objects.requireNonNull(sourcePath);java.util.Objects.requireNonNull(original);java.util.Objects.requireNonNull(embedded);frames=List.copyOf(frames);frameTicks=List.copyOf(frameTicks);}
    }
}
