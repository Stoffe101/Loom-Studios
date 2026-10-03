package dev.loomstudios.project;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Local editor choices, deliberately separate from portable artwork and network state. */
public final class EditorPreferences {
    private final Path file;
    private final Properties values = new Properties();
    public EditorPreferences(Path file) throws IOException {
        this.file=file;
        if (Files.isRegularFile(file) && Files.size(file)<=65536)
            try (InputStream stream=Files.newInputStream(file)) { values.load(stream); }
    }
    public boolean enabled(String key, boolean fallback) {
        return Boolean.parseBoolean(values.getProperty(key,Boolean.toString(fallback)));
    }
    public String choice(String key,String fallback) { return values.getProperty(key,fallback); }
    public void set(String key,String value) throws IOException {
        values.setProperty(key,value); save();
    }
    public boolean favorite(UUID id) { return enabled("favorite."+id,false); }
    public void toggleFavorite(UUID id) throws IOException {
        if(favorite(id)) values.remove("favorite."+id); else values.setProperty("favorite."+id,"true");
        save();
    }
    private void save() throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp=file.resolveSibling(file.getFileName()+".tmp");
        try(OutputStream stream=Files.newOutputStream(temp)) { values.store(stream,"Loom Studios editor preferences"); }
        try { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
        catch(AtomicMoveNotSupportedException e) { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING); }
    }
}
