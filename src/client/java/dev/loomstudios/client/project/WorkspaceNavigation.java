package dev.loomstudios.client.project;

import dev.loomstudios.client.screen.LoomDecisionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import java.nio.file.Files;
import java.util.List;

/** Screen-level guards; internal tabs and shared-workspace editors do not prompt. */
public final class WorkspaceNavigation {
    private WorkspaceNavigation() { }
    public static void request(Screen source,Runnable action){
        if(!ClientProjectWorkspace.isInitialized()||!WorkspaceRecovery.editingStarted||!ClientProjectWorkspace.isDirty()){action.run();return;}
        ClientProjectWorkspace.endCompoundEdit();
        Minecraft.getInstance().setScreen(new LoomDecisionScreen(source,"Unsaved changes",ClientProjectWorkspace.project().name(),List.of(
            new LoomDecisionScreen.Choice("Save and continue",()->{ClientProjectWorkspace.save();action.run();},false),
            new LoomDecisionScreen.Choice("Keep recovery draft",()->{WorkspaceRecovery.keepDraft();action.run();},false),
            new LoomDecisionScreen.Choice("Discard changes",()->{ClientProjectWorkspace.discardChanges();action.run();},true))));
    }
}
