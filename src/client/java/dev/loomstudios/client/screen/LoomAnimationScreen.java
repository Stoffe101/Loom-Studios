package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.*;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.project.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.UnaryOperator;

/** Shared Cape/Elytra animation workspace with explicit effects and drag handles. */
public final class LoomAnimationScreen extends LoomPointerScreen implements LoomAnimationTimelineWidget.Controller {
    private final Screen parent;
    private final AnimationChannel channel;
    private UUID layerId,trackId;
    private int tick;
    private double cursor;
    private boolean playing,refreshPending;
    private AnimationEffectType chosen=AnimationEffectType.PULSE;
    private LoomAnimationTimelineWidget timeline;
    private String message="1. Choose a layer · 2. Pick a preset · 3. Apply & preview · 4. Save";
    private AnimationPreset preset=AnimationPreset.GLOW;
    private float presetRate=.5F;
    private LoomPlayerPreviewWidget preview;
    private boolean advanced;
    private int guideTop,guideWidth;
    public LoomAnimationScreen(Screen parent,AnimationChannel channel,UUID layerId){super(Component.literal("Animation studio"));this.parent=parent;this.channel=channel;this.layerId=layerId;}
    private List<LoomLayer> layers(){return(channel==AnimationChannel.CAPE?ClientProjectWorkspace.project().cape():ClientProjectWorkspace.project().elytra()).layers();}
    private AnimationTrack track(){return ClientProjectWorkspace.project().animation().tracks().stream().filter(t->t.id().equals(trackId)).findFirst().orElse(null);}
    @Override protected void init(){
        int top=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+8,right=width-204,bottom=height-28;
        if(layerId==null||layers().stream().noneMatch(l->l.id().equals(layerId)))layerId=layers().getFirst().id();
        if(track()==null)trackId=ClientProjectWorkspace.project().animation().tracks().stream().filter(t->t.channel()==channel&&t.layerId().equals(layerId)).map(AnimationTrack::id).findFirst().orElse(null);
        addRenderableWidget(new LoomButton(8,top,54,20,Component.literal("Back"),this::onClose));
        addRenderableWidget(new LoomButton(66,top,Math.max(150,right-152),20,Component.literal("Layer: "+layers().stream().filter(l->l.id().equals(layerId)).findFirst().orElseThrow().name()),()->{int index=0;for(int i=0;i<layers().size();i++)if(layers().get(i).id().equals(layerId))index=i;layerId=layers().get((index+1)%layers().size()).id();trackId=null;rebuildWidgets();}));
        addRenderableWidget(new LoomButton(right-80,top,80,20,Component.literal("Save"),()->{try{ClientProjectWorkspace.save();message="Saved animation";}catch(java.io.IOException e){message="Save failed";}}));
        addRenderableWidget(new LoomButton(right+8,top,188,20,Component.literal("Undo / Ctrl+Z"),()->{ClientProjectWorkspace.undo();rebuildWidgets();}));
        int workTop=top+26;
        var view=preview==null?null:preview.viewState();
        int previewHeight=Math.max(60,Math.min(180,bottom-workTop-(advanced?158:124)));
        preview=addRenderableWidget(new LoomPlayerPreviewWidget(right+8,workTop,188,previewHeight,ClientProjectWorkspace::project,channel==AnimationChannel.CAPE?LoomPlayerPreviewWidget.Mode.CAPE:LoomPlayerPreviewWidget.Mode.ELYTRA));
        preview.restoreViewState(view);preview.setTimelineTickSupplier(()->tick);
        int y=workTop+previewHeight+5;
        button(right+8,y,188,"Preset: "+preset.label(),()->{preset=preset.next();rebuildWidgets();});
        button(right+8,y+24,92,"Rate "+presetRate+" Hz",()->{presetRate=presetRate>=2?.25F:presetRate*2;rebuildWidgets();});
        button(right+104,y+24,92,"Apply & play",this::applyPreset);
        button(right+8,y+48,92,advanced?"Presets":"Advanced",()->{advanced=!advanced;rebuildWidgets();});
        button(right+104,y+48,92,"Delete track",()->{if(track()!=null)deleteTrack(trackId);});
        if(advanced){
            button(right+8,y+72,188,"Effect: "+(track()==null?chosen:track().effect()).displayName(),()->{chosen=(track()==null?chosen:track().effect()).next();if(track()!=null)changeTrack(t->t.withEffect(chosen));rebuildWidgets();});
            LoomSlider value=addRenderableWidget(new LoomSlider(right+8,y+96,188,"Key value",()->track()==null?0:AnimationEvaluator.valueAt(track(),ClientProjectWorkspace.project().animation(),tick),v->{if(track()!=null)changeTrack(t->AnimationAuthoring.addOrReplaceKeyframe(t,tick,(float)v));}));value.active=track()!=null;
            button(right+8,y+120,60,"+ Key",()->{if(track()!=null)addKeyframe(trackId);});
            button(right+72,y+120,60,"− Key",()->{if(track()!=null)removeKeyframe(trackId);});
            button(right+136,y+120,60,"Speed",()->{if(track()!=null)cycleTrackSpeed(trackId);});
        }
        guideTop=workTop;guideWidth=right-24;
        timeline=addRenderableWidget(new LoomAnimationTimelineWidget(8,workTop+66,right-8,bottom-workTop-66,ClientProjectWorkspace::project,channel,()->trackId,()->tick,()->playing,this,false));
    }
    public void applyPreset(){
        try{
            var animation=ClientProjectWorkspace.project().animation();
            int period=Math.round(20/presetRate);
            int duration=Math.min(LoomAnimation.MAX_DURATION_TICKS,((animation.durationTicks()+period-1)/period)*period);
            var recipe=preset.create(layerId,channel,duration,presetRate);
            if(animation.tracks().size()>=LoomAnimation.MAX_TRACKS&&track()==null)throw new IllegalStateException("Delete a track before adding another");
            var current=track();
            if(current!=null){
                var replacement=new AnimationTrack(current.id(),layerId,channel,recipe.effect(),true,recipe.speed(),true,recipe.keyframes());
                ClientProjectWorkspace.apply(p->p.withAnimation(AnimationAuthoring.replaceTrack(AnimationAuthoring.changeDuration(p.animation(),duration),replacement)));trackId=replacement.id();
            }else{
                ClientProjectWorkspace.apply(p->{var tracks=new ArrayList<>(p.animation().tracks());tracks.add(recipe);return p.withAnimation(AnimationAuthoring.changeDuration(p.animation(),duration).withTracks(tracks));});trackId=recipe.id();
            }
            tick=0;cursor=0;playing=true;message=preset.label()+" applied to selected layer · "+presetRate+" cycles/sec · Save to keep changes";rebuildWidgets();
        }catch(IllegalArgumentException|IllegalStateException e){message=e.getMessage();}
    }

    private void button(int x,int y,int w,String label,Runnable action){addRenderableWidget(new LoomButton(x,y,w,22,Component.literal(label),action));}
    private void changeTrack(UnaryOperator<AnimationTrack> edit){AnimationTrack current=track();if(current!=null)ClientProjectWorkspace.apply(p->p.withAnimation(AnimationAuthoring.replaceTrack(p.animation(),edit.apply(current))));}
    @Override public void togglePlayback(){playing=!playing;cursor=tick;}
    @Override public void toggleTimelineLoop(){ClientProjectWorkspace.apply(p->p.withAnimation(p.animation().withLoop(!p.animation().loop())));}
    @Override public void changeDuration(int delta){ClientProjectWorkspace.apply(p->p.withAnimation(AnimationAuthoring.changeDuration(p.animation(),p.animation().durationTicks()+delta)));tick=Math.min(tick,ClientProjectWorkspace.project().animation().durationTicks());}
    @Override public void changePlaybackSpeed(float delta){ClientProjectWorkspace.apply(p->p.withAnimation(p.animation().withPlaybackSpeed(Math.max(.25F,Math.min(4F,p.animation().playbackSpeed()+delta)))));}
    @Override public void scrubTo(int tick){this.tick=tick;cursor=tick;playing=false;}
    @Override public void addTrack(){try{var p=ClientProjectWorkspace.apply(current->current.withAnimation(AnimationAuthoring.addTrack(current.animation(),layerId,channel,chosen)));trackId=p.animation().tracks().getLast().id();rebuildWidgets();}catch(IllegalStateException e){message=e.getMessage();}}
    @Override public void selectTrack(UUID id){trackId=id;var t=track();if(t!=null){layerId=t.layerId();chosen=t.effect();}refreshPending=true;}
    @Override public void toggleTrack(UUID id){trackId=id;changeTrack(t->t.withEnabled(!t.enabled()));}
    @Override public void cycleEffect(UUID id){trackId=id;changeTrack(t->t.withEffect(t.effect().next()));rebuildWidgets();}
    @Override public void addKeyframe(UUID id){trackId=id;changeTrack(t->AnimationAuthoring.addOrReplaceKeyframe(t,tick,AnimationEvaluator.valueAt(t,ClientProjectWorkspace.project().animation(),tick)));}
    @Override public void removeKeyframe(UUID id){trackId=id;AnimationTrack t=track();if(t!=null){int nearest=t.keyframes().stream().min(Comparator.comparingInt(k->Math.abs(k.tick()-tick))).orElseThrow().tick();changeTrack(a->AnimationAuthoring.removeKeyframe(a,nearest));}}
    @Override public void adjustKeyframeValue(UUID id,float delta){trackId=id;changeTrack(t->AnimationAuthoring.addOrReplaceKeyframe(t,tick,Math.max(0,Math.min(1,AnimationEvaluator.valueAt(t,ClientProjectWorkspace.project().animation(),tick)+delta))));}
    @Override public void cycleTrackSpeed(UUID id){trackId=id;changeTrack(t->t.withSpeed(t.speed()>=4?.25F:t.speed()+.25F));}
    @Override public void deleteTrack(UUID id){ClientProjectWorkspace.apply(p->p.withAnimation(AnimationAuthoring.removeTrack(p.animation(),id)));trackId=null;rebuildWidgets();}
    @Override public void tick(){if(refreshPending&&!ClientProjectWorkspace.session().isCompoundEditActive()){refreshPending=false;rebuildWidgets();}if(playing){var a=ClientProjectWorkspace.project().animation();cursor+=a.playbackSpeed();if(cursor>a.durationTicks()){if(a.loop())cursor%=a.durationTicks();else{cursor=a.durationTicks();playing=false;}}tick=(int)cursor;}}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if(e.hasControlDownWithQuirk()&&e.key()==90){ClientProjectWorkspace.undo();rebuildWidgets();return true;}if(e.hasControlDownWithQuirk()&&e.key()==89){ClientProjectWorkspace.redo();rebuildWidgets();return true;}return super.keyPressed(e);}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,channel.displayName()+" animation",LoomUiTheme.compact(width,height));LoomScreenChrome.panel(g,8,guideTop,width-204,guideTop+61);
        var f=minecraft.font;
        dev.loomstudios.client.ui.premium.PremiumControls.label(g,"1. Layer   2. Preset & rate   3. Apply & play   4. Save",16,guideTop+8,guideWidth,9,LoomUiTheme.ACCENT,false);
        var lines=f.split(Component.literal(preset.description()),guideWidth);
        for(int i=0;i<Math.min(2,lines.size());i++)g.drawString(f,lines.get(i),16,guideTop+24+i*11,LoomUiTheme.TEXT_MUTED,false);
        super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,font.plainSubstrByWidth(message,width-130),playing?"Playing":"Paused");}
    @Override public void removed(){if(timeline!=null)timeline.closeGesture();ClientProjectWorkspace.endCompoundEdit();super.removed();}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
