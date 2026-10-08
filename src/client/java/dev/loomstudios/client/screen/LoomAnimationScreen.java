package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.*;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.project.*;
import java.util.*;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
    private String message =
        "Choose a layer, pick an effect preset and press Apply & play. Save when finished.";
    private AnimationPreset preset=AnimationPreset.GLOW;
    private float presetRate=.5F;
    private LoomPlayerPreviewWidget preview;
    private boolean advanced,tryPreview;
    private LoomProject draftCache;
    private String draftKey;
    private int guideTop,guideWidth;
    private LoomButton presetButton,rateButton,effectButton;
    public LoomAnimationScreen(Screen parent,AnimationChannel channel,UUID layerId){super(Component.literal("Animation studio"));this.parent=parent;this.channel=channel;this.layerId=layerId;}
    private List<LoomLayer> layers(){return(channel==AnimationChannel.CAPE?ClientProjectWorkspace.project().cape():ClientProjectWorkspace.project().elytra()).layers();}
    private AnimationTrack track(){return ClientProjectWorkspace.project().animation().tracks().stream().filter(t->t.id().equals(trackId)).findFirst().orElse(null);}
    @Override protected void init(){
        int top=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+8,right=width-204,bottom=height-28;
        if(layerId==null||layers().stream().noneMatch(l->l.id().equals(layerId)))layerId=layers().getFirst().id();
        if(track()==null)trackId=ClientProjectWorkspace.project().animation().tracks().stream().filter(t->t.channel()==channel&&t.layerId().equals(layerId)).map(AnimationTrack::id).findFirst().orElse(null);
        addRenderableWidget(new LoomButton(8,top,54,20,Component.literal("Back"),this::onClose));
        LoomButton[] layerChoice = {null};
        layerChoice[0] = addRenderableWidget(new LoomButton(
            66, top, Math.max(150, right-152), 20,
            Component.literal("Layer: " + layers().stream().filter(l -> l.id().equals(layerId))
                .findFirst().orElseThrow().name()),
            () -> showChoices(layerChoice[0], "Choose animation layer",
                layers().stream().map(l -> new LoomChoicePopup.Option<>(
                    l.id(), l.name(), l.locked() ? "Locked layer" : "Animate this layer"))
                    .toList(),
                layerId,
                id -> { layerId = id; trackId = null; rebuildWidgets(); })));
        layerChoice[0].setIcon(LoomButton.Icon.DOWN);
        layerChoice[0].setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Choose the Cape or Elytra layer to animate. Existing keyframes stay intact.")));
        addRenderableWidget(new LoomButton(right-80,top,80,20,Component.literal("Save"),()->{try{ClientProjectWorkspace.save();message="Saved animation";}catch(java.io.IOException e){message="Save failed";}}));
        addRenderableWidget(new LoomButton(right+8,top,188,20,Component.literal("Undo / Ctrl+Z"),()->{ClientProjectWorkspace.undo();rebuildWidgets();}));
        int workTop=top+26;
        var view=preview==null?null:preview.viewState();
        int previewHeight=Math.max(40,Math.min(180,bottom-workTop-(advanced?184:124)));
        preview=addRenderableWidget(new LoomPlayerPreviewWidget(right+8,workTop,188,previewHeight,this::previewProject,channel==AnimationChannel.CAPE?LoomPlayerPreviewWidget.Mode.CAPE:LoomPlayerPreviewWidget.Mode.ELYTRA));
        preview.restoreViewState(view);preview.setTimelineTickSupplier(()->tick);
        int y=workTop+previewHeight+5;
        presetButton=button(right+8,y,188,"Preset: "+preset.label(),()->showChoices(presetButton,"Animation preset",Arrays.stream(AnimationPreset.values()).map(p->new LoomChoicePopup.Option<>(p,p.label(),p.description())).toList(),preset,p->{preset=p;rebuildWidgets();}));presetButton.setIcon(LoomButton.Icon.DOWN);
        rateButton=button(right+8,y+24,92,"Rate "+presetRate+" Hz",()->showChoices(rateButton,"Cycles per second",List.of(.25F,.5F,1F,2F).stream().map(r->new LoomChoicePopup.Option<>(r,r+" Hz",r==.25F?"One cycle every four seconds":r==.5F?"One cycle every two seconds":r==1F?"One cycle per second":"Two cycles per second")).toList(),presetRate,r->{presetRate=r;rebuildWidgets();}));rateButton.setIcon(LoomButton.Icon.DOWN);
        button(right+104,y+24,92,"Apply & play",this::applyPreset);
        button(right+8,y+48,92,advanced?"Simple mode":"Advanced",()->{
            advanced=!advanced;
            // Switching workspace mode must NEVER rewrite a keyframe or apply a draft.
            tryPreview=false;draftCache=null;
            message=advanced?"Advanced: edit selected keys or parameter curves":
                "Choose a visual effect and Try before Apply";
            rebuildWidgets();
        });
        if(advanced) button(right+104,y+48,92,"Delete track",()->{if(track()!=null)deleteTrack(trackId);});
        else {
            var tryButton=button(right+8,y+72,188,tryPreview?"Stop preview":"Try on 3D · no changes",
                ()->{tryPreview=!tryPreview;tick=0;cursor=0;playing=tryPreview;draftCache=null;rebuildWidgets();});
            tryButton.setIcon(tryPreview?LoomButton.Icon.PAUSE:LoomButton.Icon.PLAY);
            tryButton.setPrimary(tryPreview);
        }
        if(advanced){
            effectButton=button(right+8,y+72,188,"Effect: "+(track()==null?chosen:track().effect()).displayName(),()->showChoices(effectButton,"Animation effect",Arrays.stream(AnimationEffectType.values()).map(e->new LoomChoicePopup.Option<>(e,e.displayName(),e.description())).toList(),track()==null?chosen:track().effect(),e->{chosen=e;if(track()!=null)changeTrack(t->t.withEffect(e));rebuildWidgets();}));effectButton.setIcon(LoomButton.Icon.DOWN);
            LoomSlider value=addRenderableWidget(new LoomSlider(right+8,y+96,188,(track()==null?chosen:track().effect()).valueLabel(),()->track()==null?0:AnimationEvaluator.valueAt(track(),ClientProjectWorkspace.project().animation(),tick),v->{if(track()!=null)changeTrack(t->AnimationAuthoring.addOrReplaceKeyframe(t,tick,(float)v));}));value.active=track()!=null;value.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal((track()==null?chosen:track().effect()).description()+". Editing changes the key at the current timeline position.")));
            button(right+8,y+120,60,"+ Key",()->{if(track()!=null)addKeyframe(trackId);});
            button(right+72,y+120,60,"- Key",()->{if(track()!=null)removeKeyframe(trackId);});
            button(right+136,y+120,60,"Speed",()->{if(track()!=null)cycleTrackSpeed(trackId);});
            var curveButton = button(right+8,y+144,188,
          "Parameters & curves",()->{if(track()!=null)minecraft.setScreen(new LoomParameterAnimationScreen(this, channel,trackId,tick));});
        curveButton.active = track()!=null;
        curveButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(
            track()==null?"Apply a preset or add a track first":"Animate separate properties and edit easing with a live cosmetic preview")));
        }
        guideTop=workTop;guideWidth=right-24;
        timeline=null;
        if(advanced){
            timeline=addRenderableWidget(new LoomAnimationTimelineWidget(8,workTop+66,right-8,
                bottom-workTop-66,ClientProjectWorkspace::project,channel,()->trackId,
                ()->tick,()->playing,this,false));
        } else {
            addRenderableWidget(new LoomAnimationPresetGallery(8,workTop+66,right-16,
                bottom-workTop-68,()->preset,chosen->{
                    preset=chosen;draftCache=null;
                    message=chosen.label()+": "+chosen.description()+" · Try or Apply";
                    rebuildWidgets();
                }));
        }
    }
    /**
     * An uncommitted visual preview is built from a project snapshot. It never
     * enters the editing session, undo history, or multiplayer/equip state.
     */
    private LoomProject previewProject(){
        LoomProject source=ClientProjectWorkspace.project();
        if(!tryPreview || advanced)return source;
        String key=ClientProjectWorkspace.revision()+":"+layerId+":"+trackId+":"+preset+
            ":"+presetRate+":"+channel;
        if(draftCache!=null&&key.equals(draftKey))return draftCache;
        try {
            draftCache=AnimationPresetDraft.compose(source,layerId,channel,trackId,preset,presetRate);
            draftKey=key;
            return draftCache;
        } catch(IllegalArgumentException|IllegalStateException ex) {
            message=ex.getMessage();
            tryPreview=false;draftCache=null;draftKey=null;
            return source;
        }
    }

    public void startPreview(){
        tryPreview=true;draftCache=null;
        playing=true;tick=0;cursor=0;rebuildWidgets();
    }

    public void applyPreset(){
        try{
            UUID previous=trackId;
            var next=ClientProjectWorkspace.apply(p->
                AnimationPresetDraft.compose(p,layerId,channel,previous,preset,presetRate));
            trackId=next.animation().tracks().stream()
                .filter(t->t.channel()==channel&&t.layerId().equals(layerId))
                .reduce((a,b)->b).orElseThrow().id();
            tryPreview=false;draftCache=null;draftKey=null;
            tick=0;cursor=0;playing=true;
            message=preset.label()+" applied · Save when ready · Ctrl+Z to undo";
            rebuildWidgets();
        }catch(IllegalArgumentException|IllegalStateException e){message=e.getMessage();}
    }

    private LoomButton button(int x,int y,int w,String label,Runnable action){
        var button=addRenderableWidget(new LoomButton(x,y,w,22,Component.literal(label),action));
        String hint=label.startsWith("Preset:")?"Browse ready-made animation effects":label.startsWith("Rate ")?"How many times the effect repeats each second":label.equals("Apply & play")?"Apply this preset to the chosen layer and start preview playback; Undo restores the previous track":label.equals("Advanced")?"Show timeline key and parameter controls":label.equals("Simple mode")?"Return to beginner-friendly animation presets":label;
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(hint)));return button;
    }
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
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if(hasChoices())return super.keyPressed(e);if(e.hasControlDownWithQuirk()&&e.key()==90){ClientProjectWorkspace.undo();rebuildWidgets();return true;}if(e.hasControlDownWithQuirk()&&e.key()==89){ClientProjectWorkspace.redo();rebuildWidgets();return true;}if(e.key()==32){togglePlayback();rebuildWidgets();return true;}return super.keyPressed(e);}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,channel.displayName()+" animation",LoomUiTheme.compact(width,height));LoomScreenChrome.panel(g,8,guideTop,width-204,guideTop+61);
        var f=minecraft.font;
        dev.loomstudios.client.ui.premium.PremiumControls.label(g,advanced ? "Advanced: select a track, edit keys, open Parameters & curves" : "1. Choose an effect   2. Try it in 3D   3. Apply & Save",16,guideTop+8,guideWidth,9,LoomUiTheme.ACCENT,false);
        var lines=new ArrayList<String>();String line="";
        for(String word:preset.description().split(" ")){if(!line.isEmpty()&&f.width(line+" "+word)>guideWidth){lines.add(line);line=word;}else line=line.isEmpty()?word:line+" "+word;}
        if(!line.isEmpty())lines.add(line);
        for(int i=0;i<Math.min(2,lines.size());i++)dev.loomstudios.client.ui.premium.PremiumControls.label(g,lines.get(i),16,guideTop+24+i*12,guideWidth,9,LoomUiTheme.TEXT_MUTED,false);
        super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,font.plainSubstrByWidth(message,width-130),
        tryPreview?"UNSAVED PREVIEW":playing?"Playing":"Paused");}
    @Override public void removed(){if(timeline!=null)timeline.closeGesture();ClientProjectWorkspace.endCompoundEdit();super.removed();}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
