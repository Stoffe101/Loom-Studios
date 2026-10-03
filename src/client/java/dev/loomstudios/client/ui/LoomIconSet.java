package dev.loomstudios.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import java.util.Map;

/** Consistent 12-cell pixel silhouettes; no font glyphs or low-resolution atlas stretching. */
final class LoomIconSet {
    private static final Map<LoomButton.Icon,String[]> SHAPES = Map.ofEntries(
        Map.entry(LoomButton.Icon.PENCIL, new String[]{"............","........##..",".......###..","......####..",".....####...","....####....","...####.....","..####......","..###.......",".aa#........",".a..........","............"}),
        Map.entry(LoomButton.Icon.ERASER, new String[]{"............","......##....",".....####...","....######..","...######...","..######....",".######.....","..aaaa......","...aa.......","..#######...","............","............"}),
        Map.entry(LoomButton.Icon.EYEDROPPER,new String[]{"............",".......##...","......####..",".....#####..","....###.#...","...###......","..###.......",".###........",".aa.........",".a..........","............","............"}),
        Map.entry(LoomButton.Icon.SELECT,new String[]{"............",".##.##.##.#.",".#........#.","............",".#........#.",".#........#.","............",".#........#.",".#........#.","............",".##.##.##.#.","............"}),
        Map.entry(LoomButton.Icon.UNDO,new String[]{"............","...#........","..##........",".########...","..##....##..","...#.....#..",".........#..",".........#..","........##..",".....####...","............","............"}),
        Map.entry(LoomButton.Icon.REDO,new String[]{"............","........#...","........##..","...########.","..##....##..","..#.....#...","..#.........","..#.........","..##........","...####.....","............","............"}),
        Map.entry(LoomButton.Icon.CAPE,new String[]{"............","....####....","...######...","...######...","..########..","..###aa###..","..###aa###..","..###aa###..","..########..","..########..","...######...","............"}),
        Map.entry(LoomButton.Icon.ELYTRA,new String[]{"............","....#..#....","...##..##...","..###..###..",".####..####.",".###....###.",".###....###.",".##......##.",".aa......aa.","..a......a..","............","............"}),
        Map.entry(LoomButton.Icon.FILL,new String[]{"............",".....##.....","....#..#....","...##...#...","..####.##...",".#######....","..#####..a..","...###..aaa.","....#...aaa.",".........a..","............","............"}),
        Map.entry(LoomButton.Icon.HOME,new String[]{"............",".....##.....","....####....","...######...","..########..",".##########.","..########..","..###..###..","..###..###..","..###..###..","............","............"}),
        Map.entry(LoomButton.Icon.LOCK,new String[]{"............","....####....","...#....#...","...#....#...","..########..","..########..","..###..###..","..###..###..","..########..","..########..","............","............"}),
        Map.entry(LoomButton.Icon.SAVE,new String[]{"............",".#########..",".##aaaa###..",".##aaaa####.",".##########.",".##########.",".##......##.",".##.####.##.",".##.####.##.",".##########.","............","............"}),
        Map.entry(LoomButton.Icon.EXPORT,new String[]{"............",".....aa.....",".....aa.....","..a..aa..a..","...aaaaaa...","....aaaa....",".....aa.....","............",".##......##.",".##########.","............","............"})
    );
    private LoomIconSet() { }
    static boolean draw(GuiGraphics g,int x,int y,int size,LoomButton.Icon icon,int color) {
        String[] rows = SHAPES.get(icon);
        if (rows == null) return false;
        for (int row=0;row<12;row++) for(int col=0;col<12;col++) {
            char cell=rows[row].charAt(col);
            if(cell=='.')continue;
            int left=x+col*size/12,top=y+row*size/12;
            int right=x+(col+1)*size/12,bottom=y+(row+1)*size/12;
            if(right>left&&bottom>top)g.fill(left,top,right,bottom,cell=='a'&&color==LoomUiTheme.TEXT?LoomUiTheme.ACCENT:color);
        }
        return true;
    }
}
