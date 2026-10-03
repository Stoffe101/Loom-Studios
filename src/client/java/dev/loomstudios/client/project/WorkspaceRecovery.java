package dev.loomstudios.client.project;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.*;
import java.io.IOException;
import java.nio.file.*;

/** Dirty drafts are never equipped or substituted for explicit saves. */
public final class WorkspaceRecovery {
    public static final ProjectFileStore STORE=new ProjectFileStore(LocalProjectLibrary.root().getParent().resolve("drafts"));
    private static LoomProject checkpoint;
    public static boolean editingStarted;
    private static long lastWrite;
    private WorkspaceRecovery() { }
    public static void tick() {
        if(editingStarted&&LoomPreferences.get().enabled("autosave",true) && System.currentTimeMillis()-lastWrite>=30000) checkpoint();
    }
    public static void checkpoint() {
        if(!editingStarted||!ClientProjectWorkspace.isInitialized() || !ClientProjectWorkspace.isDirty()
                || ClientProjectWorkspace.session().isCompoundEditActive()) return;
        LoomProject project=ClientProjectWorkspace.project();
        if(project==checkpoint)return;
        try { STORE.save(project);checkpoint=project;lastWrite=System.currentTimeMillis(); }
        catch(IOException e) { lastWrite=System.currentTimeMillis(); LoomStudios.LOGGER.error("Could not checkpoint Loom draft",e); }
    }
    public static void saved(LoomProject project) {
        try{Files.deleteIfExists(STORE.pathFor(project.projectId()));}catch(IOException e){LoomStudios.LOGGER.warn("Saved design but could not remove its recovery draft",e);}
        checkpoint=project;
    }
}
