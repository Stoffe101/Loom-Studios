package dev.loomstudios.project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class LibraryAuthoringTest {
    @TempDir Path directory;
    @Test void renameTrashRestorePreserveArtworkAndNeverOverwrite() throws Exception {
        ProjectFileStore store=new ProjectFileStore(directory);LoomProject p=LoomProjectFactory.blank("Moon",1000);store.save(p);
        LoomProject renamed=store.rename(p.projectId()," Moonlight ",2000);assertEquals("Moonlight",renamed.name());assertEquals(p.cape(),renamed.cape());assertEquals(p.projectId(),renamed.projectId());
        Path trashed=store.trash(p.projectId());assertTrue(store.list().isEmpty());assertEquals(renamed,store.load(trashed));store.save(p);
        assertThrows(FileAlreadyExistsException.class,()->store.restore(p.projectId()));assertEquals(p,store.load(store.pathFor(p.projectId())));Files.delete(store.pathFor(p.projectId()));assertEquals(renamed,store.load(store.restore(p.projectId())));
    }
    @Test void draftsAreSeparateFromSavedSession() throws Exception {
        ProjectFileStore saved=new ProjectFileStore(directory.resolve("projects")),drafts=new ProjectFileStore(directory.resolve("drafts"));ProjectSession session=new ProjectSession(LoomProjectFactory.blank("Moon",1000),saved);session.save();session.apply(p->p.withName("Unsaved"));drafts.save(session.project());assertTrue(session.isDirty());assertEquals("Moon",saved.load(session.sourcePath()).name());assertEquals("Unsaved",drafts.load(drafts.pathFor(session.project().projectId())).name());
    }
    @Test void preferencesPersistWithoutChangingProjects() throws Exception {
        UUID id=UUID.randomUUID();EditorPreferences p=new EditorPreferences(directory.resolve("editor.properties"));assertTrue(p.enabled("autosave",true));p.toggleFavorite(id);p.set("grid","false");EditorPreferences loaded=new EditorPreferences(directory.resolve("editor.properties"));assertTrue(loaded.favorite(id));assertFalse(loaded.enabled("grid",true));loaded.toggleFavorite(id);assertFalse(new EditorPreferences(directory.resolve("editor.properties")).favorite(id));
    }
    @Test void rectangleRotationAndPasteAreLosslessAndBounded(){PixelPatch p=new PixelPatch(2,3,new int[]{1,2,3,4,5,6});assertArrayEquals(new int[]{5,3,1,6,4,2},p.rotate().data());assertArrayEquals(p.data(),p.rotate().rotate().rotate().rotate().data());assertThrows(IllegalArgumentException.class,()->p.paste(p.rotate(),0,0));int[] copy=p.data();copy[0]=99;assertEquals(1,p.data()[0]);}
    @Test void fillDoesNotLeakAcrossSemanticBoundaries(){PixelPatch p=new PixelPatch(3,3,new int[]{0,1,0,0,1,0,0,1,0});assertArrayEquals(new int[]{7,1,0,7,1,0,7,1,0},PixelDrawing.shape(p,PixelDrawing.Tool.FILL,0,0,0,0,1,7,false).data());}
    @Test void wingEditsMirrorOnlyChangedPixelsAndPreserveOtherUv(){LoomProject p=LoomProjectFactory.blank("Wings",1000);UUID layer=p.elytra().layers().getFirst().id();LoomProject edited=SurfaceEdits.wing(p,layer,ElytraWing.LEFT,true,patch->PixelDrawing.shape(patch,PixelDrawing.Tool.LINE,0,0,0,2,1,0xFF22D7E8,false));int[] pixels=edited.elytra().layers().getFirst().pixels();assertEquals(0xFF22D7E8,pixels[ElytraWing.LEFT.atlasY(0,1)*64+ElytraWing.LEFT.atlasX(0,1)]);assertEquals(0xFF22D7E8,pixels[ElytraWing.RIGHT.atlasY(0,1)*64+ElytraWing.RIGHT.atlasX(ElytraWing.RIGHT.mirroredLocalX(0,1),1)]);assertEquals(0,pixels[0]);LoomProject locked=ProjectEdits.setElytraLayerLocked(p,layer,true);assertThrows(IllegalArgumentException.class,()->SurfaceEdits.wing(locked,layer,ElytraWing.LEFT,false,x->x));}
    @Test void keyframeDragSortsAndMergesWithOneUndoGesture(){LoomProject p=LoomProjectFactory.blank("Animated",1000);UUID layer=p.cape().layers().getFirst().id();LoomAnimation a=AnimationAuthoring.addTrack(p.animation(),layer,AnimationChannel.CAPE,AnimationEffectType.PULSE);AnimationTrack t=a.tracks().getFirst();AnimationTrack moved=AnimationAuthoring.moveKeyframe(t,0,a.durationTicks()/2);assertEquals(2,moved.keyframes().size());assertEquals(.55F,moved.keyframes().getFirst().value());ProjectSession session=new ProjectSession(p.withAnimation(a),new ProjectFileStore(directory));session.beginCompoundEdit();session.apply(v->v.withAnimation(AnimationAuthoring.replaceTrack(a,moved)));session.apply(v->v.withAnimation(AnimationAuthoring.replaceTrack(v.animation(),AnimationAuthoring.moveKeyframe(moved,moved.keyframes().getFirst().tick(),3))));session.endCompoundEdit();session.undo();assertEquals(a,session.project().animation());}
}
