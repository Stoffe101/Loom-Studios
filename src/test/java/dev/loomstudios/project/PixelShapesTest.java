package dev.loomstudios.project;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PixelShapesTest {
    private Set<String> points(int x0,int y0,int x1,int y1,boolean filled) {
        Set<String> result=new HashSet<>(); PixelShapes.ellipse(x0,y0,x1,y1,filled,(x,y)->result.add(x+","+y)); return result;
    }
    @Test void dragDirectionAndSymmetryPreserveTheShape() {
        for(boolean filled:new boolean[]{false,true}) {
            var pixels=points(2,3,11,14,filled);
            assertEquals(pixels,points(11,14,2,3,filled));
            for(String p:pixels) {
                String[] xy=p.split(",");int x=Integer.parseInt(xy[0]),y=Integer.parseInt(xy[1]);
                assertTrue(x>=2&&x<=11&&y>=3&&y<=14);
                assertTrue(pixels.contains((13-x)+","+y)); assertTrue(pixels.contains(x+","+(17-y)));
            }
        }
    }
    @Test void outlineIsHollowAndFillRetainsItsBoundary() {
        var outline=points(0,0,8,8,false);var fill=points(0,0,8,8,true);
        assertFalse(outline.contains("4,4"));assertTrue(fill.contains("4,4"));assertTrue(fill.containsAll(outline));
        assertTrue(outline.contains("4,0"));assertTrue(outline.contains("8,4"));
    }
    @Test void singlePixelAndThinDragsRemainPaintable() {
        assertEquals(Set.of("3,5"),points(3,5,3,5,false));
        assertEquals(7,points(3,2,3,8,false).size());assertEquals(7,points(2,3,8,3,true).size());
    }
}
