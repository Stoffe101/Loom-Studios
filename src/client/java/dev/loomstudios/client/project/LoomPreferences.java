package dev.loomstudios.client.project;

import dev.loomstudios.project.EditorPreferences;
import dev.loomstudios.LoomStudios;
import java.io.IOException;

public final class LoomPreferences {
    private static EditorPreferences values;
    private LoomPreferences() { }
    public static EditorPreferences get() {
        if(values==null) try { values=new EditorPreferences(LocalProjectLibrary.root().getParent().resolve("editor.properties")); }
        catch(IOException|IllegalArgumentException e) { LoomStudios.LOGGER.warn("Could not read editor preferences; using defaults",e);values=EditorPreferences.defaults(LocalProjectLibrary.root().getParent().resolve("editor.properties")); }
        return values;
    }
    public static void toggle(String key,boolean fallback) {
        try { get().set(key,Boolean.toString(!get().enabled(key,fallback))); }
        catch(IOException e) { LoomStudios.LOGGER.error("Could not save editor preferences",e); }
    }
}
