package dev.loomstudios.project;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Bounded local snapshots. File checksums describe original bytes, including legacy schemas. */
public final class ProjectVersions {
    public static final int LIMIT = 20;
    private final ProjectFileStore store;
    public ProjectVersions(ProjectFileStore store) { this.store = store; }
    private Path directory(UUID id) { return store.root().resolve("history").resolve(id.toString()); }
    public List<Path> list(UUID id) throws IOException {
        Path d = directory(id);
        if (!Files.isDirectory(d)) return List.of();
        try (var files = Files.list(d)) {
            return files.filter(p -> p.getFileName().toString().matches("[0-9]{13}-[a-f0-9]{64}\\.loom") && Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed()).toList();
        }
    }
    public Path backup(LoomProject project) throws IOException { return writeSnapshot(project.projectId(), project.encode()); }
    Path backupSource(Path source, UUID id) throws IOException {
        var normalized=source.toAbsolutePath().normalize();
        if(!normalized.equals(store.pathFor(id)))throw new IOException("Unknown source project");
        byte[] bytes=readBounded(normalized);
        if(!LoomProjectCodec.decode(bytes).projectId().equals(id))throw new IOException("Backup project identity mismatch");
        return writeSnapshot(id,bytes);
    }
    private Path writeSnapshot(UUID id,byte[] bytes)throws IOException {
        Path d=directory(id);Files.createDirectories(d);
        String suffix="-"+LoomProjectCodec.sha256(bytes)+".loom";
        for(Path existing:list(id))if(existing.getFileName().toString().endsWith(suffix)){read(id,existing);return existing;}
        Path path=d.resolve(System.currentTimeMillis()+suffix),temp=path.resolveSibling(path.getFileName()+".tmp");
        Files.write(temp,bytes);
        try{Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,path);}
        var versions=list(id);for(int i=LIMIT;i<versions.size();i++)Files.delete(versions.get(i));
        return path;
    }
    private static byte[] readBounded(Path path)throws IOException {
        if(!Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS)||Files.size(path)<1||Files.size(path)>LoomProjectCodec.MAX_SERIALIZED_BYTES)throw new IOException("Version file size or type invalid");
        try(var input=Files.newInputStream(path)){
            byte[] data=input.readNBytes(LoomProjectCodec.MAX_SERIALIZED_BYTES+1);
            if(data.length==0||data.length>LoomProjectCodec.MAX_SERIALIZED_BYTES)throw new IOException("Version file size invalid");
            return data;
        }
    }
    public LoomProject read(UUID id,Path path)throws IOException {
        var normalized=path.toAbsolutePath().normalize();
        if(!list(id).contains(normalized))throw new IOException("Unknown version");
        byte[] bytes=readBounded(normalized);
        LoomProject project=LoomProjectCodec.decode(bytes);
        if(!project.projectId().equals(id)||!normalized.getFileName().toString().endsWith("-"+LoomProjectCodec.sha256(bytes)+".loom"))throw new IOException("Version identity or checksum mismatch");
        return project;
    }
    public LoomProject restore(UUID id,Path path)throws IOException {LoomProject project=read(id,path);store.save(project);return project;}
}
