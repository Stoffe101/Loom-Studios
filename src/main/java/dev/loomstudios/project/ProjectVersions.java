package dev.loomstudios.project;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Bounded immutable local snapshots. Restoring preserves identity and backs up the current save. */
public final class ProjectVersions {
    public static final int LIMIT=20;
    private final ProjectFileStore store;
    public ProjectVersions(ProjectFileStore store){this.store=store;}
    private Path directory(UUID id){return store.root().resolve("history").resolve(id.toString());}
    public List<Path> list(UUID id) throws IOException {
        Path d=directory(id);if(!Files.isDirectory(d))return List.of();
        try(var files=Files.list(d)){return files.filter(p->p.getFileName().toString().matches("[0-9]+-[a-f0-9]{64}\\.loom")&&Files.isRegularFile(p,LinkOption.NOFOLLOW_LINKS)).sorted(Comparator.comparing((Path p)->p.getFileName().toString()).reversed()).toList();}
    }
    public Path backup(LoomProject p) throws IOException {
        Path d=directory(p.projectId());Files.createDirectories(d);String suffix="-"+p.hash()+".loom";
        for(Path existing:list(p.projectId()))if(existing.getFileName().toString().endsWith(suffix))return existing;
        Path path=d.resolve(System.currentTimeMillis()+suffix),temp=path.resolveSibling(path.getFileName()+".tmp");Files.write(temp,p.encode());
        try{Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,path);}
        var versions=list(p.projectId());for(int i=LIMIT;i<versions.size();i++)Files.delete(versions.get(i));return path;
    }
    public LoomProject read(UUID id,Path path) throws IOException {
        if(!list(id).contains(path.toAbsolutePath().normalize()))throw new IOException("Unknown version");
        LoomProject p=new ProjectFileStore(directory(id)).load(path);if(!p.projectId().equals(id)||!path.getFileName().toString().endsWith("-"+p.hash()+".loom"))throw new IOException("Version identity or checksum mismatch");return p;
    }
    public LoomProject restore(UUID id,Path path) throws IOException {LoomProject p=read(id,path);store.save(p);return p;}
}
