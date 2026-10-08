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
    private final LinkedHashSet<UUID> selectedLayerIds=new LinkedHashSet<>();
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
    private boolean advanced,tryPreview,previewLoop;
    private LoomProject draftCache;
    private String draftKey;
    private int guideTop,guideWidth,studioRight;
    private LoomButton presetButton,rateButton,effectButton;
    public LoomAnimationScreen(Screen parent,AnimationChannel channel,UUID layerId){super(Component.literal("Animation studio"));this.parent=parent;this.channel=channel;this.layerId=layerId;if(layerId!=null)selectedLayerIds.add(layerId);this.previewLoop=ClientProjectWorkspace.project().animation().loop();}
    private LoomCanvas canvas(){return channel==AnimationChannel.CAPE
        ? ClientProjectWorkspace.project().cape() : ClientProjectWorkspace.project().elytra();}
    private List<LoomLayer> layers(){return canvas().layers();}
    private List<UUID> targetIds(){return List.copyOf(selectedLayerIds);}
    private boolean batch(){return selectedLayerIds.size()>1;}
    private void selectAnimationLayer(UUID id,net.minecraft.client.input.MouseButtonEvent event){
        if(event.hasControlDown()){
            if(!selectedLayerIds.add(id))selectedLayerIds.remove(id);
        }else{
            selectedLayerIds.clear();selectedLayerIds.add(id);
        }
        if(selectedLayerIds.contains(id)){layerId=id;trackId=null;}
        if(!selectedLayerIds.isEmpty()&&!selectedLayerIds.contains(layerId))
            layerId=selectedLayerIds.iterator().next();
        draftCache=null;draftKey=null;
        message=selectedLayerIds.size()+" layers selected · Try or Apply";
        rebuildWidgets();
    }
    private AnimationTrack track(){return ClientProjectWorkspace.project().animation().tracks().stream().filter(t->t.id().equals(trackId)).findFirst().orElse(null);}
    @Override protected void init(){
        int top=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+8;
        int inspectorWidth=width>=1400?370:width>=950?275:204;
        int right=width-inspectorWidth,bottom=height-28;
        studioRight=right;
        int sideW=inspectorWidth-16,halfW=(sideW-4)/2;
        if(layerId==null||layers().stream().noneMatch(l->l.id().equals(layerId)))layerId=layers().getFirst().id();
        selectedLayerIds.retainAll(layers().stream().map(LoomLayer::id).toList());
        if(selectedLayerIds.isEmpty())selectedLayerIds.add(layerId);
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
                id -> { layerId = id; trackId = null; selectedLayerIds.clear(); selectedLayerIds.add(id); rebuildWidgets(); })));
        layerChoice[0].setIcon(LoomButton.Icon.DOWN);
        layerChoice[0].setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Choose the Cape or Elytra layer to animate. Existing keyframes stay intact.")));
        addRenderableWidget(new LoomButton(right-80,top,80,20,Component.literal("Save"),()->{try{ClientProjectWorkspace.save();message="Saved animation";}catch(java.io.IOException e){message="Save failed";}}));
        addRenderableWidget(new LoomButton(right+8,top,sideW,20,Component.literal("Undo / Ctrl+Z"),()->{ClientProjectWorkspace.undo();rebuildWidgets();}));
        int workTop=top+26;
        var view=preview==null?null:preview.viewState();
        int previewHeight=Math.max(40,Math.min(width>=950?270:180,
            bottom-workTop-(advanced?184:124)));
        preview=addRenderableWidget(new LoomPlayerPreviewWidget(right+8,workTop,sideW,previewHeight,this::previewProject,channel==AnimationChannel.CAPE?LoomPlayerPreviewWidget.Mode.CAPE:LoomPlayerPreviewWidget.Mode.ELYTRA));
        preview.restoreViewState(view);preview.setTimelineTickSupplier(()->tick);
        int y=workTop+previewHeight+5;
        presetButton=button(right+8,y,sideW,"Preset: "+preset.label(),()->showChoices(presetButton,"Animation preset",Arrays.stream(AnimationPreset.values()).map(p->new LoomChoicePopup.Option<>(p,p.label(),p.description())).toList(),preset,p->{preset=p;rebuildWidgets();}));presetButton.setIcon(LoomButton.Icon.DOWN);
        rateButton=button(right+8,y+24,halfW,"Rate "+presetRate+" Hz",()->showChoices(rateButton,"Cycles per second",List.of(.25F,.5F,1F,2F).stream().map(r->new LoomChoicePopup.Option<>(r,r+" Hz",r==.25F?"One cycle every four seconds":r==.5F?"One cycle every two seconds":r==1F?"One cycle per second":"Two cycles per second")).toList(),presetRate,r->{presetRate=r;rebuildWidgets();}));rateButton.setIcon(LoomButton.Icon.DOWN);
        boolean customLanes=track()!=null&&!track().lanes().isEmpty()&&!batch();
        var applyButton=button(right+12+halfW,y+24,sideW-halfW-4,
            batch()?"Apply to "+selectedLayerIds.size():"Apply & play",this::applyPreset);
        applyButton.active=!customLanes&&!selectedLayerIds.isEmpty();
        if(customLanes)applyButton.setTooltip(
            net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("Customized parameter lanes: edit this track in Advanced.")));
        button(right+8,y+48,halfW,advanced?"Simple mode":"Advanced",()->{
            advanced=!advanced;
            if(!advanced)previewLoop=ClientProjectWorkspace.project().animation().loop();
            // Switching workspace mode must NEVER rewrite a keyframe or apply a draft.
            tryPreview=false;draftCache=null;
            message=advanced?"Advanced: edit selected keys or parameter curves":
                "Choose a visual effect and Try before Apply";
            rebuildWidgets();
        });
        if(advanced) button(right+12+halfW,y+48,sideW-halfW-4,"Delete track",()->{if(track()!=null)deleteTrack(trackId);});
        else {
            var tryButton=button(right+8,y+72,sideW,tryPreview?"Stop preview":"Try on 3D · no changes",
                ()->{tryPreview=!tryPreview;tick=0;cursor=0;playing=tryPreview;draftCache=null;
                    message=tryPreview?"Trying "+preset.label()+" · nothing added until Apply":
                        "Try stopped · project is unchanged";
                    rebuildWidgets();});
            var loopButton=button(right+8,y+96,sideW,
                previewLoop?"Loop: On · repeats":"Loop: Off · play once",
                ()->{previewLoop=!previewLoop;draftCache=null;
                    message="Loop "+(previewLoop?"enabled":"disabled")+" for Try · Apply to save";
                    rebuildWidgets();});
            loopButton.setIcon(LoomButton.Icon.LOOP);
            loopButton.setSelected(previewLoop);
            loopButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("Controls this preview. Only Apply saves the Loop setting.")));
            tryButton.setIcon(tryPreview?LoomButton.Icon.PAUSE:LoomButton.Icon.PLAY);
            tryButton.setPrimary(tryPreview);
            tryButton.active=!customLanes&&!selectedLayerIds.isEmpty();
            if(customLanes)tryButton.setTooltip(
                net.minecraft.client.gui.components.Tooltip.create(Component.literal(
                    "Customized animation: use Advanced to edit this track safely.")));
        }
        if(advanced){
            effectButton=button(right+8,y+72,sideW,"Effect: "+(track()==null?chosen:track().effect()).displayName(),()->showChoices(effectButton,"Animation effect",Arrays.stream(AnimationEffectType.values()).map(e->new LoomChoicePopup.Option<>(e,e.displayName(),e.description())).toList(),track()==null?chosen:track().effect(),e->{chosen=e;if(track()!=null)changeTrack(t->t.withEffect(e));rebuildWidgets();}));effectButton.setIcon(LoomButton.Icon.DOWN);
            LoomSlider value=addRenderableWidget(new LoomSlider(right+8,y+96,sideW,(track()==null?chosen:track().effect()).valueLabel(),()->track()==null?0:AnimationEvaluator.valueAt(track(),ClientProjectWorkspace.project().animation(),tick),v->{if(track()!=null)changeTrack(t->AnimationAuthoring.addOrReplaceKeyframe(t,tick,(float)v));}));value.active=track()!=null;value.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal((track()==null?chosen:track().effect()).description()+". Editing changes the key at the current timeline position.")));
            button(right+8,y+120,(sideW-8)/3,"+ Key",()->{if(track()!=null)addKeyframe(trackId);});
            button(right+12+(sideW-8)/3,y+120,(sideW-8)/3,"- Key",()->{if(track()!=null)removeKeyframe(trackId);});
            button(right+16+2*((sideW-8)/3),y+120,sideW-8-2*((sideW-8)/3),"Speed",()->{if(track()!=null)cycleTrackSpeed(trackId);});
            var curveButton = button(right+8,y+144,sideW,
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
            int layerW=Math.max(130,Math.min(230,(right-28)/3));
            addRenderableWidget(new LoomLayerListWidget(8,workTop+66,layerW,
                bottom-workTop-68,this::canvas,()->layerId,
                id->{layerId=id;},id->{
                    var l=layers().stream().filter(a->a.id().equals(id)).findFirst().orElseThrow();
                    ClientProjectWorkspace.apply(p->LayerBatch.apply(p,
                        channel==AnimationChannel.ELYTRA,Set.of(id),
                        l.visible()?LayerBatch.Action.HIDE:LayerBatch.Action.SHOW));
                },id->{
                    var l=layers().stream().filter(a->a.id().equals(id)).findFirst().orElseThrow();
                    ClientProjectWorkspace.apply(p->LayerBatch.apply(p,
                        channel==AnimationChannel.ELYTRA,Set.of(id),
                        l.locked()?LayerBatch.Action.UNLOCK:LayerBatch.Action.LOCK));
                }).setElytraThumbnails(channel==AnimationChannel.ELYTRA)
                    .setMultiSelection(selectedLayerIds,this::selectAnimationLayer));
            addRenderableWidget(new LoomAnimationPresetGallery(14+layerW,workTop+66,
                right-layerW-22,bottom-workTop-68,()->preset,chosen->{
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
        String key=ClientProjectWorkspace.revision()+":"+targetIds()+":"+layerId+":"+trackId+":"+preset+
            ":"+presetRate+":"+channel+":"+previewLoop;
        if(draftCache!=null&&key.equals(draftKey))return draftCache;
        try {
            draftCache=batch()
                ? AnimationPresetBatch.compose(source,channel,targetIds(),preset,presetRate,previewLoop)
                : AnimationPresetDraft.compose(source,layerId,channel,trackId,preset,presetRate,previewLoop);
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
        playing=true;tick=0;cursor=0;
        message="Trying "+preset.label()+" · Apply to keep this effect";
        rebuildWidgets();
    }

    public void applyPreset(){
        try{
            UUID previous=trackId;
            var next=ClientProjectWorkspace.apply(p->batch()
                ? AnimationPresetBatch.compose(p,channel,targetIds(),preset,presetRate,previewLoop)
                : AnimationPresetDraft.compose(p,layerId,channel,previous,preset,presetRate,previewLoop));
            trackId=!batch()&&previous!=null?previous:next.animation().tracks().stream()
                .filter(t->t.channel()==channel&&t.layerId().equals(layerId))
                .reduce((a,b)->b).orElseThrow().id();
            tryPreview=false;draftCache=null;draftKey=null;
            tick=0;cursor=0;playing=true;
            message=preset.label()+" applied to "+selectedLayerIds.size()+
                " layer(s) · Save when ready · Ctrl+Z to undo";
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
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,channel.displayName()+" animation",LoomUiTheme.compact(width,height));LoomScreenChrome.panel(g,8,guideTop,studioRight,guideTop+61);
        var f=minecraft.font;
        dev.loomstudios.client.ui.premium.PremiumControls.label(g,advanced ? "Advanced: select a track, edit keys, open Parameters & curves"
            : track()!=null&&!track().lanes().isEmpty()
                ? "Customized animation · switch to Advanced to edit"
                : "Ctrl-click layers ("+selectedLayerIds.size()+" selected) · Try · Apply",
            16,guideTop+8,guideWidth,9,LoomUiTheme.ACCENT,false);
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
