package dev.loomstudios.project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class LibraryPolishTest {
    @TempDir Path directory;
    @Test void restoreBacksUpCurrentAndPreservesIdentity() throws Exception {
        var store=new ProjectFileStore(directory);var first=LoomProjectFactory.blank("Original",1);store.save(first);
        var second=first.withName("Second");store.save(second);var history=new ProjectVersions(store);
        assertEquals(1,history.list(first.projectId()).size());Path version=history.list(first.projectId()).getFirst();
        assertEquals(first,history.read(first.projectId(),version));assertEquals(first,history.restore(first.projectId(),version));
        assertEquals(first,store.load(store.pathFor(first.projectId())));assertTrue(history.list(first.projectId()).stream().anyMatch(p->{try{return history.read(first.projectId(),p).equals(second);}catch(Exception e){return false;}}));
        assertThrows(java.io.IOException.class,()->history.restore(first.projectId(),store.pathFor(first.projectId())));
        assertEquals(first,store.load(store.pathFor(first.projectId())));
    }
    @Test void historyIsBoundedAndDeduplicatesIdenticalArtwork() throws Exception {
        var store=new ProjectFileStore(directory);var p=LoomProjectFactory.blank("A",1);var history=new ProjectVersions(store);
        Path first=history.backup(p);assertEquals(first,history.backup(p));assertEquals(1,history.list(p.projectId()).size());
        for(int i=0;i<30;i++)history.backup(p.withName("Version "+i));assertEquals(ProjectVersions.LIMIT,history.list(p.projectId()).size());
        for(Path path:history.list(p.projectId()))assertEquals(p.projectId(),history.read(p.projectId(),path).projectId());
    }
    @Test void invalidVersionCannotOverwriteTheCurrentProject() throws Exception {
        var store=new ProjectFileStore(directory);var p=LoomProjectFactory.blank("Saved",1);store.save(p);var history=new ProjectVersions(store);Path version=history.backup(p);
        Files.write(version,LoomProjectFactory.blank("Wrong identity",1).encode());assertThrows(java.io.IOException.class,()->history.restore(p.projectId(),version));assertEquals(p,store.load(store.pathFor(p.projectId())));
    }
    @Test void changedSnapshotContentCannotBeRestoredOrSilentlyReused() throws Exception {
        var store=new ProjectFileStore(directory);var p=LoomProjectFactory.blank("Saved",1);store.save(p);var history=new ProjectVersions(store);Path version=history.backup(p);
        Files.write(version,p.withName("Tampered").encode());assertThrows(java.io.IOException.class,()->history.restore(p.projectId(),version));assertThrows(java.io.IOException.class,()->store.save(p.withName("Replacement")));assertEquals(p,store.load(store.pathFor(p.projectId())));
    }
    @Test void failedPreferenceWriteRollsBackTheInMemoryState() throws Exception {
        Path blocker=directory.resolve("file");Files.writeString(blocker,"not a directory");var prefs=EditorPreferences.defaults(blocker.resolve("editor.properties"));
        assertThrows(java.io.IOException.class,()->prefs.setAll(Map.of("folder","Changed","tags","pink")));assertEquals("",prefs.choice("folder",""));assertEquals("",prefs.choice("tags",""));
    }
    @Test void organizationPersistsAndDoesNotModifyArtwork() throws Exception {
        Path file=directory.resolve("editor.properties");var p=TemplateCatalog.create(TemplateCatalog.Kind.CAT,1);String hash=p.hash();var org=new LibraryOrganization(new EditorPreferences(file));
        org.organize(List.of(p.projectId()),"Cute", "Pink, cute, PINK");org.group(p.projectId(),List.of(p.cape().layers().getFirst().id()),"Face");
        org=new LibraryOrganization(new EditorPreferences(file));assertEquals("Cute",org.folder(p.projectId()));assertEquals(List.of("cute","pink"),org.tags(p.projectId()));assertTrue(org.matches(p.projectId(),p.name(),"pink"));assertEquals("Face",org.group(p.projectId(),p.cape().layers().getFirst().id()));assertEquals(hash,p.hash());
        LibraryOrganization loaded=org;assertThrows(IllegalArgumentException.class,()->loaded.organize(List.of(p.projectId()),"Changed","a".repeat(25)));assertEquals("Cute",org.folder(p.projectId()));
    }
    @Test void selectedLayerBlockMovesTogetherAndBatchUndoIsOneStep() {
        LoomProject p=LoomProjectFactory.blank("Layers",1);p=ProjectEdits.addCapeLayer(p, "Layer");p=ProjectEdits.addCapeLayer(p, "Layer");p=ProjectEdits.addCapeLayer(p, "Layer");
        var ids=p.cape().layers().stream().map(LoomLayer::id).toList();var selected=Set.of(ids.get(1),ids.get(2));var moved=LayerBatch.apply(p,false,selected,LayerBatch.Action.UP);
        assertEquals(List.of(ids.get(0),ids.get(3),ids.get(1),ids.get(2)),moved.cape().layers().stream().map(LoomLayer::id).toList());assertEquals(p.elytra(),moved.elytra());
        var session=new ProjectSession(p,new ProjectFileStore(directory));session.apply(v->LayerBatch.apply(v,false,selected,LayerBatch.Action.HIDE));assertFalse(session.project().cape().layers().get(1).visible());session.undo();assertEquals(p,session.project());assertFalse(session.canUndo());
        LoomProject original=p;assertThrows(IllegalArgumentException.class,()->LayerBatch.apply(original,false,Set.copyOf(ids),LayerBatch.Action.DELETE));assertEquals(2,LayerBatch.apply(p,false,selected,LayerBatch.Action.DELETE).cape().layers().size());
    }
}
