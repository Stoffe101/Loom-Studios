package dev.loomstudios.client.debug;

import dev.loomstudios.client.importing.PngImportAdapter;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.LocalProjectLibrary;
import dev.loomstudios.client.screen.CapeEditorScreen;
import dev.loomstudios.client.screen.ElytraEditorScreen;
import dev.loomstudios.client.screen.LoomCodesScreen;
import dev.loomstudios.client.screen.LoomHomeScreen;
import dev.loomstudios.client.screen.SmartImportScreen;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.PixelSelection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

/** Opt-in development capture runner; never runs in packaged or ordinary clients. */
public final class LoomUiCapture {
    private static final boolean ENABLED = FabricLoader.getInstance().isDevelopmentEnvironment()
            && Boolean.getBoolean("loom.uiCapture");
    private static final int[][] PROFILES = {{1920,1080,2}, {1920,1080,3}, {3440,1440,2}, {3440,1440,3}};
    private static int ticks, stage = -2, wait;
    private static boolean pending, prepared, fixturesPrepared, importHandleChecked;
    private static Path fixturePath;
    private static LoomProject fixtureProject;
    private static boolean workflowsVerified;
    private static String networkFixtureHash;
    private static int networkWaitTicks;
    private static java.util.concurrent.CompletableFuture<Boolean> networkCheck;
    private static final java.util.Set<Integer> cacheProbes=new java.util.HashSet<>();
    private LoomUiCapture() {}
    public static void tick(Minecraft client) {
        if (!ENABLED || pending) return;
        try {
            if (++ticks > 12000) throw new IllegalStateException("Capture timed out");
            if (stage == -2 && (client.screen instanceof TitleScreen || (client.screen != null && client.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")))) {
                stage = -1;
                CreateWorldScreen.testWorld(client, () -> {});
                return;
            }
            if (stage == -1 && client.screen instanceof CreateWorldScreen screen) {
                Method create = CreateWorldScreen.class.getDeclaredMethod("onCreate");
                create.setAccessible(true); create.invoke(screen); stage = Integer.getInteger("loom.uiCaptureStart", 0); wait = 80; return;
            }
            if (client.player == null || client.level == null || wait-- > 0) return;
            if (stage >= Integer.getInteger("loom.uiCaptureEnd", 326)) {
                if(!workflowsVerified) {
                    verifyWorkflows(client); verifyInputAndPreview(client); verifyLibraryWorkflows(client); verifySafetyWorkflows(client); verifyPolishWorkflows(client); verifyUsability(client); LoomAuthoringVerification.verify(client);
          LoomCreativeVerification.verify(client); verifyAuthoringPayload(client);
                    workflowsVerified=true;
                }
                if(!verifyLiveAuthoringNetwork(client))return;
                pending=true;System.out.println("LOOM_UI_CAPTURE COMPLETE");client.stop();return;
            }
            if (!fixturesPrepared) {
                String[] names = {"Moonlit", "Void Walker", "Alpine", "Crimson Flight"};
                for(int i=3;i>=0;i--) {
                    var project = LoomCaptureFixtures.moon(names[i],i);
                    var path = LocalProjectLibrary.save(project);
                    if(i==0) { fixturePath=path; fixtureProject=project; }
                }
                ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
                fixturesPrepared=true;
            }
            client.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            client.getToastManager().clear();
            int profile = Math.min(stage / 2, 3);
            int[] p = PROFILES[profile];
            if (!prepared && stage != 20) {
                int[] target =
            stage >= 310
                ? PROFILES[(stage - 310) / 4]
                : stage >= 278
                ? PROFILES[(stage - 278) / 8]
                : stage>=238?PROFILES[(stage-238)/10]:stage>=218?PROFILES[(stage-218)/5]:stage>=190?PROFILES[(stage-190)/7]:stage>=182?PROFILES[(stage-182)/2]:stage == 21 ? new int[]{854,480,2} : stage>=176?new int[]{1904,960,3}:stage>=128?PROFILES[(stage-128)/12]:stage>=124?new int[]{1904,960,3}:stage>=100?PROFILES[(stage-100)/6]:stage >= 96 ? new int[]{1904,960,3} : stage >= 68 ? PROFILES[(stage-68)/7] : stage >= 42 ? new int[]{stage>=57?1904:1920,stage>=57?960:1000,3} : stage >= 22 ? PROFILES[(stage-22)/5] : stage < 8 ? p : PROFILES[1];
                client.options.guiScale().set(target[2]);
                org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.getWindow().handle(), target[0], target[1]);
                client.resizeDisplay(); prepared = true; wait = 10; return;
            }
            if (stage != 20) {
                client.options.guiScale().set(
                stage >= 310
                    ? PROFILES[(stage - 310) / 4][2]
                    : stage >= 278
                    ? PROFILES[(stage - 278) / 8][2]
                    :stage>=238?PROFILES[(stage-238)/10][2]:stage>=218?PROFILES[(stage-218)/5][2]:stage>=190?PROFILES[(stage-190)/7][2]:stage>=182?PROFILES[(stage-182)/2][2]:stage == 21 ? 2 : stage>=176?3:stage>=128?PROFILES[(stage-128)/12][2]:stage>=124?3:stage>=100?PROFILES[(stage-100)/6][2]:stage >= 96 ? 3 : stage >= 68 ? PROFILES[(stage-68)/7][2] : stage >= 42 ? 3 : stage >= 22 ? PROFILES[(stage-22)/5][2] : stage < 8 ? p[2] : 3);
                client.resizeDisplay();
            }
            if (stage < 8) {
                ClientProjectWorkspace.ensure(client.player.getUUID());
                Screen screen = stage % 2 == 0 ? new CapeEditorScreen(new LoomHomeScreen()) : new ElytraEditorScreen(new LoomHomeScreen());
                client.setScreen(screen);
                if (screen instanceof CapeEditorScreen) {
                    set(screen, "tool", enumValue(screen, "tool", "SELECT"));
                    set(screen, "selection", PixelSelection.between(0, 0, 3, 5));
                    call(screen, "updateButtonStates");
                } else { call(screen, "convertCapeToElytra"); }
            } else if (stage < 12) {

                if (stage == 8 || stage == 9) {
                    CapeEditorScreen screen = new CapeEditorScreen(new LoomHomeScreen()); client.setScreen(screen);
                    set(screen,"inspectorTab",enumValue(screen,"inspectorTab",stage == 8 ? "COLOR" : "PROPERTIES"));
                    call(screen,"updateInspectorVisibility");
                } else {
                    ElytraEditorScreen screen = new ElytraEditorScreen(new LoomHomeScreen()); client.setScreen(screen);
                    call(screen,"addAnimationTrack");
                    set(screen,"inspectorTab",enumValue(screen,"inspectorTab","ANIMATION"));
                    if(stage == 11) set(screen,"animationPage",1);
                    call(screen,"updateInspectorVisibility");
                }
            }
            if (stage == 17) ClientProjectWorkspace.apply(project -> dev.loomstudios.project.ProjectResizer.resizeCape(project,dev.loomstudios.project.CanvasResolution.STANDARD));
            if (stage >= 12 && stage < 19) {
                if (stage <= 14 || stage == 17) {
                    CapeEditorScreen screen = new CapeEditorScreen(new LoomHomeScreen()); client.setScreen(screen);
                    if (stage <= 14) {
                        call(screen,"addGradientLayer");
                        set(screen,"inspectorTab",enumValue(screen,"inspectorTab","PROPERTIES"));
                        set(screen,"propertyPage",stage - 11);
                    } else { for (int i=0;i<14;i++) call(screen,"addLayer"); }
                    call(screen,"updateInspectorVisibility");
                } else {
                    ElytraEditorScreen screen = new ElytraEditorScreen(new LoomHomeScreen()); client.setScreen(screen);
                    if (stage == 18) { for(int i=0;i<9;i++) call(screen,"addAnimationTrack"); }
                    set(screen,"inspectorTab",enumValue(screen,"inspectorTab",stage == 15 ? "PROPERTIES" : stage == 16 ? "COLOR" : "ANIMATION"));
                    call(screen,"updateInspectorVisibility");
                }
            }
            if (stage >= 22 && stage < 42) {
                ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
                int view=(stage-22)%5;
                if(view==0) client.setScreen(new LoomHomeScreen());
                else if(view<=2) {
                    var screen=new LoomCodesScreen(new LoomHomeScreen(),fixtureProject); client.setScreen(screen);
                    if(view==2) { set(screen,"workspace",enumValue(screen,"workspace","IMPORT")); call(screen,"updateWorkspaceVisibility"); }
                } else {
                    var screen=new SmartImportScreen(new LoomHomeScreen(),dev.loomstudios.project.CapeUvRegion.OUTSIDE);
                    var source=LoomCaptureFixtures.source(fixtureProject);
                    set(screen,"loaded",new PngImportAdapter.LoadedImage(Path.of("capture-moon.png"),source,source));
                    set(screen,"status","Moon artwork ready to import");
                    if(view==4) set(screen,"panelTab",enumValue(screen,"panelTab","PROCESSING"));
                    client.setScreen(screen);
                }
            }
            if (stage == 19) {
                ClientProjectWorkspace.apply(project -> dev.loomstudios.project.ProjectResizer.resizeCape(project,dev.loomstudios.project.CanvasResolution.STANDARD));
                CapeEditorScreen screen = new CapeEditorScreen(new LoomHomeScreen()); client.setScreen(screen);
                set(screen,"tool",enumValue(screen,"tool","SELECT")); call(screen,"updateButtonStates");
                var canvas = (dev.loomstudios.client.ui.LoomCapeFaceWidget)field(screen,"canvasWidget").get(screen);
                canvas.restoreViewState(new dev.loomstudios.client.ui.LoomCapeFaceWidget.ViewState(3,0,0,true));
                var geometry = canvas.getClass().getDeclaredMethod("geometry",dev.loomstudios.project.CapeUvRegion.class,int.class);
                geometry.setAccessible(true);
                var transform = (dev.loomstudios.ui.CanvasViewportTransform)geometry.invoke(canvas,dev.loomstudios.project.CapeUvRegion.OUTSIDE,1);
                var event = new net.minecraft.client.input.MouseButtonEvent(transform.screenX(5)+transform.pixelScale()/2.0,transform.screenY(8)+transform.pixelScale()/2.0,new net.minecraft.client.input.MouseButtonInfo(0,0));
                canvas.mouseClicked(event,false); canvas.mouseDragged(event,0,0);
            } else if (stage == 20) {
                var canvas = (dev.loomstudios.client.ui.LoomCapeFaceWidget)field(client.screen,"canvasWidget").get(client.screen);
                canvas.mouseReleased(new net.minecraft.client.input.MouseButtonEvent(0,0,new net.minecraft.client.input.MouseButtonInfo(0,0)));
                var selection=(PixelSelection)field(client.screen,"selection").get(client.screen);
                if(selection == null || selection.minX()!=5 || selection.minY()!=8 || selection.maxX()!=5 || selection.maxY()!=8)
                    throw new IllegalStateException("Single-pixel selection changed on release");
            }
            if (stage == 21) client.setScreen(new CapeEditorScreen(new LoomHomeScreen()));
            if(stage>=42 && stage<68) {
                ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
                if(stage==55 || stage==56) ClientProjectWorkspace.apply(old -> dev.loomstudios.project.LoomProjectFactory.blank("Transparent",1L));
                if(stage==46) client.setScreen(new LoomHomeScreen());
                else if(stage==47) client.setScreen(new LoomCodesScreen(new LoomHomeScreen(),fixtureProject));
                else if(stage==48) {
                    var screen=new SmartImportScreen(new LoomHomeScreen(),dev.loomstudios.project.CapeUvRegion.OUTSIDE);
                    var source=LoomCaptureFixtures.source(fixtureProject);
                    set(screen,"loaded",new PngImportAdapter.LoadedImage(Path.of("capture-moon.png"),source,source)); client.setScreen(screen);
                } else {
                    boolean wing=stage==43||stage==45||stage==50||stage==54||stage==56||stage==58||stage==60;
                    Screen screen=wing?new ElytraEditorScreen(new LoomHomeScreen()):new CapeEditorScreen(new LoomHomeScreen());client.setScreen(screen);
                    if((boolean)field(screen,"workspaceTooSmall").get(screen))throw new IllegalStateException("Windowed GUI3 rejected");
                    if(stage==44||stage==58) {set(screen,"inspectorTab",enumValue(screen,"inspectorTab","PROPERTIES"));call(screen,"updateInspectorVisibility");}
                    if(stage==45){call(screen,"addAnimationTrack");set(screen,"inspectorTab",enumValue(screen,"inspectorTab","ANIMATION"));call(screen,"updateInspectorVisibility");}
                    if(stage==49||stage==50){set(screen,"inspectorTab",enumValue(screen,"inspectorTab","COLOR"));call(screen,"updateInspectorVisibility");call(screen,wing?"toggleSwatches":"togglePaletteWindow");}
                    if(stage==51||stage==52||stage==61||stage==62) {
                        set(screen,"tool",enumValue(screen,"tool","CIRCLE")); if(stage>=61)set(screen,"rectangleFilled",true);call(screen,"updateButtonStates");call(screen,"updateContextVisibility");
                        var canvas=(dev.loomstudios.client.ui.LoomCapeFaceWidget)field(screen,"canvasWidget").get(screen);
                        var geometry=canvas.getClass().getDeclaredMethod("geometry",dev.loomstudios.project.CapeUvRegion.class,int.class);geometry.setAccessible(true);
                        int scale=dev.loomstudios.project.CanvasResolution.fromCanvas(ClientProjectWorkspace.project().cape()).scale();
                        var t=(dev.loomstudios.ui.CanvasViewportTransform)geometry.invoke(canvas,dev.loomstudios.project.CapeUvRegion.OUTSIDE,scale);
                        var start=mouse(t.screenX(2)+t.pixelScale()/2.0,t.screenY(2)+t.pixelScale()/2.0,0);
                        var end=mouse(t.screenX(7)+t.pixelScale()/2.0,t.screenY(12)+t.pixelScale()/2.0,0);
                        screen.mouseClicked(start,false);screen.mouseDragged(end,end.x()-start.x(),end.y()-start.y());
                        if(stage==52||stage==62) screen.mouseReleased(end);
                    }
                    if(stage==53||stage==54) verifyPan(screen);
                    if(stage==59||stage==60) {
                        var preview=(AbstractWidget)field(screen,"previewWidget").get(screen);
                        screen.mouseClicked(mouse(preview.getRight()-12,preview.getY()+9,0),false);
                        if(!(client.screen instanceof dev.loomstudios.client.screen.LoomPlayerPreviewScreen))throw new IllegalStateException("Expand preview unavailable");
                    }
                }
            }
            if(stage>=63 && stage<68) {
                Screen screen;
                if(stage==63)screen=new CapeEditorScreen(new LoomHomeScreen());
                else if(stage==64)screen=new ElytraEditorScreen(new LoomHomeScreen());
                else if(stage==65)screen=new LoomHomeScreen();
                else if(stage==67)screen=new LoomCodesScreen(new LoomHomeScreen(),fixtureProject);
                else {
                    var imported=new SmartImportScreen(new LoomHomeScreen(),dev.loomstudios.project.CapeUvRegion.OUTSIDE);
                    var source=LoomCaptureFixtures.source(fixtureProject);
                    set(imported,"loaded",new PngImportAdapter.LoadedImage(Path.of("capture-moon.png"),source,source));screen=imported;
                }
                client.setScreen(screen);verifyPreviewPan(screen);
            }
            if(stage>=68&&stage<100) prepareMilestone(client);
            if(stage>=100&&stage<128) prepareSafety(client);
            if(stage>=128 && stage<182) preparePolish(client);
            if(stage>=182&&stage<190) {
                client.options.guiScale().set(PROFILES[(stage-182)/2][2]);client.resizeDisplay();
                client.setScreen(new dev.loomstudios.client.screen.LoomPremiumPrototypeScreen(stage%2==0));
            }
            if(stage>=310)prepareAssetLibrary(client,(stage-310)%4);
             else if(stage>= 278) prepareCreative(client);
      else if (stage >=238)prepareAuthoring(client);else if(stage>=218)prepareChoices(client);else if(stage>=190)prepareUsability(client);
            client.screen.setFocused(null);
            org.lwjgl.glfw.GLFW.glfwSetCursorPos(client.getWindow().handle(),2,2);
            if(stage>=128&&stage<176&&(stage-128)%12==0){double scale=client.getWindow().getGuiScale();org.lwjgl.glfw.GLFW.glfwSetCursorPos(client.getWindow().handle(),(field(client.screen,"menuX").getInt(client.screen)+60)*scale,(field(client.screen,"menuY").getInt(client.screen)+11)*scale);}
            pending = true;
            int captureStage = stage;
            // Allow several complete render frames after window resize and widget initialization.
            new Thread(() -> {
                try { Thread.sleep(1800); client.execute(() -> capture(client,captureStage)); }
                catch (Exception ex) { ex.printStackTrace(); client.execute(client::stop); }
            }, "loom-ui-capture").start();
        } catch (Exception ex) { ex.printStackTrace(); client.stop(); }
    }
    private static void capture(Minecraft client, int index) {
        try {
            if(index>=182)System.out.println("LOOM_PREMIUM_CPU "+index+" "+dev.loomstudios.client.ui.premium.PremiumPaint.metrics());
            if((index==8||index>=22&&index<42&&(index-22)%5==0||index>=182&&index<190&&index%2==0)&&cacheProbes.add(index)) {
                long uploads=dev.loomstudios.client.ui.LoomUiTextureCache.uploads();
                long paints=dev.loomstudios.client.ui.premium.PremiumGuiRenderer.paints();
                long submitted=dev.loomstudios.client.ui.premium.PremiumGuiRenderer.submittedFrames();
                new Thread(()-> {
                    try {
                        Thread.sleep(400);
                        client.execute(()-> {
                            if(uploads!=dev.loomstudios.client.ui.LoomUiTextureCache.uploads()
                                    ||dev.loomstudios.client.ui.LoomUiTextureCache.size()>32
                                    ||paints!=dev.loomstudios.client.ui.premium.PremiumGuiRenderer.paints()
                                    ||submitted==dev.loomstudios.client.ui.premium.PremiumGuiRenderer.submittedFrames()) {
                                System.err.println("LOOM_UI_TEXTURE_CACHE_REUSE FAIL");client.stop();return;
                            }
                            System.out.println("LOOM_UI_TEXTURE_CACHE_REUSE PASS "+index+" uploads="+uploads);
                            capture(client,index);
                        });
                    } catch(InterruptedException error) {Thread.currentThread().interrupt();client.execute(client::stop);}
                },"loom-cache-probe").start();return;
            }
            if(index==66&&!importHandleChecked){
                var widget=(AbstractWidget)field(client.screen,"texturePreview").get(client.screen);
                var before=(LoomProject)invokeCandidate(client.screen);
                var press=mouse(widget.getX()+widget.getWidth()/2.0,widget.getY()+widget.getHeight()/2.0,0);
                client.screen.mouseClicked(press,false);client.screen.mouseDragged(mouse(press.x()+6,press.y()+3,0),6,3);client.screen.mouseReleased(mouse(press.x()+6,press.y()+3,0));
                if(before.hash().equals(((LoomProject)invokeCandidate(client.screen)).hash()))throw new IllegalStateException("Import artwork drag did not change candidate");
                importHandleChecked=true;System.out.println("LOOM_UI_IMPORT_HANDLES PASS: Screen drag changes the rendered import candidate");
                new Thread(()->{try{Thread.sleep(400);client.execute(()->capture(client,index));}catch(InterruptedException e){Thread.currentThread().interrupt();}},"loom-handle-capture").start();return;
            }
            for (var child : client.screen.children()) if (child instanceof AbstractWidget w && w.visible) {
                if (w.getX() < 0 || w.getY() < 0 || w.getRight() > client.screen.width || w.getBottom() > client.screen.height - 28)
                    throw new IllegalStateException("Out of bounds: " + w.getClass().getSimpleName() + " " + w.getMessage().getString());
            }
            var widgets=client.screen.children().stream().filter(c -> c instanceof AbstractWidget w && w.visible).map(c -> (AbstractWidget)c).toList();
            for(int a=0;a<widgets.size();a++) for(int b=a+1;b<widgets.size();b++) {
                var x=widgets.get(a); var y=widgets.get(b);
                if(x instanceof dev.loomstudios.client.ui.LoomPaletteWindow || y instanceof dev.loomstudios.client.ui.LoomPaletteWindow) continue;
                if(x.getX()<y.getRight() && x.getRight()>y.getX() && x.getY()<y.getBottom() && x.getBottom()>y.getY())
                    throw new IllegalStateException("Overlapping controls: "+x.getMessage().getString()+" / "+y.getMessage().getString());
            }
            Path dir = Path.of("../docs/verification/editor-workspace"); Files.createDirectories(dir);
            String name =
          index >= 310
              ? assetLibraryCaptureName(index)
              : index >= 278
              ? creativeCaptureName(index)
              : index>=238?authoringCaptureName(index):index>=218?"choices-"+new String[]{"home-insets","preset-dropdown","studio-effect-dropdown","elytra-effect-dropdown","hidden-held-items"}[(index-218)%5]+"-"+PROFILES[(index-218)/5][0]+"x"+PROFILES[(index-218)/5][1]+"-gui"+PROFILES[(index-218)/5][2]:index>=190?"usability-"+new String[]{"home","swatches-scroll","cape-only","elytra-only","animation-presets","animation-advanced","expanded-cape-only"}[(index-190)%7]+"-"+PROFILES[(index-190)/7][0]+"x"+PROFILES[(index-190)/7][1]+"-gui"+PROFILES[(index-190)/7][2]:index>=182?"premium-"+(index%2==0?"smooth":"native")+"-"+PROFILES[(index-182)/2][0]+"x"+PROFILES[(index-182)/2][1]+"-gui"+PROFILES[(index-182)/2][2]:index>=128?polishCaptureName(index):index>=100?safetyCaptureName(index):index>=96 ? new String[]{"library-context-635x320","library-drafts-635x320","library-trash-635x320","rename-635x320"}[index-96] : index>=68 ? new String[]{"library","templates","settings","cape-animation-studio","elytra-animation-studio","preview-open","preview-gliding"}[(index-68)%7]+"-"+PROFILES[(index-68)/7][0]+"x"+PROFILES[(index-68)/7][1]+"-gui"+PROFILES[(index-68)/7][2] : index>=42 ? new String[]{"cape-windowed-gui3","elytra-windowed-gui3","cape-properties-windowed-gui3","elytra-animation-windowed-gui3","home-windowed-gui3","share-windowed-gui3","smart-import-windowed-gui3","cape-palette-windowed-gui3","elytra-palette-windowed-gui3","circle-live-windowed-gui3","circle-committed-windowed-gui3","cape-middle-pan-windowed-gui3","elytra-middle-pan-windowed-gui3","cape-transparent-guide-windowed-gui3","elytra-transparent-guide-windowed-gui3","cape-635x320-gui3","elytra-properties-635x320-gui3","cape-expanded-635x320-gui3","elytra-expanded-635x320-gui3","circle-filled-live-gui3","circle-filled-committed-gui3","cape-3d-pan-gui3","elytra-3d-pan-gui3","home-3d-pan-gui3","smart-import-3d-pan-gui3","share-3d-pan-gui3"}[index-42] : index >= 22 ? new String[]{"home","share-export","share-import","smart-import-placement","smart-import-processing"}[(index-22)%5] + "-" + PROFILES[(index-22)/5][0]+"x"+PROFILES[(index-22)/5][1]+"-gui"+PROFILES[(index-22)/5][2]
                    : index < 8 ? (index % 2 == 0 ? "cape" : "elytra") + "-" + PROFILES[index/2][0] + "x" + PROFILES[index/2][1] + "-gui" + PROFILES[index/2][2]
                    : new String[]{"cape-color-compact","cape-properties-compact","elytra-animation-compact","elytra-playback-compact","cape-gradient-compact","cape-transform-compact","cape-stops-compact","elytra-properties-compact","elytra-color-compact","cape-many-layers-compact","elytra-many-tracks-compact","cape-single-pixel-live-200percent","cape-single-pixel-committed-200percent","cape-small-window-guidance"}[index-8];
            Screenshot.takeScreenshot(client.getMainRenderTarget(), image -> {
                try { image.writeToFile(dir.resolve(name+".png")); System.out.println("LOOM_UI_CAPTURE " + name + " " + client.screen.width + "x" + client.screen.height); }
                catch(Exception ex) { ex.printStackTrace(); } finally { image.close(); stage++; wait=5; pending=false; prepared=false; }
            });
        } catch (Exception ex) { ex.printStackTrace(); client.stop(); }
    }
    private static net.minecraft.client.input.MouseButtonEvent mouse(double x,double y,int button) {
        return new net.minecraft.client.input.MouseButtonEvent(x,y,new net.minecraft.client.input.MouseButtonInfo(button,0));
    }
    private static void verifyPan(Screen screen) throws Exception {
        var canvas=(AbstractWidget)field(screen,"canvasWidget").get(screen);
        for(int i=0;i<4;i++) canvas.mouseScrolled(canvas.getX()+canvas.getWidth()/2.0,canvas.getY()+canvas.getHeight()/2.0,0,1);
        var view=canvas.getClass().getMethod("viewState");Object before=view.invoke(canvas);
        var press=mouse(canvas.getX()+canvas.getWidth()/2.0,canvas.getY()+canvas.getHeight()/2.0,2);
        if(!screen.mouseClicked(press,false)||!screen.mouseDragged(mouse(press.x()+12,press.y()+8,2),12,8)
                ||!screen.mouseReleased(mouse(press.x()+12,press.y()+8,2)))throw new IllegalStateException("Middle-button route lost");
        if(before.equals(view.invoke(canvas)))throw new IllegalStateException("Canvas did not pan through Screen");
    }
    private static void verifyPreviewPan(Screen screen) throws Exception {
        var preview=(dev.loomstudios.client.ui.LoomPlayerPreviewWidget)field(screen,screen instanceof SmartImportScreen?"playerPreview":"previewWidget").get(screen);
        var press=mouse(preview.getX()+preview.getWidth()/2.0,preview.getY()+preview.getHeight()/2.0,2);
        for(int i=0;i<4;i++)screen.mouseScrolled(press.x(),press.y(),0,1);
        var before=preview.viewState();
        if(!screen.mouseClicked(press,false)||!screen.mouseDragged(mouse(press.x()+8,press.y()+6,2),8,6)
                ||!screen.mouseReleased(mouse(press.x()+8,press.y()+6,2)))throw new IllegalStateException("3D pan route lost");
        var after=preview.viewState();
        if(after.panX()!=before.panX()+8||after.panY()!=before.panY()+6)throw new IllegalStateException("3D pan unchanged");
    }
    private static void verifyInputAndPreview(Minecraft client) throws Exception {
        for(boolean wing:new boolean[]{false,true}) {
            ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
            Screen screen=wing?new ElytraEditorScreen(new LoomHomeScreen()):new CapeEditorScreen(new LoomHomeScreen());client.setScreen(screen);
            verifyPan(screen);verifyPreviewPan(screen);call(screen,wing?"toggleSwatches":"togglePaletteWindow");
            var palette=(dev.loomstudios.client.ui.LoomPaletteWindow)field(screen,"paletteWindow").get(screen);
            var close=(AbstractWidget)palette.children().stream().filter(c->c instanceof AbstractWidget w && w.getMessage().getString().equals("Close swatches")).findFirst().orElseThrow();
            screen.mouseClicked(mouse(close.getX()+8,close.getY()+8,0),false);
            if(palette.visible||(boolean)field(screen,"paletteWindowVisible").get(screen))throw new IllegalStateException("Palette close did not update editor state");
            if(!wing) {
                var layerId=(java.util.UUID)field(screen,"selectedLayerId").get(screen);
                ClientProjectWorkspace.apply(project -> dev.loomstudios.project.ProjectEdits.setCapeLayerLocked(project,layerId,true));
                set(screen,"workspaceState",ClientProjectWorkspace.state());call(screen,"updateButtonStates");
                if(((AbstractWidget)field(screen,"circleButton").get(screen)).active)throw new IllegalStateException("Circle remains enabled for a locked layer");
            }
        }
        var expanded=new dev.loomstudios.client.screen.LoomPlayerPreviewScreen(client.screen,ClientProjectWorkspace::project);
        client.setScreen(expanded);var center=mouse(expanded.width/2.0,expanded.height/2.0,2);
        if(!expanded.mouseClicked(center,false)||!expanded.mouseDragged(mouse(center.x()+8,center.y()+6,2),8,6)||!expanded.mouseReleased(center))throw new IllegalStateException("Expanded middle pan route lost");
        if(field(expanded,"panX").getInt(expanded)!=8||field(expanded,"panY").getInt(expanded)!=6)throw new IllegalStateException("Expanded pan unchanged");
        var first=dev.loomstudios.client.render.PlayerCosmeticRenderer.withPreviewProject(client,fixtureProject,()->dev.loomstudios.client.render.LoomPreviewState.extract(client.player));
        var second=dev.loomstudios.client.render.LoomPreviewState.extract(client.player);
        var counter=dev.loomstudios.client.render.PlayerCosmeticRenderer.class.getDeclaredField("previewHashComputations");counter.setAccessible(true);
        long beforeHashes=counter.getLong(null);
        for(int i=0;i<20;i++) {
            dev.loomstudios.client.render.LoomPreviewState.extract(client.player);
            var reused=(net.minecraft.client.renderer.entity.state.AvatarRenderState)dev.loomstudios.client.render.PlayerCosmeticRenderer.withPreviewProject(client,fixtureProject,()->dev.loomstudios.client.render.LoomPreviewState.extract(client.player));
            if(reused.skin!=((net.minecraft.client.renderer.entity.state.AvatarRenderState)first).skin)throw new IllegalStateException("World/preview frames invalidate each other's skin patch");
        }
        if(counter.getLong(null)!=beforeHashes)throw new IllegalStateException("Immutable preview rehashes every frame");
        if(first==second)throw new IllegalStateException("Preview shares mutable state");
        var avatar=(net.minecraft.client.renderer.entity.state.AvatarRenderState)first;
        if(avatar.skin.cape()==null||avatar.skin.elytra()==null)throw new IllegalStateException("Preview has no cosmetic assets");
        dev.loomstudios.client.render.LoomPreviewState.orient(first,25);
        if(avatar.yRot!=0||avatar.bodyRot!=180)throw new IllegalStateException("Preview head/body orientation diverged");
        var blank=dev.loomstudios.project.LoomProjectFactory.blank("Alpha isolation",1L);
        var blankState=(net.minecraft.client.renderer.entity.state.AvatarRenderState)dev.loomstudios.client.render.PlayerCosmeticRenderer.withPreviewProject(client,blank,()->dev.loomstudios.client.render.LoomPreviewState.extract(client.player));
        var guide=dev.loomstudios.client.render.RuntimeCosmeticCache.byCapeTexture(blankState.skin.cape().texturePath());
        var actual=dev.loomstudios.client.render.RuntimeCosmeticCache.getOrCompile(client,blank.hash(),blank);
        for(String channel:new String[]{"capeImage","elytraImage"}) {
            var imageField=guide.getClass().getDeclaredField(channel);imageField.setAccessible(true);
            int guided=((com.mojang.blaze3d.platform.NativeImage)imageField.get(guide)).getPixel(1,1);
            int authored=((com.mojang.blaze3d.platform.NativeImage)imageField.get(actual)).getPixel(1,1);
            if(guided>>>24!=255 || authored!=0)throw new IllegalStateException("Alpha guide leaked into "+channel);
        }
        if(blank.cape().layers().getFirst().pixelAt(65)!=0||blank.elytra().layers().getFirst().pixelAt(65)!=0)
            throw new IllegalStateException("Alpha guide modified the project");
        dev.loomstudios.client.render.RuntimeCosmeticCache.release(client,blank.hash());
        System.out.println(
        "LOOM_UI_INPUT_PREVIEW PASS: Screen canvas/3D/expanded middle pan, palette close, isolated"
            + " snapshot, cosmetic assets, 20-frame hash/skin reuse and Cape/Elytra alpha"
            + " isolation");
    }
    private static void verifyWorkflows(Minecraft client) throws Exception {
        var projectPath=dev.loomstudios.client.sharing.LoomShareExportAdapter.exportProject(fixtureProject);
        var portablePath=dev.loomstudios.client.sharing.LoomShareExportAdapter.exportPortableCode(fixtureProject);
        var capePath=dev.loomstudios.client.sharing.LoomShareExportAdapter.exportCapePng(fixtureProject);
        var wingPath=dev.loomstudios.client.sharing.LoomShareExportAdapter.exportElytraPng(fixtureProject);
        if(!dev.loomstudios.project.LoomProjectCodec.decode(Files.readAllBytes(projectPath)).hash().equals(fixtureProject.hash())
                || !dev.loomstudios.project.LoomProjectCode.decodePortable(Files.readString(portablePath)).hash().equals(fixtureProject.hash()))
            throw new IllegalStateException("Export round-trip changed the project");
        try(var cape=com.mojang.blaze3d.platform.NativeImage.read(Files.readAllBytes(capePath));
                var wing=com.mojang.blaze3d.platform.NativeImage.read(Files.readAllBytes(wingPath))) {
            if(cape.getWidth()!=fixtureProject.cape().width() || cape.getHeight()!=fixtureProject.cape().height()
                    || wing.getWidth()!=fixtureProject.elytra().width() || wing.getHeight()!=fixtureProject.elytra().height())
                throw new IllegalStateException("Export texture dimensions changed");
        }
        for(boolean processing : new boolean[]{false,true}) {
            ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
            var screen=new SmartImportScreen(new LoomHomeScreen(),dev.loomstudios.project.CapeUvRegion.OUTSIDE);
            var source=LoomCaptureFixtures.source(fixtureProject);
            set(screen,"loaded",new PngImportAdapter.LoadedImage(Path.of("capture-moon.png"),source,source));
            if(processing) set(screen,"panelTab",enumValue(screen,"panelTab","PROCESSING"));
            client.setScreen(screen);
            var candidateMethod=screen.getClass().getDeclaredMethod("candidateProject"); candidateMethod.setAccessible(true);
            var candidate=(LoomProject)candidateMethod.invoke(screen);
            call(screen,"applyImport");
            var actual=ClientProjectWorkspace.project();
            if(actual.cape().layers().size()!=fixtureProject.cape().layers().size()+1
                    || !java.util.Arrays.equals(dev.loomstudios.client.render.LoomTextureCompiler.compile(candidate.cape(),0,false,false),
                            dev.loomstudios.client.render.LoomTextureCompiler.compile(actual.cape(),0,false,false)))
                throw new IllegalStateException("Applied import differs from candidate preview: base="+fixtureProject.cape().layers().size()+" actual="+actual.cape().layers().size()+" error="+ClientProjectWorkspace.session().editError());
        }
        System.out.println("LOOM_UI_WORKFLOWS PASS: project/portable/PNG export and both import pages");
    }
    private static void prepareMilestone(Minecraft client) throws Exception {
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        int view=stage>=96?0:(stage-68)%7;
        Screen screen;
        if(view==0) screen=new dev.loomstudios.client.screen.LoomLibraryScreen(new LoomHomeScreen());
        else if(view==1) screen=new dev.loomstudios.client.screen.LoomTemplatesScreen(new LoomHomeScreen());
        else if(view==2) screen=new dev.loomstudios.client.screen.LoomSettingsScreen(new LoomHomeScreen());
        else if(view<=4) {
            var channel=view==3?dev.loomstudios.project.AnimationChannel.CAPE:dev.loomstudios.project.AnimationChannel.ELYTRA;
            var layer=(view==3?ClientProjectWorkspace.project().cape():ClientProjectWorkspace.project().elytra()).layers().getFirst();
            screen=new dev.loomstudios.client.screen.LoomAnimationScreen(new LoomHomeScreen(),channel,layer.id());
        } else {
            var preview=new dev.loomstudios.client.screen.LoomPlayerPreviewScreen(new LoomHomeScreen(),ClientProjectWorkspace::project);
            set(preview,"mode",enumValue(preview,"mode","ELYTRA"));
            preview.setView(25,0,1,0,0,view==5?dev.loomstudios.client.render.LoomPreviewState.PreviewPose.OPEN:dev.loomstudios.client.render.LoomPreviewState.PreviewPose.GLIDING,0);
            screen=preview;
        }
        client.setScreen(screen);
        if(screen instanceof dev.loomstudios.client.screen.LoomAnimationScreen animation) animation.addTrack();
        if(stage>=96) {
            if(stage==96) {
                var cards=(java.util.List<?>)field(screen,"cards").get(screen);
                if(cards.isEmpty())throw new IllegalStateException("Library fixture missing");
                var card=(AbstractWidget)cards.getFirst();
                screen.mouseClicked(mouse(card.getX()+10,card.getY()+12,1),false);
                if(field(screen,"menu").get(screen)==null)throw new IllegalStateException("Right click menu missing");
            } else if(stage==97 || stage==98) {
                set(screen,"tab",stage==97?1:2);call(screen,"rebuild");
            } else {
                screen=new dev.loomstudios.client.screen.LoomRenameScreen(screen,"Moonlit",name->{});client.setScreen(screen);
            }
        }
    }
    private static void verifyLibraryWorkflows(Minecraft client) throws Exception {
        var project=dev.loomstudios.project.LoomProjectCode.forkImported(fixtureProject,System.currentTimeMillis()).withName("Library action test");
        var path=LocalProjectLibrary.save(project);
        var library=new dev.loomstudios.client.screen.LoomLibraryScreen(new LoomHomeScreen(),project.projectId());client.setScreen(library);
        if(field(library,"menu").get(library)==null)throw new IllegalStateException("Context selection not loaded");
        var act=library.getClass().getDeclaredMethod("act",int.class);act.setAccessible(true);
        act.invoke(library,4);
        if(!(client.screen instanceof dev.loomstudios.client.screen.LoomDecisionScreen)||!Files.exists(path))throw new IllegalStateException("Delete confirmation bypassed");
        choose(client,0);
        if(Files.exists(path))throw new IllegalStateException("Delete did not remove saved design");
        set(library,"tab",2);call(library,"rebuild");
        var selected=(dev.loomstudios.client.project.ProjectDescriptor)field(library,"selected").get(library);
        if(selected==null||!selected.projectId().equals(project.projectId()))throw new IllegalStateException("Deleted design not in Trash");
        set(library,"menu",selected);act.invoke(library,0);
        if(!Files.exists(path)||!LocalProjectLibrary.store().load(path).hash().equals(project.hash()))throw new IllegalStateException("Trash restore changed artwork");
        var cards=(java.util.List<?>)field(library,"cards").get(library);
        var descriptors=(java.util.Map<?,?>)field(library,"descriptors").get(library);
        AbstractWidget target=null;
        for(var c:cards)if(((dev.loomstudios.client.project.ProjectDescriptor)descriptors.get(c)).projectId().equals(project.projectId()))target=(AbstractWidget)c;
        if(target==null)throw new IllegalStateException("Restored card missing");
        library.mouseClicked(mouse(target.getX()+10,target.getY()+12,0),true);
        if(client.screen instanceof dev.loomstudios.client.screen.LoomDecisionScreen)choose(client,1);
        if(!(client.screen instanceof CapeEditorScreen)||!ClientProjectWorkspace.project().projectId().equals(project.projectId()))throw new IllegalStateException("Double click did not select and edit design");
        ClientProjectWorkspace.apply(p->p.withName("Recovered draft"));dev.loomstudios.client.project.WorkspaceRecovery.checkpoint();
        if(!LocalProjectLibrary.store().load(path).hash().equals(project.hash()))throw new IllegalStateException("Autosave overwrote explicit save");
        var draft=dev.loomstudios.client.project.WorkspaceRecovery.STORE.pathFor(project.projectId());
        ClientProjectWorkspace.recover(draft,client.player.getUUID());
        if(!ClientProjectWorkspace.isDirty()||!ClientProjectWorkspace.project().name().equals("Recovered draft"))throw new IllegalStateException("Draft recovery lost edits");
        ClientProjectWorkspace.save();
        if(Files.exists(draft))throw new IllegalStateException("Explicit save retained stale recovery draft");
        var animation=new dev.loomstudios.client.screen.LoomAnimationScreen(new LoomHomeScreen(),dev.loomstudios.project.AnimationChannel.CAPE,ClientProjectWorkspace.project().cape().layers().getFirst().id());client.setScreen(animation);animation.addTrack();
        var original=ClientProjectWorkspace.project().animation();
        var timeline=(AbstractWidget)field(animation,"timeline").get(animation);
        Method left=timeline.getClass().getDeclaredMethod("timelineLeft"),top=timeline.getClass().getDeclaredMethod("rowTop");left.setAccessible(true);top.setAccessible(true);
        int x=(int)left.invoke(timeline),y=(int)top.invoke(timeline)+10;
        animation.mouseClicked(mouse(x,y,0),false);animation.mouseDragged(mouse(x+30,y,0),30,0);animation.mouseReleased(mouse(x+30,y,0));
        if(ClientProjectWorkspace.session().isCompoundEditActive()||ClientProjectWorkspace.project().animation().equals(original))throw new IllegalStateException("Animation keyframe drag did not commit");
        ClientProjectWorkspace.undo();if(!ClientProjectWorkspace.project().animation().equals(original))throw new IllegalStateException("Keyframe drag did not undo as one edit");
        System.out.println("LOOM_UI_ANIMATION PASS: Screen keyframe dragging and single-step undo");
        System.out.println(
        "LOOM_UI_LIBRARY PASS: right click actions, delete, Trash restore, double click edit,"
            + " isolated draft recovery and explicit save cleanup");
  }

  private static String assetLibraryCaptureName(int index) {
    int[] p = PROFILES[(index - 310) / 4];
    String view = new String[] {
      "cape-design", "cape-worn", "elytra-design", "elytra-worn"
    }[(index - 310) % 4];
    return "asset-library-" + view + "-" + p[0] + "x" + p[1] + "-gui" + p[2];
  }

  /** Semantic-face background, stored as ordinary project-owned ARGB pixels. */
  private static dev.loomstudios.project.LoomProject addGalleryBackdrop(
      dev.loomstudios.project.LoomProject project, boolean wing,
      dev.loomstudios.project.ElytraWing selectedWing) {
    var canvas = wing ? project.elytra() : project.cape();
    int scale = dev.loomstudios.project.CanvasResolution.fromCanvas(canvas).scale();
    int x, y, w, h;
    if (wing) {
      var face = dev.loomstudios.project.ElytraSurface.OUTSIDE;
      x = face.atlasX(selectedWing, 0, scale);
      y = face.atlasY(0, scale);
      w = face.width(scale);
      h = face.height(scale);
    } else {
      var face = dev.loomstudios.project.CapeUvRegion.OUTSIDE;
      x = face.atlasX(0, scale);
      y = face.atlasY(0, scale);
      w = face.width(scale);
      h = face.height(scale);
    }
    var clip = new dev.loomstudios.project.NormalizedRect(
        x/(double)canvas.width(), y/(double)canvas.height(),
        w/(double)canvas.width(), h/(double)canvas.height());
    int bw=32,bh=64;
    int[] raw = new int[bw*bh];
    for (int iy=0; iy<bh; iy++) {
      double t=iy/(double)(bh-1);
      int red=(int)(12 + 13*t), green=(int)(20 + 10*t), blue=(int)(48 + 25*t);
      for(int ix=0;ix<bw;ix++) {
        int d=(int)(Math.sin(ix*.39+iy*.27)*2);
        raw[iy*bw+ix] = 0xFF000000 | ((red+d)<<16) | ((green+d)<<8) | (blue+d);
      }
    }
    var img = new dev.loomstudios.image.PixelImage(bw,bh,raw);
    var data = dev.loomstudios.project.ImageLayerData.placed(
        img,canvas.width(),canvas.height(),clip,
        dev.loomstudios.image.ImagePlacementMode.STRETCH);
    return wing
        ? dev.loomstudios.project.ProjectEdits.addElytraImageLayer(project,"Midnight Sky",data)
        : dev.loomstudios.project.ProjectEdits.addCapeImageLayer(project,"Midnight Sky",data);
  }

  private static dev.loomstudios.project.CustomStamp galleryStamp(String name) {
    return dev.loomstudios.project.CreativeAssetCatalog.search(name,"Featured")
        .stream().filter(e->e.stamp().name().equals(name))
        .findFirst().orElseThrow().stamp();
  }

  private static void prepareAssetLibrary(Minecraft client,int mode) throws Exception {
    ClientProjectWorkspace.open(fixturePath, client.player.getUUID());
    // Each fixture uses ORIGINAL pixel assets in independently editable project-owned
    // image layers. Do not merely show preview tiles and claim these are worn art.
    boolean elytra=mode>=2;
    ClientProjectWorkspace.apply(p -> {
      if (!elytra) {
        p=addGalleryBackdrop(p,false,null);
        var moon=galleryStamp("Moonstone Crescent");
        var star=galleryStamp("Frostfire Star");
        var pine=galleryStamp("Snowkissed Fir");
        p=dev.loomstudios.project.AssetPlacement.place(p,
            dev.loomstudios.project.AnimationChannel.CAPE,moon.name(),moon.patch(),
            dev.loomstudios.project.CapeUvRegion.OUTSIDE,null,null,
            .69,.30,22,false,0);
        p=dev.loomstudios.project.AssetPlacement.place(p,
            dev.loomstudios.project.AnimationChannel.CAPE,star.name(),star.patch(),
            dev.loomstudios.project.CapeUvRegion.OUTSIDE,null,null,
            .24,.20,10,false,0);
        return dev.loomstudios.project.AssetPlacement.place(p,
            dev.loomstudios.project.AnimationChannel.CAPE,pine.name(),pine.patch(),
            dev.loomstudios.project.CapeUvRegion.OUTSIDE,null,null,
            .56,.84,24,false,0);
      }
      var left=dev.loomstudios.project.ElytraWing.LEFT;
      var right=dev.loomstudios.project.ElytraWing.RIGHT;
      var face=dev.loomstudios.project.ElytraSurface.OUTSIDE;
      p=addGalleryBackdrop(p,true,left);
      p=addGalleryBackdrop(p,true,right);
      var crystal=galleryStamp("Prismatic Crystal");
      var halo=galleryStamp("Runic Halo");
      var aurora=galleryStamp("Aurora Ribbon");
      p=dev.loomstudios.project.AssetPlacement.place(p,
          dev.loomstudios.project.AnimationChannel.ELYTRA,aurora.name(),aurora.patch(),
          null,left,face,.5,.33,29,false,0);
      p=dev.loomstudios.project.AssetPlacement.place(p,
          dev.loomstudios.project.AnimationChannel.ELYTRA,crystal.name(),crystal.patch(),
          null,left,face,.47,.64,21,false,0);
      p=dev.loomstudios.project.AssetPlacement.place(p,
          dev.loomstudios.project.AnimationChannel.ELYTRA,aurora.name(),aurora.patch(),
          null,right,face,.5,.33,29,false,0);
      return dev.loomstudios.project.AssetPlacement.place(p,
          dev.loomstudios.project.AnimationChannel.ELYTRA,halo.name(),halo.patch(),
          null,right,face,.52,.67,20,false,0);
    });
    var project=ClientProjectWorkspace.project();
    var selected=elytra?project.elytra().layers().getLast():project.cape().layers().getLast();
    // Fail closed if the new layers were not actually materialized in the
    // player-preview project and are not independently editable Image layers.
    if (selected.kind()!=dev.loomstudios.project.LayerKind.IMAGE
        || selected.imageData().source().pixels().length==0
        || (elytra?project.elytra().layers().size():project.cape().layers().size())<5)
      throw new IllegalStateException("Asset preview fixture lacks placed editable artwork");
    var screen=new dev.loomstudios.client.screen.LoomAssetLibraryScreen(
        new LoomHomeScreen(),elytra,selected.id(),
        dev.loomstudios.project.CapeUvRegion.OUTSIDE,
        dev.loomstudios.project.ElytraWing.LEFT,
        dev.loomstudios.project.ElytraSurface.OUTSIDE,0xFF45DDE8);
    client.setScreen(screen);
    if(mode%2==1)screen.showWornPreview(true);
  }

  private static String creativeCaptureName(int index) {
    int[] p = PROFILES[(index - 278) / 8];
    return "creative-"
        + new String[] {
              "lanes",
              "curve",
              "zoom-preview",
              "reference-above",
              "reference-below",
              "cape-frames-onion",
              "elytra-frames",
              "custom-stamps"
            }
            [(index - 278) % 8]
        + "-"
        + p[0]
        + "x"
        + p[1]
        + "-gui"
        + p[2];
  }

  private static void prepareCreative(Minecraft client) throws Exception {
    ClientProjectWorkspace.open(fixturePath, client.player.getUUID());
    var project = ClientProjectWorkspace.project();
    var home = new LoomHomeScreen();
    int view = (stage - 278) % 8;
    boolean wing = view == 4 || view == 6;
    var channel =
        wing
            ? dev.loomstudios.project.AnimationChannel.ELYTRA
            : dev.loomstudios.project.AnimationChannel.CAPE;
    var id = (wing ? project.elytra() : project.cape()).layers().getFirst().id();
    var source = LoomCaptureFixtures.source(fixtureProject);
    var altPixels = source.pixels();
    for (int i = 0; i < altPixels.length; i++)
      if ((altPixels[i] >>> 24) > 0)
        altPixels[i] = dev.loomstudios.project.EditableFrames.over(altPixels[i], 0x5522EEFF);
    var alt = new dev.loomstudios.image.PixelImage(source.width(), source.height(), altPixels);
    var target =
        wing
            ? new dev.loomstudios.project.NormalizedRect(38 / 64.0, 2 / 32.0, 10 / 64.0, 20 / 32.0)
            : new dev.loomstudios.project.NormalizedRect(1 / 64.0, 1 / 32.0, 10 / 64.0, 16 / 32.0);
    var guide =
        new dev.loomstudios.project.ReferenceImage(
            java.util.UUID.randomUUID(),
            "Moon guide",
            channel,
            dev.loomstudios.project.ImageLayerData.placed(
                source, 64, 32, target, dev.loomstudios.image.ImagePlacementMode.FIT),
            .35f,
            true,
            true,
            view == 3);
    dev.loomstudios.client.project.EditorOverlayState.references(
        project.projectId(), java.util.List.of(guide));
    dev.loomstudios.client.project.EditorOverlayState.onion(0, .3f);
    if (view < 3) {
      var animationLayer = id;
      ClientProjectWorkspace.apply(
          p -> {
            var a =
                dev.loomstudios.project.AnimationAuthoring.addTrack(
                    p.animation(),
                    animationLayer,
                    channel,
                    dev.loomstudios.project.AnimationEffectType.SPARKLE);
            var t = a.tracks().getLast();
            for (var parameter : dev.loomstudios.project.AnimationParameter.forEffect(t.effect()))
              t =
                  dev.loomstudios.project.AnimationKeyEditing.addLane(
                      t, parameter, a.durationTicks());
            return p.withAnimation(dev.loomstudios.project.AnimationAuthoring.replaceTrack(a, t));
          });
      var t = ClientProjectWorkspace.project().animation().tracks().getLast();
      var screen =
          new dev.loomstudios.client.screen.LoomParameterAnimationScreen(home, channel, t.id(), 20);
      client.setScreen(screen);
      set(screen, "parameter", dev.loomstudios.project.AnimationParameter.DENSITY);
      if (view == 1) {
        ClientProjectWorkspace.apply(p -> p.withAnimation(
            dev.loomstudios.project.AnimationKeyEditing.edit(p.animation(), java.util.Set.of(
                new dev.loomstudios.project.AnimationKeyEditing.Address(t.id(),
                    dev.loomstudios.project.AnimationParameter.DENSITY, 0)),
                k -> new dev.loomstudios.project.AnimationKeyframe(k.tick(), k.value(),
                    dev.loomstudios.project.AnimationEasing.CUSTOM,
                    new dev.loomstudios.project.KeyframeCurve(.15f, 0, .85f, 1)), false)));
        var selected =
            (java.util.Set<dev.loomstudios.project.AnimationKeyEditing.Address>)
                field(screen, "selected").get(screen);
        selected.add(
            new dev.loomstudios.project.AnimationKeyEditing.Address(
                t.id(), dev.loomstudios.project.AnimationParameter.DENSITY, 0));
      }
      if (view == 2) {
        set(screen, "zoom", 4);
        set(screen, "previewMode", true);
      }
      call(screen, "rebuildWidgets");
    } else {
      int tab = view < 5 ? 0 : view < 7 ? 1 : 2;
      if (tab == 1) {
        var data =
            dev.loomstudios.project.ImageLayerData.placed(
                    source, 64, 32, target, dev.loomstudios.image.ImagePlacementMode.STRETCH)
                .withFrames(java.util.List.of(source, alt, source), java.util.List.of(4, 8, 4));
        ClientProjectWorkspace.apply(
            p ->
                wing
                    ? dev.loomstudios.project.ProjectEdits.addElytraImageLayer(
                        p, "Editable GIF", data)
                    : dev.loomstudios.project.ProjectEdits.addCapeImageLayer(
                        p, "Editable GIF", data));
        id =
            (wing
                    ? ClientProjectWorkspace.project().elytra()
                    : ClientProjectWorkspace.project().cape())
                .layers()
                .getLast()
                .id();
      }
      var selection = new java.util.BitSet();
      selection.set(0, 16);
      var screen =
          new dev.loomstudios.client.screen.LoomCreativeAssetsScreen(
                  home,
                  wing,
                  id,
                  dev.loomstudios.project.CapeUvRegion.OUTSIDE,
                  dev.loomstudios.project.ElytraWing.LEFT,
                  dev.loomstudios.project.ElytraSurface.OUTSIDE,
                  0xFF45DDE8,
                  selection)
              .openTab(tab);
      client.setScreen(screen);
      if (tab == 1) LoomAuthoringVerification.press(screen, "Convert GIF to Editable Animation");
    }
    }
    private static String authoringCaptureName(int index){int view=(index-238)%10;int[] p=PROFILES[(index-238)/10];return "authoring-"+new String[]{"cape-8x","elytra-8x","elytra-inside","wand","seam-stamps","layer-mask","image-tint","background-remove","typed-effect","timeline"}[view]+"-"+p[0]+"x"+p[1]+"-gui"+p[2];}
    private static void prepareAuthoring(Minecraft client)throws Exception{
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        ClientProjectWorkspace.apply(p->{p=dev.loomstudios.project.ProjectResizer.resizeCape(p,dev.loomstudios.project.CanvasResolution.MAXIMUM);p=dev.loomstudios.project.ProjectResizer.resizeElytra(p,dev.loomstudios.project.CanvasResolution.MAXIMUM);while(p.cape().layers().size()<12)p=dev.loomstudios.project.ProjectEdits.addCapeLayer(p,"Detail");return p;});
        int view=(stage-238)%10;var home=new LoomHomeScreen();var p=ClientProjectWorkspace.project();var cape=p.cape().layers().getFirst().id();var wing=p.elytra().layers().getFirst().id();
        if(view==0){ClientProjectWorkspace.replaceWith(dev.loomstudios.project.LoomProjectCode.forkImported(p,System.currentTimeMillis()),client.player.getUUID());ClientProjectWorkspace.save();ClientProjectWorkspace.equipCurrent();client.setScreen(new CapeEditorScreen(home));}
        else if(view<3){var screen=new ElytraEditorScreen(home);client.setScreen(screen);if(view==2){set(screen,"wingSurface",dev.loomstudios.project.ElytraSurface.INSIDE);call(screen,"rebuildWidgets");}}
        else if(view<6){var screen=new dev.loomstudios.client.screen.LoomSurfaceToolsScreen(home,false,cape,dev.loomstudios.project.CapeUvRegion.OUTSIDE,null,null,0xFF45DAEE);client.setScreen(screen);if(view==3){Method click=screen.getClass().getDeclaredMethod("click",int.class,int.class);click.setAccessible(true);click.invoke(screen,24,24);}else if(view==4){set(screen,"mode",1);set(screen,"seams",true);call(screen,"rebuildWidgets");}else{ClientProjectWorkspace.apply(current->{var layer=current.cape().layers().getFirst();byte[] mask=new byte[current.cape().width()*current.cape().height()];java.util.Arrays.fill(mask,(byte)255);return current.withCape(current.cape().replaceLayer(cape,layer.withMask(mask)));});set(screen,"mode",2);set(screen,"maskEdit",true);call(screen,"rebuildWidgets");}}
        else if(view<8){var source=LoomCaptureFixtures.source(fixtureProject);var settings=dev.loomstudios.image.ImageProcessingSettings.defaults().withTint(0xFF78CAFF,.5f);if(view==7)settings=settings.withBackground(new dev.loomstudios.image.BackgroundRemoval(true,source.pixelAt(0,0),16,true,0,0));var screen=new dev.loomstudios.client.screen.LoomImageEffectsScreen(home,source,settings,value->{});client.setScreen(screen);if(view==7){set(screen,"backgroundTab",true);call(screen,"rebuildWidgets");}}
        else{
            ClientProjectWorkspace.apply(current->current.withAnimation(dev.loomstudios.project.AnimationAuthoring.addTrack(current.animation(),cape,dev.loomstudios.project.AnimationChannel.CAPE,dev.loomstudios.project.AnimationEffectType.SCROLL)));
            var track=ClientProjectWorkspace.project().animation().tracks().getLast();
            if(view==8)client.setScreen(new dev.loomstudios.client.screen.LoomEffectParametersScreen(home,track.id(),0));
            else{var screen=new dev.loomstudios.client.screen.LoomAnimationScreen(home,dev.loomstudios.project.AnimationChannel.CAPE,cape);client.setScreen(screen);set(screen,"advanced",true);call(screen,"rebuildWidgets");}
        }
    }
    private static void verifyAuthoringPayload(Minecraft client)throws Exception{
        var p=dev.loomstudios.project.ProjectResizer.resizeCape(fixtureProject,dev.loomstudios.project.CanvasResolution.MAXIMUM);
        p=dev.loomstudios.project.ProjectResizer.resizeElytra(p,dev.loomstudios.project.CanvasResolution.MAXIMUM);
        var random=new java.util.Random(113);var layers=new java.util.ArrayList<dev.loomstudios.project.LoomLayer>();
        for(int n=0;n<6;n++){int[] pixels=new int[512*256];for(int i=0;i<pixels.length;i++)pixels[i]=random.nextInt();layers.add(new dev.loomstudios.project.LoomLayer(java.util.UUID.randomUUID(),"Network "+n,true,1,dev.loomstudios.project.BlendMode.NORMAL,false,pixels));}
        p=p.withCape(new dev.loomstudios.project.LoomCanvas(512,256,layers));byte[] data=p.encode();if(data.length<1024*1024)throw new IllegalStateException("Large packet fixture was too small");String hash=dev.loomstudios.project.LoomProjectCodec.sha256(data);
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),client.level.registryAccess());
        try{var outbound=new dev.loomstudios.network.payload.ProjectBlobC2SPayload(hash,data);dev.loomstudios.network.payload.ProjectBlobC2SPayload.CODEC.encode(buffer,outbound);var inbound=dev.loomstudios.network.payload.ProjectBlobC2SPayload.CODEC.decode(buffer);if(!hash.equals(inbound.projectHash())||!java.util.Arrays.equals(data,inbound.data()))throw new IllegalStateException("Large C2S codec mismatch");buffer.clear();dev.loomstudios.network.payload.ProjectBlobS2CPayload.CODEC.encode(buffer,new dev.loomstudios.network.payload.ProjectBlobS2CPayload(hash,data));var downloaded=dev.loomstudios.network.payload.ProjectBlobS2CPayload.CODEC.decode(buffer);if(!java.util.Arrays.equals(data,downloaded.data()))throw new IllegalStateException("Large S2C codec mismatch");}
        finally{buffer.release();}
        Method validate=dev.loomstudios.network.LoomNetworking.class.getDeclaredMethod("validateProjectBlob",String.class,byte[].class);validate.setAccessible(true);if(!(Boolean)validate.invoke(null,hash,data))throw new IllegalStateException("Server rejects large valid project");byte[] corrupt=data.clone();corrupt[corrupt.length-1]^=1;if((Boolean)validate.invoke(null,hash,corrupt))throw new IllegalStateException("Server accepts corrupt project hash");
        System.out.println(
        "LOOM_AUTHORING_PAYLOAD PASS schema5 protocol3 bytes="+data.length+" save/load/core384MiB=COVERED");
        ClientProjectWorkspace.replaceWith(dev.loomstudios.project.LoomProjectCode.forkImported(p,System.currentTimeMillis()),client.player.getUUID());
        ClientProjectWorkspace.save();ClientProjectWorkspace.equipCurrent();networkFixtureHash=ClientProjectWorkspace.equippedProjectHash();
        client.setScreen(new LoomHomeScreen());
        dev.loomstudios.client.network.ClientCosmeticSync.tick(client);
    }
    private static Field staticField(Class<?> type,String name)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
    private static boolean verifyLiveAuthoringNetwork(Minecraft client)throws Exception {
        if(++networkWaitTicks>400)throw new IllegalStateException("Large live project upload/download timed out");
        var server=client.getSingleplayerServer();if(server==null)throw new IllegalStateException("Missing integrated server");
        if(networkCheck==null){
            networkCheck=new java.util.concurrent.CompletableFuture<>();var check=networkCheck;
            server.execute(()->{try {
                var projects=staticField(dev.loomstudios.network.LoomNetworking.class,"PROJECTS").get(null);
                var equipped=(java.util.Map<?,?>)staticField(dev.loomstudios.network.LoomNetworking.class,"EQUIPPED").get(null);
                boolean cached=(Boolean)projects.getClass().getMethod("containsKey",Object.class).invoke(projects,networkFixtureHash);
                check.complete(cached&&networkFixtureHash.equals(equipped.get(client.player.getUUID())));
            }catch(Exception e){check.completeExceptionally(e);}});
            return false;
        }
        if(!networkCheck.isDone())return false;
        boolean serverAccepted=networkCheck.join();networkCheck=null;
        var remote=staticField(dev.loomstudios.client.network.ClientCosmeticSync.class,"PROJECTS").get(null);
        boolean downloaded=(Boolean)remote.getClass().getMethod("containsKey",Object.class).invoke(remote,networkFixtureHash);
        if(!serverAccepted||!downloaded)return false;
        System.out.println(
        "LOOM_AUTHORING_NETWORK PASS: >1MiB C2S fragmentation, async validation, equip broadcast,"
            + " S2C fragmentation and client decode");
        return true;
    }
    private static void prepareChoices(Minecraft client)throws Exception{
        if(!Files.exists(fixturePath))LocalProjectLibrary.store().restore(fixtureProject.projectId());
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        int view=(stage-218)%5;var home=new LoomHomeScreen();
        if(view==0)client.setScreen(home);
        else if(view==4){
            client.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            client.player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
            var screen=new dev.loomstudios.client.screen.LoomPlayerPreviewScreen(home,ClientProjectWorkspace::project);screen.setCharacterVisible(false);client.setScreen(screen);
        }else if(view==3){
            var screen=new ElytraEditorScreen(home);client.setScreen(screen);call(screen,"addAnimationTrack");
            set(screen,"inspectorTab",enumValue(screen,"inspectorTab","ANIMATION"));call(screen,"updateInspectorVisibility");
            var button=(AbstractWidget)field(screen,"animationEffectButton").get(screen);screen.mouseClicked(mouse(button.getX()+10,button.getY()+8,0),false);
        }else{
            var screen=new dev.loomstudios.client.screen.LoomAnimationScreen(home,dev.loomstudios.project.AnimationChannel.CAPE,ClientProjectWorkspace.project().cape().layers().getFirst().id());client.setScreen(screen);screen.applyPreset();
            if(view==2){set(screen,"advanced",true);call(screen,"rebuildWidgets");}
            var button=(AbstractWidget)field(screen,view==1?"presetButton":"effectButton").get(screen);screen.mouseClicked(mouse(button.getX()+10,button.getY()+8,0),false);
        }
    }
    private static void prepareUsability(Minecraft client)throws Exception{
        if(!Files.exists(fixturePath))LocalProjectLibrary.store().restore(fixtureProject.projectId());
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        int view=(stage-190)%7;var home=new LoomHomeScreen();
        if(view==0)client.setScreen(home);
        else if(view==1){
            for(int paletteIndex=0;paletteIndex<5;paletteIndex++){
                dev.loomstudios.client.palette.ColorPaletteLibrary.create("Study palette "+paletteIndex,0xFF29385A);
                for(int color=0;color<12;color++)dev.loomstudios.client.palette.ColorPaletteLibrary.addColorToSelected(0xFF000000|((paletteIndex*45+color*19)&255)<<16|((color*27)&255)<<8|170);
            }
            var editor=new CapeEditorScreen(home);client.setScreen(editor);call(editor,"togglePaletteWindow");
            var palette=(dev.loomstudios.client.ui.LoomPaletteWindow)field(editor,"paletteWindow").get(editor);
            set(palette,"managementExpanded",true);call(palette,"applyManagementVisibility");set(palette,"swatchesScroll",40);
        }else if(view==2||view==3){
            if(view==3)ClientProjectWorkspace.apply(p->{var recipe=dev.loomstudios.project.AnimationPreset.GLOW.create(p.elytra().layers().getLast().id(),dev.loomstudios.project.AnimationChannel.ELYTRA,p.animation().durationTicks(),.5F);var tracks=new java.util.ArrayList<>(p.animation().tracks());tracks.add(recipe);return p.withAnimation(p.animation().withTracks(tracks));});
            Screen editor=view==2?new CapeEditorScreen(home):new ElytraEditorScreen(home);client.setScreen(editor);
            var preview=(dev.loomstudios.client.ui.LoomPlayerPreviewWidget)field(editor,"previewWidget").get(editor);
            preview.mouseClicked(mouse(preview.getRight()-32,preview.getY()+10,0),false);
            if(preview.characterVisible())throw new IllegalStateException("Character toggle click failed");
        }else if(view==4||view==5){
            var animation=new dev.loomstudios.client.screen.LoomAnimationScreen(home,dev.loomstudios.project.AnimationChannel.CAPE,ClientProjectWorkspace.project().cape().layers().getFirst().id());
            client.setScreen(animation);set(animation,"layerId",ClientProjectWorkspace.project().cape().layers().get(1).id());set(animation,"preset",dev.loomstudios.project.AnimationPreset.STARS);animation.applyPreset();
            if(view==5){set(animation,"advanced",true);call(animation,"rebuildWidgets");}
            for(var child:animation.children())if(child instanceof AbstractWidget w&&w.getBottom()>animation.height-28)throw new IllegalStateException("Animation control crosses footer: "+w.getMessage());
        }else{
            var preview=new dev.loomstudios.client.screen.LoomPlayerPreviewScreen(home,ClientProjectWorkspace::project);preview.setCharacterVisible(false);client.setScreen(preview);
        }
    }
    private static void verifyUsability(Minecraft client)throws Exception{
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        var editor=new CapeEditorScreen(new LoomHomeScreen());client.setScreen(editor);
        var preview=(dev.loomstudios.client.ui.LoomPlayerPreviewWidget)field(editor,"previewWidget").get(editor);
        preview.mouseClicked(mouse(preview.getRight()-32,preview.getY()+10,0),false);
        if(preview.characterVisible())throw new IllegalStateException("Character toggle failed");
        var state=preview.viewState();preview.setCharacterVisible(true);preview.restoreViewState(state);
        if(preview.characterVisible())throw new IllegalStateException("Character toggle lost on rebuild");
        var world=dev.loomstudios.client.render.LoomPreviewState.extract(client.player);
        if(((dev.loomstudios.client.render.LoomPreviewVisibility)world).loom$characterHidden())throw new IllegalStateException("Preview visibility leaked into player state");
        var animation=new dev.loomstudios.client.screen.LoomAnimationScreen(editor,dev.loomstudios.project.AnimationChannel.CAPE,ClientProjectWorkspace.project().cape().layers().getFirst().id());client.setScreen(animation);
        var before=ClientProjectWorkspace.project().animation();animation.applyPreset();
        if(before.equals(ClientProjectWorkspace.project().animation()))throw new IllegalStateException("Preset did not apply");
        ClientProjectWorkspace.undo();if(!before.equals(ClientProjectWorkspace.project().animation()))throw new IllegalStateException("Preset was not one undoable edit");
        var presetButton=(AbstractWidget)field(animation,"presetButton").get(animation);
        animation.mouseClicked(mouse(presetButton.getX()+8,presetButton.getY()+8,0),false);
        var popup=(dev.loomstudios.client.ui.LoomChoicePopup<?>)field(animation,"choicePopup").get(animation);
        if(popup==null||popup.left()<0||popup.top()<0||popup.left()+popup.width()>animation.width||popup.top()+popup.height()>animation.height-28)throw new IllegalStateException("Dropdown crosses screen bounds");
        String hash=ClientProjectWorkspace.project().hash();
        animation.keyPressed(new net.minecraft.client.input.KeyEvent(256,0,0));
        if(animation.hasChoices()||!hash.equals(ClientProjectWorkspace.project().hash()))throw new IllegalStateException("Dropdown Escape edited project");
        animation.mouseClicked(mouse(presetButton.getX()+8,presetButton.getY()+8,0),false);
        animation.keyPressed(new net.minecraft.client.input.KeyEvent(269,0,0));animation.keyPressed(new net.minecraft.client.input.KeyEvent(257,0,0));
        if(animation.hasChoices()||field(animation,"preset").get(animation)!=dev.loomstudios.project.AnimationPreset.WAVE)throw new IllegalStateException("Dropdown keyboard selection failed");
        var held=client.player.getMainHandItem();var off=client.player.getOffhandItem();
        try{
            client.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            client.player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
            var clean=(net.minecraft.client.renderer.entity.state.AvatarRenderState)dev.loomstudios.client.render.LoomPreviewState.extract(client.player);
            dev.loomstudios.client.render.LoomPreviewState.visibility(clean,false);
            if(!clean.leftHandItemState.isEmpty()||!clean.rightHandItemState.isEmpty()||!clean.leftHandItemStack.isEmpty()||!clean.rightHandItemStack.isEmpty()||!clean.headEquipment.isEmpty()||!clean.legsEquipment.isEmpty()||!clean.feetEquipment.isEmpty())throw new IllegalStateException("Cosmetic-only preview retained inventory layers");
            if(!client.player.getMainHandItem().is(net.minecraft.world.item.Items.DIAMOND_SWORD)||!client.player.getOffhandItem().is(net.minecraft.world.item.Items.SHIELD))throw new IllegalStateException("Preview modified real inventory");
        }finally{client.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,held);client.player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,off);}
        System.out.println(
        "LOOM_UI_CHOICES PASS: bounded dropdown, Escape cancellation, keyboard choice, clean"
            + " hands/armor and unchanged real inventory");
        int action=dev.loomstudios.client.ui.LoomProjectMenu.actionAt(20,20,8,8,9);
        if(action!=0||dev.loomstudios.client.ui.LoomProjectMenu.actionAt(9,9,8,8,9)!=-1)throw new IllegalStateException("Menu inset hit targets incorrect");
        ClientProjectWorkspace.apply(p->{var recipe=dev.loomstudios.project.AnimationPreset.GLOW.create(p.elytra().layers().getLast().id(),dev.loomstudios.project.AnimationChannel.ELYTRA,p.animation().durationTicks(),.5F);var tracks=new java.util.ArrayList<>(p.animation().tracks());tracks.add(recipe);return p.withAnimation(p.animation().withTracks(tracks));});
        var glowProject=ClientProjectWorkspace.project();
        var glowState=dev.loomstudios.client.render.PlayerCosmeticRenderer.withPreviewProjectAtTick(client,glowProject,0,()->dev.loomstudios.client.render.LoomPreviewState.extract(client.player));
        if(dev.loomstudios.client.render.PlayerCosmeticRenderer.getElytraEmissiveTexture((net.minecraft.client.renderer.entity.state.AvatarRenderState)glowState)==null)throw new IllegalStateException("Elytra Glow mask missing");
        var dim=dev.loomstudios.client.render.LoomTextureCompiler.compileAnimated(glowProject,dev.loomstudios.project.AnimationChannel.ELYTRA,0,0,true);
        var bright=dev.loomstudios.client.render.LoomTextureCompiler.compileAnimated(glowProject,dev.loomstudios.project.AnimationChannel.ELYTRA,20,0,true);
        if(java.util.Arrays.equals(dim,bright)||java.util.Arrays.stream(bright).noneMatch(pixel->(pixel>>>24)>0))throw new IllegalStateException("Elytra Glow did not animate");
        System.out.println(
        "LOOM_UI_USABILITY PASS: character click/state isolation, preset application/single undo,"
            + " menu gutters, bounded animation controls and animated Elytra Glow mask");
    }
    private static final String[] POLISH_NAMES={"home-menu-hover","templates-cool","templates-cute","library-favorites-folders","library-bulk","library-organize","library-versions","cape-layers-dense","elytra-layers-dense","cape-layer-groups","elytra-layer-groups","layer-group-name"};
    private static String polishCaptureName(int index){if(index>=176)return "polish-"+new String[]{"bulk-compact","organize-compact","versions-compact","cape-layer-manager-compact","elytra-layer-manager-compact","templates-cute-compact"}[index-176];int[] p=PROFILES[(index-128)/12];return "polish-"+POLISH_NAMES[(index-128)%12]+"-"+p[0]+"x"+p[1]+"-gui"+p[2];}
    private static void preparePolish(Minecraft client)throws Exception{
        if(!Files.exists(fixturePath))LocalProjectLibrary.store().restore(fixtureProject.projectId());
        dev.loomstudios.client.project.LoomPreferences.get().set("previewBackground","SCENIC");
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        var org=new dev.loomstudios.project.LibraryOrganization(dev.loomstudios.client.project.LoomPreferences.get());
        org.organize(java.util.List.of(fixtureProject.projectId()),"Moon collection","space, moon");
        if(!dev.loomstudios.client.project.LoomPreferences.get().favorite(fixtureProject.projectId()))dev.loomstudios.client.project.LoomPreferences.get().toggleFavorite(fixtureProject.projectId());
        dev.loomstudios.client.project.ProjectLibraryIndex.refresh();
        var descriptor=dev.loomstudios.client.project.ProjectLibraryIndex.find(fixtureProject.projectId()).orElseThrow();
        int view=stage>=176?new int[]{4,5,6,9,10,2}[stage-176]:(stage-128)%12;
        var home=new LoomHomeScreen();
        if(view==0){client.setScreen(home);var card=home.children().stream().filter(c->c instanceof dev.loomstudios.client.ui.LoomProjectCard).map(c->(AbstractWidget)c).findFirst().orElseThrow();home.mouseClicked(mouse(card.getX()+12,card.getY()+12,1),false);if(client.screen!=home||field(home,"projectMenu").get(home)==null)throw new IllegalStateException("Home right-click did not open inline menu");}
        else if(view==1||view==2){var screen=new dev.loomstudios.client.screen.LoomTemplatesScreen(home);set(screen,"page",view);client.setScreen(screen);}
        else if(view==3){var screen=new dev.loomstudios.client.screen.LoomLibraryScreen(home);set(screen,"folderFilter","Moon collection");client.setScreen(screen);}
        else if(view==4)client.setScreen(new dev.loomstudios.client.screen.LoomBulkScreen(home,dev.loomstudios.client.project.ProjectLibraryIndex.entries().stream().limit(3).toList(),false));
        else if(view==5)client.setScreen(new dev.loomstudios.client.screen.LoomOrganizeScreen(home,java.util.List.of(descriptor)));
        else if(view==6){var history=new dev.loomstudios.project.ProjectVersions(LocalProjectLibrary.store());for(int i=0;i<3;i++)history.backup(fixtureProject.withName("Moonlit backup "+i));client.setScreen(new dev.loomstudios.client.screen.LoomVersionsScreen(home,descriptor));}
        else if(view==11)client.setScreen(new dev.loomstudios.client.screen.LoomRenameScreen(home,"Layer group","Group name","Assign group","Moon & stars",name->{}));
        else{
            boolean wing=view==8||view==10;
            for(int i=0;i<(wing?6:3);i++){final int n=i;ClientProjectWorkspace.apply(p->wing?dev.loomstudios.project.ProjectEdits.addElytraLayer(p,"Detail "+n):dev.loomstudios.project.ProjectEdits.addCapeLayer(p,"Detail "+n));}
            var layers=(wing?ClientProjectWorkspace.project().elytra():ClientProjectWorkspace.project().cape()).layers();var selected=layers.subList(layers.size()-3,layers.size()).stream().map(dev.loomstudios.project.LoomLayer::id).toList();
            org.group(fixtureProject.projectId(),selected,"Details");
            if(view==9||view==10){var screen=new dev.loomstudios.client.screen.LoomLayerManagerScreen(home,wing,layers.getLast().id());@SuppressWarnings("unchecked") var multi=(java.util.Set<java.util.UUID>)field(screen,"multi").get(screen);multi.addAll(selected);client.setScreen(screen);}
            else{Screen screen=wing?new ElytraEditorScreen(home):new CapeEditorScreen(home);client.setScreen(screen);var list=(AbstractWidget)field(screen,"layerListWidget").get(screen);if(stage<176&&(stage-128)/12==1&&list.getHeight()<92)throw new IllegalStateException("GUI3 layer panel shows fewer than four rows");}
        }
    }
    private static void verifyPolishWorkflows(Minecraft client)throws Exception{
        LocalProjectLibrary.save(fixtureProject);ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        var home=new LoomHomeScreen();client.setScreen(home);var card=home.children().stream().filter(c->c instanceof dev.loomstudios.client.ui.LoomProjectCard).map(c->(AbstractWidget)c).findFirst().orElseThrow();home.mouseClicked(mouse(card.getX()+12,card.getY()+12,1),false);
        if(client.screen!=home)throw new IllegalStateException("Home context actions navigate prematurely");
        home.keyPressed(new net.minecraft.client.input.KeyEvent(256,0,0));if(field(home,"projectMenu").get(home)!=null)throw new IllegalStateException("Home Escape does not dismiss menu");
        call(home,"createGradientTemplate");if(!java.util.Arrays.equals(dev.loomstudios.client.render.LoomTextureCompiler.compile(ClientProjectWorkspace.project().cape(),0,false,false),dev.loomstudios.client.render.LoomTextureCompiler.compile(dev.loomstudios.project.TemplateCatalog.create(dev.loomstudios.project.TemplateCatalog.Kind.GRADIENT,0).cape(),0,false,false)))throw new IllegalStateException("Home gradient action differs from catalog artwork");ClientProjectWorkspace.open(fixturePath,client.player.getUUID());
        var lib=new dev.loomstudios.client.screen.LoomLibraryScreen(home);client.setScreen(lib);Method filtered=lib.getClass().getDeclaredMethod("filtered");filtered.setAccessible(true);@SuppressWarnings("unchecked") var list=(java.util.List<dev.loomstudios.client.project.ProjectDescriptor>)filtered.invoke(lib);
        boolean nonFavorite=false;for(var d:list){boolean favorite=dev.loomstudios.client.project.LoomPreferences.get().favorite(d.projectId());if(favorite&&nonFavorite)throw new IllegalStateException("Favorites are not first");if(!favorite)nonFavorite=true;}
        var cards=lib.children().stream().filter(c->c instanceof dev.loomstudios.client.ui.LoomProjectCard).map(c->(AbstractWidget)c).limit(2).toList();for(var c:cards)lib.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(c.getX()+10,c.getY()+10,new net.minecraft.client.input.MouseButtonInfo(0,2)),false);
        if(((java.util.Set<?>)field(lib,"multi").get(lib)).size()!=2)throw new IllegalStateException("Ctrl-click bulk selection failed");
        call(lib,"bulkActions");if(!(client.screen instanceof dev.loomstudios.client.screen.LoomBulkScreen))throw new IllegalStateException("Bulk action navigation failed");
        var manager=new dev.loomstudios.client.screen.LoomLayerManagerScreen(home,false,fixtureProject.cape().layers().getFirst().id());client.setScreen(manager);
        Method act=manager.getClass().getDeclaredMethod("act",int.class);act.setAccessible(true);act.invoke(manager,0);act.invoke(manager,3);if(ClientProjectWorkspace.project().cape().layers().stream().anyMatch(dev.loomstudios.project.LoomLayer::visible))throw new IllegalStateException("Layer batch hide failed");manager.keyPressed(new net.minecraft.client.input.KeyEvent(90,0,2));if(!ClientProjectWorkspace.project().cape().equals(fixtureProject.cape()))throw new IllegalStateException("Layer batch undo failed");
        var descriptor=dev.loomstudios.client.project.ProjectLibraryIndex.find(fixtureProject.projectId()).orElseThrow();lib.executeAction(descriptor,6);if(!ClientProjectWorkspace.isCurrentProjectEquipped())throw new IllegalStateException("Library Equip failed");
        System.out.println(
        "LOOM_UI_POLISH PASS: inline Home menu/Escape, Home/catalog artwork equivalence,"
            + " favorites-first, Ctrl bulk selection, batch hide/undo, library Equip");
    }
    private static String safetyCaptureName(int index){if(index>=124)return new String[]{"safety-settings-compact","safety-unsaved-compact","safety-undo-delete-compact","safety-palette-management-compact"}[index-124];int[] p=PROFILES[(index-100)/6];return "safety-"+new String[]{"unsaved","delete","recent-design-colors","preview-light","preview-dark","preview-checker"}[(index-100)%6]+"-"+p[0]+"x"+p[1]+"-gui"+p[2];}
    private static void choose(Minecraft client,int index){var w=client.screen.children().stream().filter(c->c instanceof AbstractWidget).map(c->(AbstractWidget)c).toList().get(index);client.screen.mouseClicked(mouse(w.getX()+5,w.getY()+8,0),false);}
    private static void prepareSafety(Minecraft client) throws Exception {
        if(!Files.exists(fixturePath))LocalProjectLibrary.store().restore(fixtureProject.projectId());
        dev.loomstudios.client.project.LoomPreferences.get().set("previewBackground","SCENIC");
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());int view=stage>=124?new int[]{6,0,7,8}[stage-124]:(stage-100)%6;
        if(view==0){var source=new CapeEditorScreen(new LoomHomeScreen());client.setScreen(source);ClientProjectWorkspace.apply(p->p.withName("Moonlit · unsaved artwork"));source.onClose();}
        else if(view==1||view==7){var lib=new dev.loomstudios.client.screen.LoomLibraryScreen(new LoomHomeScreen(),fixtureProject.projectId());client.setScreen(lib);Method act=lib.getClass().getDeclaredMethod("act",int.class);act.setAccessible(true);act.invoke(lib,4);if(view==7)choose(client,0);}
        else if(view==2||view==8){dev.loomstudios.client.palette.EditorColors.use(0xFF22D7E8);dev.loomstudios.client.palette.EditorColors.use(0xFF9B4DFF);var screen=new CapeEditorScreen(new LoomHomeScreen());client.setScreen(screen);call(screen,"togglePaletteWindow");if(view==8){var window=field(screen,"paletteWindow").get(screen);call(window,"toggleManagement");}}
        else if(view==6)client.setScreen(new dev.loomstudios.client.screen.LoomSettingsScreen(new LoomHomeScreen()));
        else {dev.loomstudios.client.project.LoomPreferences.get().set("previewBackground",new String[]{"LIGHT","DARK","CHECKER"}[view-3]);client.setScreen(new dev.loomstudios.client.screen.LoomPlayerPreviewScreen(new LoomHomeScreen(),ClientProjectWorkspace::project));}
        // A deleted fixture is restored on the next capture, keeping profiles isolated.
    }
    private static void verifySafetyWorkflows(Minecraft client) throws Exception {
        if(!Files.exists(fixturePath))LocalProjectLibrary.store().restore(fixtureProject.projectId());
        dev.loomstudios.client.project.LoomPreferences.get().set("previewBackground","SCENIC");
        ClientProjectWorkspace.open(fixturePath,client.player.getUUID());ClientProjectWorkspace.apply(p->p.withName("Safety unsaved"));var screen=new CapeEditorScreen(new LoomHomeScreen());client.setScreen(screen);screen.onClose();
        if(!(client.screen instanceof dev.loomstudios.client.screen.LoomDecisionScreen))throw new IllegalStateException("Editor close bypassed unsaved prompt");choose(client,3);
        if(client.screen!=screen||!ClientProjectWorkspace.isDirty())throw new IllegalStateException("Cancel lost unsaved work");screen.onClose();choose(client,1);
        var draft=dev.loomstudios.client.project.WorkspaceRecovery.STORE.pathFor(fixtureProject.projectId());if(!Files.exists(draft)||!LocalProjectLibrary.store().load(fixturePath).hash().equals(fixtureProject.hash()))throw new IllegalStateException("Keep draft overwrote saved artwork");
        client.setScreen(screen);screen.onClose();choose(client,2);if(ClientProjectWorkspace.isDirty()||Files.exists(draft)||!ClientProjectWorkspace.project().hash().equals(fixtureProject.hash()))throw new IllegalStateException("Discard failed to restore saved baseline");
        ClientProjectWorkspace.apply(p->p.withName("Safety saved"));client.setScreen(screen);screen.onClose();choose(client,0);if(ClientProjectWorkspace.isDirty()||!LocalProjectLibrary.store().load(fixturePath).name().equals("Safety saved"))throw new IllegalStateException("Save and continue did not save");
        var lib=new dev.loomstudios.client.screen.LoomLibraryScreen(new LoomHomeScreen(),fixtureProject.projectId());client.setScreen(lib);Method act=lib.getClass().getDeclaredMethod("act",int.class);act.setAccessible(true);act.invoke(lib,4);choose(client,1);if(!Files.exists(fixturePath))throw new IllegalStateException("Delete cancel erased design");set(lib,"menu",dev.loomstudios.client.project.ProjectLibraryIndex.find(fixtureProject.projectId()).orElseThrow());act.invoke(lib,4);choose(client,0);call(lib,"undoDelete");if(!Files.exists(fixturePath))throw new IllegalStateException("Immediate Undo failed");
        var palette=dev.loomstudios.client.palette.EditorColors.saveDesign();if(palette.colors().isEmpty()||dev.loomstudios.client.palette.ColorPaletteLibrary.find(palette.id()).isEmpty())throw new IllegalStateException("Design palette not persisted");
        var before=ClientProjectWorkspace.project().hash();for(int i=0;i<4;i++)dev.loomstudios.client.ui.LoomPreviewBackground.cycle();if(!before.equals(ClientProjectWorkspace.project().hash()))throw new IllegalStateException("Preview backgrounds changed artwork");
        var report=dev.loomstudios.client.project.LoomDiagnostics.report(client);if(!report.contains("GUI scale setting")||!report.contains("minecraft: 1.21.11")||report.contains(fixturePath.toString())||report.contains(ClientProjectWorkspace.project().name()))throw new IllegalStateException("Diagnostics incomplete or leaked private data");
        System.out.println(
        "LOOM_UI_SAFETY PASS: unsaved cancel/save/keep/discard, delete cancel/Undo, design palette,"
            + " background isolation, private diagnostics");
    }
    private static Object invokeCandidate(Object screen) throws Exception {Method m=screen.getClass().getDeclaredMethod("candidateProject");m.setAccessible(true);return m.invoke(screen);}
    private static Field field(Object object,String name) throws Exception {
        for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass()) {
            try { Field f=type.getDeclaredField(name);f.setAccessible(true);return f; }
            catch(NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }
    private static void set(Object object,String name,Object value) throws Exception { field(object,name).set(object,value); }
    @SuppressWarnings({"unchecked","rawtypes"}) private static Object enumValue(Object object,String name,String value) throws Exception { return Enum.valueOf((Class)field(object,name).getType(),value); }
    private static void call(Object object,String name) throws Exception { Class<?> type=object.getClass();while(type!=null){try{Method m=type.getDeclaredMethod(name);m.setAccessible(true);m.invoke(object);return;}catch(NoSuchMethodException e){type=type.getSuperclass();}}throw new NoSuchMethodException(name); }
}
