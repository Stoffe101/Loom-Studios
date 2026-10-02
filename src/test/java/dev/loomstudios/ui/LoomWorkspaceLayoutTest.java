package dev.loomstudios.ui;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LoomWorkspaceLayoutTest {
    @Test void requiredProfilesAndRoundingKeepWorkspaceBounded() {
        for (int[] profile : new int[][]{{640,360},{960,540},{1720,720},{1147,480}})
            for (int dx : new int[]{-1,0,1}) for (int dy : new int[]{-1,0,1})
                for (boolean elytra : new boolean[]{false,true}) for (int tracks : new int[]{0,1,5,16}) {
                    int width = profile[0]+dx, height = profile[1]+dy;
                    var layout = LoomWorkspaceLayout.create(width,height,elytra,tracks);
                    var window = new LoomWorkspaceLayout.Rect(0,0,width,height-18);
                    var regions = List.of(layout.tools(),layout.toolbar(),layout.canvas(),layout.context(),
                            layout.preview(),layout.inspectorTabs(),layout.inspector());
                    for (var r : regions) {
                        assertTrue(window.contains(r), "Offscreen " + r);
                        assertTrue(r.width() > 0 && r.height() > 0, "Empty " + r);
                    }
                    assertTrue(layout.canvas().height() >= (elytra ? 90 : 180));
                    assertTrue(layout.inspector().height() >= 162, "Six-row property budget");
                    assertTrue(layout.canvas().bottom() < layout.context().top());
                    assertTrue(layout.preview().bottom() < layout.inspectorTabs().top());
                    assertTrue(layout.tools().right() < layout.canvas().left());
                    assertTrue(layout.canvas().right() < layout.preview().left());
                    if (elytra) assertTrue(layout.context().bottom() < layout.timeline().top());
                }
    }
    @Test void emptyTimelineReturnsSpaceToWingCanvas() {
        var empty = LoomWorkspaceLayout.create(640,360,true,0);
        var tracks = LoomWorkspaceLayout.create(640,360,true,5);
        assertEquals(66, empty.timeline().height());
        assertTrue(empty.canvas().height() > tracks.canvas().height());
    }
}
