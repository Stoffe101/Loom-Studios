package dev.loomstudios.client.ui;

import dev.loomstudios.project.AnimationPreset;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Visual animation recipes with deterministic signal thumbnails. This is a
 * compact-native composition, not the Advanced timeline squeezed into cards.
 * The gallery only selects a recipe: it never changes the project on click.
 */
public final class LoomAnimationPresetGallery extends AbstractWidget {
    private final Supplier<AnimationPreset> selected;
    private final Consumer<AnimationPreset> onChoose;
    private int columns, cardWidth, cardHeight, gridX, gridY;
    private static final AnimationPreset[] PRESETS=AnimationPreset.values();

    public LoomAnimationPresetGallery(int x,int y,int w,int h,
            Supplier<AnimationPreset> selected,Consumer<AnimationPreset> onChoose) {
        super(x,y,w,h,Component.literal("Animation presets"));
        this.selected=selected;
        this.onChoose=onChoose;
        layout();
        setTooltip(null);
    }

    private void layout(){
        columns=getWidth()>=780?4:getWidth()>=330?3:2;
        int rows=(PRESETS.length+columns-1)/columns;
        int gap=7;
        cardWidth=Math.max(48, Math.min(getWidth()/columns, (getWidth()-(columns-1)*gap)/columns));
        cardHeight=Math.max(34,Math.min(100,(getHeight()-(rows-1)*gap)/rows));
        gridX=getX()+Math.max(0,(getWidth()-(cardWidth*columns+(columns-1)*gap))/2);
        gridY=getY()+Math.max(0,(getHeight()-(cardHeight*rows+(rows-1)*gap))/2);
    }

    private int cardX(int i){return gridX+(i%columns)*(cardWidth+7);}
    private int cardY(int i){return gridY+(i/columns)*(cardHeight+7);}
    private int hovered(double x,double y){
        for(int i=0;i<PRESETS.length;i++)
            if(x>=cardX(i)&&x<cardX(i)+cardWidth&&y>=cardY(i)
                &&y<cardY(i)+cardHeight)return i;
        return -1;
    }

    @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
        layout();
        int hovered=hovered(mx,my);
        for(int i=0;i<PRESETS.length;i++){
            var preset=PRESETS[i];int x=cardX(i),y=cardY(i),w=cardWidth,h=cardHeight;
            boolean active=selected.get()==preset;
            int border=active?LoomUiTheme.ACCENT:(hovered==i?LoomUiTheme.ACCENT_ALT:LoomUiTheme.BORDER);
            int background=active?0xFF203E52:(hovered==i?0xFF213344:LoomUiTheme.PANEL_INNER);
            g.fill(x,y,x+w,y+h,border);
            g.fill(x+1,y+1,x+w-1,y+h-1,background);
            int graphX=x+7,graphY=y+7,graphW=Math.min(w-16, h>=60?45:28),graphH=Math.min(23,h-16);
            renderSignal(g,graphX,graphY,graphW,graphH,preset);
            int textX=graphX+graphW+5;
            int maxText=Math.max(8,x+w-textX-5);
            String title=Minecraft.getInstance().font.plainSubstrByWidth(preset.label(),maxText);
            dev.loomstudios.client.ui.premium.PremiumText.drawString(g,Minecraft.getInstance().font,
                Component.literal(title),textX,y+7,
                active?LoomUiTheme.ACCENT:LoomUiTheme.TEXT,false);
            if(h>=53){
                String hint=switch(preset){
                    case GLOW->"Gentle light";case PULSE->"Breathe in/out";
                    case SHIMMER->"Sparkle highlights";case STARS->"Starry blinking";
                    case HUE->"Shift colors";case SCROLL->"Slide side to side";
                    case WAVE->"Flowing pattern";
                };
                hint=Minecraft.getInstance().font.plainSubstrByWidth(hint,Math.max(8,w-14));
                dev.loomstudios.client.ui.premium.PremiumText.drawString(g,Minecraft.getInstance().font,
                    Component.literal(hint),x+7,y+h-17,LoomUiTheme.TEXT_MUTED,false);
            }
            if(active)g.fill(x+w-5,y+4,x+w-3,y+9,LoomUiTheme.ACCENT);
        }
    }

    private void renderSignal(GuiGraphics g,int x,int y,int w,int h,AnimationPreset p){
        final int accent= switch(p){case GLOW->0xFFF8D388;case PULSE->0xFF8FCFEF;
            case SHIMMER->0xFFD8B7FF;case STARS->0xFFFFE6A3;
            case HUE->0xFFCB8DDB;case SCROLL->0xFF62DFBD;case WAVE->0xFF75A4F4;};
        g.fill(x,y,x+w,y+h,0xFF162733);
        int last=-1;
        for(int i=1;i<w-2;i++){
            double t=i/(double)Math.max(1,w-3);
            double v=switch(p){
                case GLOW,PULSE -> .5+.45*Math.cos(2*Math.PI*t);
                case STARS -> (t>.36&&t<.57)? .08: .87;
                case SHIMMER -> .35+.24*Math.sin(12*Math.PI*t)+.18*Math.sin(29*Math.PI*t);
                case HUE -> t;case SCROLL -> t<.5? t*2 : (t-.5)*2;
                case WAVE -> .5+.45*Math.sin(4*Math.PI*t);
            };
            int yy=y+2+(int)Math.round((h-5)*Math.max(0,Math.min(1,v)));
            if(last!=-1)g.fill(x+i,y+Math.min(last,yy),x+i+1,y+Math.max(last,yy)+1,accent);
            last=yy;
        }
    }

    @Override public void onClick(MouseButtonEvent e,boolean twice) {
        int i=hovered(e.x(),e.y());
        if(e.button()==0&&i>=0)onChoose.accept(PRESETS[i]);
    }

    @Override protected void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE,
            Component.literal("Choose an animation effect, then Try before applying to a layer."));
    }
}
