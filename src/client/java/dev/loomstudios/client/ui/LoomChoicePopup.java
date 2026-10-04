package dev.loomstudios.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.Consumer;

/** A bounded modal choice list; cancellation never applies an edit. */
public final class LoomChoicePopup<T> {
    public record Option<T>(T value,String label,String detail) { }
    private final List<Option<T>> options;
    private final Consumer<T> choose;
    private final Runnable close;
    private final String title;
    private final int x,y,width,rows;
    private int focused,scroll;
    public LoomChoicePopup(int screenWidth,int screenHeight,LoomButton anchor,String title,List<Option<T>> options,T selected,Consumer<T> choose,Runnable close){
        this.options=List.copyOf(options);this.choose=choose;this.close=close;this.title=title;
        if(options.isEmpty())throw new IllegalArgumentException("Empty choice list");
        width=Math.min(248,screenWidth-16);rows=Math.max(1,Math.min(options.size(),(screenHeight-68)/32));
        int h=28+rows*32;
        x=Math.max(8,Math.min(screenWidth-width-8,anchor.getRight()-width));
        int below=anchor.getBottom()+4;
        y=Math.max(8,Math.min(screenHeight-28-h,below+h<=screenHeight-28?below:anchor.getY()-h-4));
        for(int i=0;i<options.size();i++)if(java.util.Objects.equals(options.get(i).value(),selected))focused=i;
        keepVisible();narrate();
    }
    private void narrate(){var option=options.get(focused);net.minecraft.client.Minecraft.getInstance().getNarrator().saySystemNow(Component.literal(title+". "+option.label()+". "+option.detail()+". Up/down to choose, Enter to apply, Escape to cancel."));}
    private static void label(GuiGraphics g,String text,int x,int y,int width,int size,int color){
        if(!dev.loomstudios.client.ui.premium.PremiumControls.label(g,text,x,y,width,size,color,false)){
            var f=net.minecraft.client.Minecraft.getInstance().font;g.drawString(f,f.plainSubstrByWidth(text,width),x,y,color,false);
        }
    }
    public int left(){return x;} public int top(){return y;} public int height(){return 28+rows*32;} public int width(){return width;}
    private void keepVisible(){scroll=Math.max(0,Math.min(Math.max(0,options.size()-rows),Math.max(focused-rows+1,Math.min(scroll,focused))));}
    public void render(GuiGraphics g,int mx,int my){
        LoomScreenChrome.panel(g,x,y,x+width,y+height());
        label(g,title,x+8,y+6,width-16,9,LoomUiTheme.ACCENT);
        for(int row=0;row<rows;row++){
            int index=scroll+row,ry=y+23+row*32;var option=options.get(index);
            boolean hot=mx>=x+6&&mx<x+width-6&&my>=ry&&my<ry+30;
            g.fill(x+6,ry,x+width-6,ry+30,hot||index==focused?LoomUiTheme.BUTTON_HOVER:LoomUiTheme.BUTTON);
            if(index==focused)g.fill(x+6,ry,x+8,ry+30,LoomUiTheme.ACCENT);
            label(g,option.label(),x+13,ry+3,width-26,9,LoomUiTheme.TEXT);
            label(g,option.detail(),x+13,ry+16,width-26,8,LoomUiTheme.TEXT_MUTED);
        }
        if(options.size()>rows){int bar=Math.max(10,rows*32*rows/options.size());int sy=y+23+(rows*32-bar)*scroll/(options.size()-rows);g.fill(x+width-4,sy,x+width-2,sy+bar,LoomUiTheme.ACCENT);}
    }
    public boolean click(MouseButtonEvent event){
        if(event.button()==0&&event.x()>=x+6&&event.x()<x+width-6){
            int row=(int)Math.floor((event.y()-y-23)/32);
            if(row>=0&&row<rows){T value=options.get(scroll+row).value();close.run();choose.accept(value);return true;}
        }
        close.run();return true;
    }
    public boolean key(KeyEvent e){
        switch(e.key()){
            case 256->close.run();
            case 265-> {focused=Math.floorMod(focused-1,options.size());keepVisible();}
            case 264,258->{focused=(focused+1)%options.size();keepVisible();}
            case 268->{focused=0;keepVisible();}
            case 269->{focused=options.size()-1;keepVisible();}
            case 257,335,32->{T value=options.get(focused).value();close.run();choose.accept(value);}
            default->{ }
        }
        if(e.key()==265||e.key()==264||e.key()==258||e.key()==268||e.key()==269)narrate();
        return true;
    }
    public boolean wheel(double dy){scroll=Math.max(0,Math.min(options.size()-rows,scroll-(int)Math.signum(dy)));focused=Math.max(scroll,Math.min(scroll+rows-1,focused));return true;}
}
