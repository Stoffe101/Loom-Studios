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
        if (Files.isRegularFile(file) && Files.size(file)<=1048576)
            try (InputStream stream=Files.newInputStream(file)) { values.load(stream); }
    }
    private EditorPreferences(Path file,boolean empty){this.file=file;}
    public static EditorPreferences defaults(Path file){return new EditorPreferences(file,true);}
    public boolean enabled(String key, boolean fallback) {
        return Boolean.parseBoolean(values.getProperty(key,Boolean.toString(fallback)));
    }
    public String choice(String key,String fallback) { return values.getProperty(key,fallback); }
    public void set(String key,String value) throws IOException {
        setAll(Map.of(key,value));
    }
    public void setAll(Map<String,String> updates) throws IOException {
        Properties previous=new Properties();previous.putAll(values);updates.forEach(values::setProperty);
        try{save();}catch(IOException e){values.clear();values.putAll(previous);throw e;}
    }
    public boolean favorite(UUID id) { return enabled("favorite."+id,false); }
    public void toggleFavorite(UUID id) throws IOException {
        set("favorite."+id,Boolean.toString(!favorite(id)));
    }
    private void save() throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp=file.resolveSibling(file.getFileName()+".tmp");
        try(OutputStream stream=Files.newOutputStream(temp)) { values.store(stream,"Loom Studios editor preferences"); }
        if(Files.size(temp)>1048576){Files.deleteIfExists(temp);throw new IOException("Organization storage limit reached");}
        try { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
        catch(AtomicMoveNotSupportedException e) { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING); }
    }
}
