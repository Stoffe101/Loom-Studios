package dev.loomstudios.client.ui.premium;

import dev.loomstudios.client.screen.LoomPremiumPrototypeScreen;
import dev.loomstudios.client.ui.LoomButton;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix3x2f;
import java.util.ArrayList;
import java.util.List;

/** One transparent, revision-cached overlay for studio UI. Existing widgets own input. */
public final class PremiumControls {
    private static final List<Command> pending=new ArrayList<>();
    private static List<Command> previous=List.of();
    private static Screen owner,previousOwner;
    private static GuiGraphics target;
    private static int previousWidth,previousHeight;
    private static long revision;
    private PremiumControls() {}
    public static void register() {
        ScreenEvents.AFTER_INIT.register((client,screen,width,height)-> {
            if(!screen.getClass().getPackageName().equals("dev.loomstudios.client.screen")
                    ||screen instanceof LoomPremiumPrototypeScreen)return;
            ScreenEvents.beforeRender(screen).register((s,g,mx,my,dt)-> {
                owner=s;target=g;pending.clear();
            });
            ScreenEvents.afterRender(screen).register((s,g,mx,my,dt)->finish(g));
            ScreenEvents.remove(screen).register(s-> {
                if(owner==s){owner=null;target=null;pending.clear();}
                if(previousOwner==s){previousOwner=null;previous=List.of();}
            });
        });
    }
    // Conservative subset of Inter's bundled Latin/Greek/Cyrillic coverage.
    // Other scripts and symbols retain Minecraft's font providers instead of missing glyphs.
    private static boolean supports(String value) {
        return value.codePoints().allMatch(c->c>=32&&c<=0x052F||c>=0x2000&&c<=0x206F||c>=0x20A0&&c<=0x20CF);
    }
    public static boolean button(GuiGraphics g,int x,int y,int w,int h,String label,LoomButton.Icon icon,
                                 boolean iconOnly,boolean active,boolean hot,boolean focused,
                                 boolean selected,boolean primary,boolean danger) {
        if(g!=target||owner==null||!supports(label))return false;
        pending.add(new Button(x,y,w,h,label,null,icon,iconOnly,active,hot,focused,selected,primary,danger,
                new Matrix3x2f(g.pose())));
        return true;
    }
    public static boolean card(GuiGraphics g,int x,int y,int w,int h,String label,String subtitle,
                               LoomButton.Icon icon,boolean active,boolean hot,boolean focused,boolean primary) {
        if(g!=target||owner==null||!supports(label)||!supports(subtitle))return false;
        pending.add(new Button(x,y,w,h,label,subtitle,icon,false,active,hot,focused,false,primary,false,
                new Matrix3x2f(g.pose())));
        return true;
    }
    public static boolean label(GuiGraphics g,String text,int x,int y,int width,float size,int ink,boolean centered) {
        if(g!=target||owner==null||!supports(text))return false;
        pending.add(new Label(x,y,Math.max(1,width),(int)Math.ceil(size+3),text,size,ink,centered,false,new Matrix3x2f(g.pose())));
        return true;
    }
    public static boolean brand(GuiGraphics g,int x,int y,int width,float size) {
        if(g!=target||owner==null)return false;
        pending.add(new Label(x,y,width,(int)Math.ceil(size+4),"Loom Studios",size,0xFFE7EEF7,true,true,new Matrix3x2f(g.pose())));
        return true;
    }
    public static boolean icon(GuiGraphics g,String name,int x,int y,int size,int ink) {
        if(g!=target||owner==null)return false;
        pending.add(new Symbol(x,y,size,size,name,ink,new Matrix3x2f(g.pose())));return true;
    }
    public static boolean icon(GuiGraphics g,LoomButton.Icon icon,int x,int y,int size,int ink) {
        return icon!=LoomButton.Icon.NONE&&icon(g,iconName(icon),x,y,size,ink);
    }
    /** Clip earlier paint around a later native popup, preserving visible portions outside it. */
    public static void occlude(GuiGraphics g,int x,int y,int w,int h) {
        if(g!=target)return;
        List<Command> visible=new ArrayList<>();
        for(Command c:pending) {
            int left=Math.max(x,c.x()),top=Math.max(y,c.y());
            int right=Math.min(x+w,c.x()+c.w()),bottom=Math.min(y+h,c.y()+c.h());
            if(right<=left||bottom<=top){visible.add(c);continue;}
            cut(visible,c,c.x(),c.y(),left-c.x(),c.h());
            cut(visible,c,right,c.y(),c.x()+c.w()-right,c.h());
            cut(visible,c,left,c.y(),right-left,top-c.y());
            cut(visible,c,left,bottom,right-left,c.y()+c.h()-bottom);
        }
        pending.clear();pending.addAll(visible);
    }
    private static void cut(List<Command> out,Command source,int x,int y,int w,int h) {
        if(w>0&&h>0)out.add(new Cut(x,y,w,h,source));
    }
    private static void finish(GuiGraphics g) {
        if(g!=target)return;
        var snapshot=List.copyOf(pending);
        if(previousOwner!=owner||previousWidth!=g.guiWidth()||previousHeight!=g.guiHeight()
                ||!snapshot.equals(previous)) {
            revision=PremiumGuiRenderer.nextRevision();previous=snapshot;previousOwner=owner;
            previousWidth=g.guiWidth();previousHeight=g.guiHeight();
        }
        if(!snapshot.isEmpty())PremiumGuiRenderer.submit(g,revision,()->snapshot.forEach(Command::paint));
        target=null;pending.clear();
    }
    private interface Command {
        int x();int y();int w();int h();void paint();
    }
    private record Cut(int x,int y,int w,int h,Command source) implements Command {
        @Override public void paint(){PremiumPaint.clip(x,y,w,h,source::paint);}
    }
    private record Label(int x,int y,int w,int h,String text,float size,int ink,boolean centered,boolean gradient,
                         Matrix3x2f pose) implements Command {
        @Override public void paint() {
            PremiumPaint.transformed(pose,()-> {
                if(gradient)PremiumPaint.brand(text,x,y,w,size);
                else PremiumPaint.fittedText(text,x,y,w,size,ink,centered);
            });
        }
    }
    private record Symbol(int x,int y,int w,int h,String name,int ink,Matrix3x2f pose) implements Command {
        @Override public void paint(){PremiumPaint.transformed(pose,()->PremiumPaint.icon(name,x,y,w,ink));}
    }
    private record Button(int x,int y,int w,int h,String label,String subtitle,LoomButton.Icon icon,boolean iconOnly,
                          boolean active,boolean hot,boolean focused,boolean selected,boolean primary,
                          boolean danger,Matrix3x2f pose) implements Command {
        @Override public void paint() {
            PremiumPaint.transformed(pose,()-> {
                int ink=active?0xFFE7EEF7:0xFF718297;
                int accent=danger?0xFFFF7C91:0xFF47D7E8;
                int surface=!active?0xFF141E2B:selected||primary?0xFF173C4A:hot?0xFF23354A:0xFF172538;
                int edge=!active?0xFF273548:selected||primary?accent:focused?0xFFAC93ED:hot?0xFF648199:0xFF3A4F65;
                PremiumPaint.box(x,y+1,w,h,4,0x60040911);
                PremiumPaint.surface(x,y,w,h,4,surface);
                PremiumPaint.outline(x,y,w,h,4,edge);
                if((selected||primary)&&active)PremiumPaint.box(x+1,y+4,2,Math.max(1,h-8),1,accent);
                int size=Math.max(10,Math.min(subtitle!=null&&h>=34?20:16,h-6));
                if(icon!=LoomButton.Icon.NONE) {
                    float ix=iconOnly?x+(w-size)/2f:x+6;
                    PremiumPaint.icon(iconName(icon),ix,y+(h-size)/2f,size,
                            active&&(selected||hot||primary)?accent:ink);
                }
                if(!iconOnly) {
                    float left=icon==LoomButton.Icon.NONE?x+4:x+size+12;
                    float room=Math.max(1,x+w-(subtitle==null?5:20)-left);
                    boolean description=subtitle!=null&&!subtitle.isEmpty()&&h>=34;
                    PremiumPaint.fittedText(label,left,y+(h-(description?24:10))/2f,room,10,ink,
                            icon==LoomButton.Icon.NONE);
                    if(description)PremiumPaint.fittedText(subtitle,left,y+(h-24)/2f+14,room,9,0xFF99ADC4,false);
                    if(subtitle!=null)PremiumPaint.icon("chevron-right",x+w-17,y+(h-12)/2f,12,hot?accent:0xFF8199B3);
                }
            });
        }
    }
    private static String iconName(LoomButton.Icon icon) {
        return switch(icon) {
            case HOME->"house";case CAPE->"cape";case ELYTRA->"elytra";
            case PENCIL->"pencil";case ERASER->"eraser";case FILL->"paint-bucket";
            case EYEDROPPER->"pipette";case SELECT->"square-dashed";case LINE->"line";
            case RECTANGLE->"square";case CIRCLE->"circle";case CLOSE->"x";
            case ROTATE_LEFT,RESET->"rotate-ccw";case ROTATE_RIGHT->"rotate-cw";
            case MOVE->"move";case FLIP_H->"flip-horizontal";case FLIP_V->"flip-vertical";
            case UNDO->"undo-2";case REDO->"redo-2";case ZOOM_IN->"zoom-in";case ZOOM_OUT->"zoom-out";
            case PALETTE->"palette";case LAYERS->"layers";case PLUS->"plus";case COPY->"copy";
            case DELETE->"trash-2";case UP->"chevron-up";case DOWN->"chevron-down";
            case MINUS->"minus";case LEFT->"chevron-left";case RIGHT->"chevron-right";
            case GRID->"grid-2x2";case IMAGE->"image";case GRADIENT->"blend";case LOCK->"lock";
            case EMISSIVE->"sparkles";case SAVE->"save";case FOLDER->"folder";case EQUIP->"shirt";
            case SHARE->"share-2";case EXPORT->"download";case SETTINGS->"settings";
            case PLAY->"play";case PAUSE->"pause";case LOOP->"repeat";case BACK->"arrow-left";
            case NONE->throw new IllegalArgumentException("Empty icon");
        };
    }
}
