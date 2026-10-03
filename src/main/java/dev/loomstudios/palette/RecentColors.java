package dev.loomstudios.palette;
import java.util.*;

/** Bounded MRU colors; malformed local preferences are ignored, not propagated. */
public final class RecentColors {
    public static final int LIMIT=16;
    private final List<Integer> colors=new ArrayList<>();
    public RecentColors(String encoded){for(String token:encoded.split(",")){try{if(token.length()==8){int value=(int)Long.parseUnsignedLong(token,16);if(!colors.contains(value)&&colors.size()<LIMIT)colors.add(value);}}catch(NumberFormatException ignored){}}}
    public boolean use(int color){if(!colors.isEmpty()&&colors.getFirst()==color)return false;colors.remove(Integer.valueOf(color));colors.addFirst(color);if(colors.size()>LIMIT)colors.removeLast();return true;}
    public List<Integer> colors(){return List.copyOf(colors);}
    public String encode(){return colors.stream().map(c->String.format(Locale.ROOT,"%08X",c)).collect(java.util.stream.Collectors.joining(","));}
}
