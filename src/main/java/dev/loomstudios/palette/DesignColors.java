package dev.loomstudios.palette;
import java.util.*;

public final class DesignColors {
    private DesignColors(){}
    /** Frequency-ranked visible colors; transparent pixels are not palette entries. */
    public static List<Integer> collect(int[]... images){
        Map<Integer,Integer> counts=new HashMap<>();for(int[] image:images)for(int color:image)if((color>>>24)!=0)counts.merge(color,1,Integer::sum);
        return counts.entrySet().stream().sorted(Comparator.<Map.Entry<Integer,Integer>>comparingInt(Map.Entry::getValue).reversed().thenComparing((a,b)->Integer.compareUnsigned(a.getKey(),b.getKey()))).limit(ColorPalette.MAX_COLORS).map(Map.Entry::getKey).toList();
    }
}
