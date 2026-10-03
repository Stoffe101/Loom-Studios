package dev.loomstudios.ui;

import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.PixelSelection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CanvasViewportTransformTest {
    @Test void boundariesAndInputAgreeForEveryFaceResolutionZoomAndPan() {
        for (int scale : new int[]{1,2,4}) for (CapeUvRegion face : CapeUvRegion.values())
            for (float zoom : new float[]{1,1.25F,1.5F,2,3,4,6,8}) for (int pan : new int[]{-311,0,287}) {
                int columns = face.width(scale), rows = face.height(scale);
                var t = CanvasViewportTransform.fit(43, 79, 421, 281, columns, rows, zoom, pan, -pan);
                for (int y = 0; y < rows; y++) for (int x = 0; x < columns; x++) {
                    var rect = t.selection(PixelSelection.between(x,y,x,y));
                    assertEquals(t.pixelScale(), rect.right() - rect.left());
                    assertEquals(t.pixelScale(), rect.bottom() - rect.top());
                    assertEquals(0, (rect.left() - t.left()) % t.pixelScale());
                    assertEquals(0, (rect.top() - t.top()) % t.pixelScale());
                    double mx = rect.left() + t.pixelScale() / 2.0;
                    double my = rect.top() + t.pixelScale() / 2.0;
                    if (mx >= t.clipLeft() && mx < t.clipRight() && my >= t.clipTop() && my < t.clipBottom())
                        assertArrayEquals(new int[]{x,y}, t.pixelAt(mx,my));
                }
                var all = t.selection(PixelSelection.between(0,0,columns-1,rows-1));
                assertEquals(t.drawWidth(), all.right() - all.left());
                assertEquals(t.drawHeight(), all.bottom() - all.top());
                assertNull(t.pixelAt(all.right(), all.top()));
                assertNull(t.pixelAt(all.left(), all.bottom()));
                assertNull(t.pixelAt(t.clipRight(), t.clipTop()));
            }
    }
}
