package dev.loomstudios.project;
import dev.loomstudios.palette.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EditorSafetyTest {
    @TempDir Path directory;
    @Test void discardRestoresLastExplicitSaveWithoutWriting() throws Exception {
        ProjectFileStore store=new ProjectFileStore(directory);LoomProject p=LoomProjectFactory.blank("Saved",1);Path path=store.save(p);ProjectSession s=ProjectSession.load(path,store);
        s.beginCompoundEdit();s.apply(x->x.withName("Unsaved"));s.discardChanges();assertFalse(s.isDirty());assertFalse(s.isCompoundEditActive());assertFalse(s.canUndo());assertEquals(p,s.project());assertEquals(p,store.load(path));
    }
    @Test void discardNewSessionDoesNotCreateAFile(){ProjectFileStore store=new ProjectFileStore(directory);LoomProject p=LoomProjectFactory.blank("Blank",1);ProjectSession s=new ProjectSession(p,store);s.apply(x->x.withName("Edit"));s.discardChanges();assertEquals(p,s.project());assertFalse(s.isDirty());assertFalse(Files.exists(store.pathFor(p.projectId())));}
    @Test void recentColorsAreUniqueBoundedMruAndPersistent(){RecentColors r=new RecentColors("");for(int i=0;i<30;i++)r.use(0xFF000000|i);assertEquals(16,r.colors().size());r.use(0xFF000010);assertEquals(0xFF000010,r.colors().getFirst());assertFalse(r.use(0xFF000010));assertEquals(r.colors(),new RecentColors(r.encode()).colors());}
    @Test void malformedRecentPreferencesAreSkipped(){RecentColors r=new RecentColors("not-a-color,FF22D7E8,FF22D7E8,ZZZZZZZZ,FFFFFFFF");assertEquals(List.of(0xFF22D7E8,0xFFFFFFFF),r.colors());assertThrows(UnsupportedOperationException.class,()->r.colors().add(1));}
    @Test void designColorsIgnoreTransparentAndRankByFrequency(){assertEquals(List.of(0xFF22D7E8,0xFFFFFFFF),DesignColors.collect(new int[]{0,0xFF22D7E8,0xFFFFFFFF},new int[]{0xFF22D7E8,0x0022D7E8}));}
    @Test void designPaletteIsDeterministicAndBounded(){int[] colors=new int[100];for(int i=0;i<100;i++)colors[i]=0xFF000000|i;var first=DesignColors.collect(colors);assertEquals(ColorPalette.MAX_COLORS,first.size());assertEquals(first,DesignColors.collect(colors));assertEquals(0xFF000000,first.getFirst());assertTrue(DesignColors.collect(new int[]{0,1}).isEmpty());}
}
