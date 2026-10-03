package dev.loomstudios.client.screen;

import dev.loomstudios.client.ui.LoomMiddlePanTarget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Captures middle drags until release; vanilla container drag routing is left-only. */
public abstract class LoomPointerScreen extends Screen {
    private AbstractWidget middleTarget;
    private dev.loomstudios.client.ui.LoomChoicePopup<?> choicePopup;
    protected LoomPointerScreen(Component title) {
        super(title);
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterRender(this).register((screen,g,mx,my,dt)->{if(choicePopup!=null)choicePopup.render(g,mx,my);});
    }
    @Override public void render(net.minecraft.client.gui.GuiGraphics g,int mx,int my,float dt){super.render(g,hasChoices()?-1:mx,hasChoices()?-1:my,dt);}
    public boolean hasChoices(){return choicePopup!=null;}
    public <T> void showChoices(dev.loomstudios.client.ui.LoomButton anchor,String title,java.util.List<dev.loomstudios.client.ui.LoomChoicePopup.Option<T>> options,T selected,java.util.function.Consumer<T> choose){
        middleTarget=null;choicePopup=new dev.loomstudios.client.ui.LoomChoicePopup<>(width,height,anchor,title,options,selected,choose,()->choicePopup=null);
    }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){return choicePopup!=null?choicePopup.key(e):super.keyPressed(e);}
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){return choicePopup!=null?choicePopup.wheel(dy):super.mouseScrolled(x,y,dx,dy);}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick) {
        if(choicePopup!=null)return choicePopup.click(event);
        if(event.button()==2) for(int i=children().size()-1;i>=0;i--) {
            if(children().get(i) instanceof AbstractWidget w && w instanceof LoomMiddlePanTarget
                    && w.visible && w.isMouseOver(event.x(),event.y()) && w.mouseClicked(event,doubleClick)) {
                middleTarget=w; return true;
            }
        }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) {
        if(choicePopup!=null)return true;
        if(event.button()==2 && middleTarget!=null) return middleTarget.mouseDragged(event,dx,dy);
        return super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if(event.button()==2 && middleTarget!=null) {
            var target=middleTarget;middleTarget=null;return target.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }
    @Override public void removed() { middleTarget=null;choicePopup=null;super.removed(); }
}
