package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.premium.PremiumGuiRenderer;
import dev.loomstudios.client.ui.premium.PremiumPaint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** Interactive renderer comparison. Existing authoring stays reachable and unchanged. */
public final class LoomPremiumPrototypeScreen extends Screen {
    private static final int TEXT=0xFFE9F2FA, MUTED=0xFF97ABBF, CYAN=0xFF37D6E8;
    private final boolean smooth;
    private final List<Control> controls=new ArrayList<>();
    private String selected="Pencil";
    private boolean previewVisible=true;
    private int inspectorX, canvasX, canvasW, bodyY, bodyH, inspectorW;
    public LoomPremiumPrototypeScreen(boolean smooth) { super(Component.literal("Loom UI prototype")); this.smooth=smooth; }
    @Override protected void init() {
        controls.clear();
        PremiumPaint.resetMetrics();
        if(minecraft.player!=null) ClientProjectWorkspace.ensure(minecraft.player.getUUID());
        boolean compact=width<800;
        bodyY=65;bodyH=height-bodyY-33;
        int rail=compact?36:112; inspectorW=compact?158:216;
        inspectorX=width-inspectorW-12;canvasX=rail+20;canvasW=inspectorX-canvasX-10;
        addControl(12,35,70,22,"Home","house",()->minecraft.setScreen(new LoomHomeScreen()));
        addControl(88,35,96,22,"Open editor","pencil",()->minecraft.setScreen(new CapeEditorScreen(this)));
        addControl(190,35,92,22,smooth?"Native UI":"Smooth UI","layers",()->minecraft.setScreen(new LoomPremiumPrototypeScreen(!smooth)));
        addControl(width-160,35,70,22,"Preview","eye",()->{previewVisible=!previewVisible;rebuildWidgets();});
        addControl(width-84,35,72,22,"Close","x",this::onClose);
        String[] tools={"Pencil","Eraser","Fill","Eyedropper","Select","Line","Rectangle","Circle"};
        String[] icons={"pencil","eraser","paint-bucket","pipette","square-dashed","minus","square","circle"};
        for(int i=0;i<tools.length;i++){
            String tool=tools[i];
            addControl(12,bodyY+12+i*27,rail,23,compact?"":tool,icons[i],()->selected=tool).setTooltip(Tooltip.create(Component.literal(tool)));
        }
        addControl(inspectorX+10,height-58,inspectorW-20,22,"Layer manager","layers",
                ()->minecraft.setScreen(new LoomLayerManagerScreen(this,false,null)));
        if(previewVisible) {
            var preview=new LoomPlayerPreviewWidget(inspectorX+8,bodyY+30,inspectorW-16,
                    Math.min(compact?105:165,bodyH/2), ClientProjectWorkspace::project);
            addRenderableWidget(preview);
        }
    }
    private AbstractButton addControl(int x,int y,int w,int h,String label,String icon,Runnable action) {
        Control c=new Control(x,y,w,h,label,icon,action);controls.add(c);
        if(smooth)return addRenderableWidget(c);
        return addRenderableWidget(new LoomButton(x,y,w,h,Component.literal(label.isEmpty()?icon:label),action));
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta) {
        if(smooth) PremiumGuiRenderer.submit(graphics,()->paint(mouseX,mouseY));
        else {
            graphics.fill(0,0,width,height,0xFF0B121D);
            graphics.fill(canvasX,bodyY,canvasX+canvasW,height-33,0xFF172437);
            graphics.drawString(font,"Loom Studios / Renderer comparison",12,12,TEXT,false);
            graphics.drawString(font,"Native Minecraft controls — identical bounds",canvasX+10,bodyY+10,TEXT,false);
        }
        super.render(graphics,mouseX,mouseY,delta);
    }
    private void paint(int mouseX,int mouseY) {
        PremiumPaint.box(0,0,width,height,0,0xFF080F19);
        PremiumPaint.box(0,0,width,28,0,0xFF142236);
        PremiumPaint.text("LOOM",12,6,16,CYAN);
        PremiumPaint.text("STUDIOS",62,7,13,TEXT);
        PremiumPaint.text("Design workspace",138,8,11,MUTED);
        panel(10,bodyY,canvasX-20,bodyH);
        panel(canvasX,bodyY,canvasW,bodyH);
        panel(inspectorX,bodyY,inspectorW,bodyH);
        PremiumPaint.text("Cape canvas",canvasX+12,bodyY+10,13,TEXT);
        PremiumPaint.text("Outside / Back  ·  400%",canvasX+12,bodyY+29,10,MUTED);
        float x=canvasX+12,y=bodyY+50,w=canvasW-24,h=bodyH-83;
        PremiumPaint.clip(x,y,w,h,()-> {
            PremiumPaint.box(x,y,w,h,5,0xFF0C1523);
            for(float gx=x;gx<x+w;gx+=12)PremiumPaint.line(gx,y,gx,y+h,.5f,0xFF182738);
            for(float gy=y;gy<y+h;gy+=12)PremiumPaint.line(x,gy,x+w,gy,.5f,0xFF182738);
            float ch=Math.min(h-22,w*1.3f),cw=ch*.625f,cx=x+(w-cw)/2,cy=y+(h-ch)/2;
            PremiumPaint.box(cx-2,cy-2,cw+4,ch+4,2,0xFF483067);
            PremiumPaint.box(cx,cy,cw,ch,0,0xFF161A40);
            PremiumPaint.circle(cx+cw*.52f,cy+ch*.33f,cw*.22f,0xFF72E7F0);
            PremiumPaint.box(cx+cw*.56f,cy+ch*.19f,cw*.19f,ch*.28f,4,0xFF161A40);
            for(int i=0;i<12;i++) {
                float sx=cx+cw*(.12f+(i*37%77)/100f),sy=cy+ch*(.08f+(i*23%80)/100f);
                PremiumPaint.circle(sx,sy,.6f,i%2==0?CYAN:0xFFB19AFF);
            }
            PremiumPaint.box(cx,cy+ch-4,cw,4,0,0xFF9858DE);
        });
        PremiumPaint.text(selected+"  ·  1 px  ·  Symmetry off",canvasX+12,height-55,10,MUTED);
        PremiumPaint.text("Preview & layers",inspectorX+12,bodyY+10,12,TEXT);
        int layerY=bodyY+34+(previewVisible?Math.min(width<800?105:165,bodyH/2)+10:0);
        PremiumPaint.text("LAYERS",inspectorX+12,layerY,9,MUTED);
        String[] layers={"Moon","Starlight","Border","Base gradient"};
        int visible=Math.max(1,Math.min(layers.length,(height-68-layerY-17)/24));
        for(int i=0;i<visible;i++) {
            int ly=layerY+17+i*24;
            PremiumPaint.box(inspectorX+8,ly,inspectorW-16,21,4,i==0?0xFF183A4B:0xFF152234);
            PremiumPaint.icon("eye",inspectorX+13,ly+4,13,TEXT);
            PremiumPaint.text(layers[i],inspectorX+33,ly+5,10,TEXT);
            PremiumPaint.text("100%",inspectorX+inspectorW-40,ly+6,9,MUTED);
        }
        for(Control control:controls) control.paint(mouseX,mouseY);
        PremiumPaint.text("Renderer prototype · artwork shown here is a sample",12,height-20,9,MUTED);
        PremiumPaint.text(PremiumPaint.metrics(),Math.max(12,width-290),height-9,8,MUTED);
    }
    private static void panel(int x,int y,int w,int h) {
        PremiumPaint.box(x,y+3,w,h,7,0x55000000);
        PremiumPaint.box(x,y,w,h,6,0xFF101C2C);
        PremiumPaint.outline(x,y,w,h,6,0xFF304359);
    }
    private final class Control extends AbstractButton {
        private final String label,icon;private final Runnable action;
        Control(int x,int y,int w,int h,String label,String icon,Runnable action) {
            super(x,y,w,h,Component.literal(label.isEmpty()?icon:label));this.label=label;this.icon=icon;this.action=action;
        }
        void paint(int mouseX,int mouseY) {
            boolean hover=mouseX>=getX()&&mouseX<getRight()&&mouseY>=getY()&&mouseY<getBottom();
            String selectedIcon=switch(selected) {
                case "Fill" -> "paint-bucket";case "Eyedropper" -> "pipette";
                case "Select" -> "square-dashed";case "Line" -> "minus";case "Rectangle" -> "square";
                default -> selected.toLowerCase(java.util.Locale.ROOT);
            };
            boolean activeTool=label.equals(selected)||label.isEmpty()&&icon.equals(selectedIcon);
            boolean hot=hover||isFocused();
            PremiumPaint.box(getX(),getY(),getWidth(),getHeight(),4,activeTool?0xFF164454:hot?0xFF233A50:0xFF192A3E);
            PremiumPaint.outline(getX(),getY(),getWidth(),getHeight(),4,activeTool?CYAN:hot?0xFF6C97AE:0xFF354B61);
            PremiumPaint.icon(icon,getX()+(label.isEmpty()?(getWidth()-14)/2:7),getY()+4,14,activeTool?CYAN:TEXT);
            if(!label.isEmpty())PremiumPaint.text(label,getX()+26,getY()+6,10,TEXT);
        }
        @Override protected void renderContents(GuiGraphics graphics,int x,int y,float delta) {}
        @Override public void onPress(InputWithModifiers input) { action.run(); }
        @Override public void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }
}
