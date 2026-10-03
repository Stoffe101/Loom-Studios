package dev.loomstudios.client.screen;

import dev.loomstudios.client.ui.LoomMiddlePanTarget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Captures middle drags until release; vanilla container drag routing is left-only. */
public abstract class LoomPointerScreen extends Screen {
    private AbstractWidget middleTarget;
    protected LoomPointerScreen(Component title) { super(title); }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick) {
        if(event.button()==2) for(int i=children().size()-1;i>=0;i--) {
            if(children().get(i) instanceof AbstractWidget w && w instanceof LoomMiddlePanTarget
                    && w.visible && w.isMouseOver(event.x(),event.y()) && w.mouseClicked(event,doubleClick)) {
                middleTarget=w; return true;
            }
        }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) {
        if(event.button()==2 && middleTarget!=null) return middleTarget.mouseDragged(event,dx,dy);
        return super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if(event.button()==2 && middleTarget!=null) {
            var target=middleTarget;middleTarget=null;return target.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }
    @Override public void removed() { middleTarget=null;super.removed(); }
}
