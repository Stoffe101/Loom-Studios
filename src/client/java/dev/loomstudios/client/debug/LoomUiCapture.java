package dev.loomstudios.client.debug;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.screen.CapeEditorScreen;
import dev.loomstudios.client.screen.ElytraEditorScreen;
import dev.loomstudios.client.screen.LoomHomeScreen;
import dev.loomstudios.client.screen.LoomCodesScreen;
import dev.loomstudios.client.screen.SmartImportScreen;
import dev.loomstudios.client.project.LocalProjectLibrary;
import dev.loomstudios.client.importing.PngImportAdapter;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.PixelSelection;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

/** Opt-in development capture runner; never runs in packaged or ordinary clients. */
public final class LoomUiCapture {
    private static final boolean ENABLED = FabricLoader.getInstance().isDevelopmentEnvironment()
            && Boolean.getBoolean("loom.uiCapture");
    private static final int[][] PROFILES = {{1920,1080,2}, {1920,1080,3}, {3440,1440,2}, {3440,1440,3}};
    private static int ticks, stage = -2, wait;
    private static boolean pending, prepared, fixturesPrepared, importHandleChecked;
    private static Path fixturePath;
    private static LoomProject fixtureProject;
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
            if (stage >= 182) { pending=true; verifyWorkflows(client); verifyInputAndPreview(client); verifyLibraryWorkflows(client); verifySafetyWorkflows(client); verifyPolishWorkflows(client); System.out.println("LOOM_UI_CAPTURE COMPLETE"); client.stop(); return; }
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
                int[] target = stage == 21 ? new int[]{854,480,2} : stage>=176?new int[]{1904,960,3}:stage>=128?PROFILES[(stage-128)/12]:stage>=124?new int[]{1904,960,3}:stage>=100?PROFILES[(stage-100)/6]:stage >= 96 ? new int[]{1904,960,3} : stage >= 68 ? PROFILES[(stage-68)/7] : stage >= 42 ? new int[]{stage>=57?1904:1920,stage>=57?960:1000,3} : stage >= 22 ? PROFILES[(stage-22)/5] : stage < 8 ? p : PROFILES[1];
                client.options.guiScale().set(target[2]);
                org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.getWindow().handle(), target[0], target[1]);
                client.resizeDisplay(); prepared = true; wait = 10; return;
            }
            if (stage != 20) {
                client.options.guiScale().set(stage == 21 ? 2 : stage>=176?3:stage>=128?PROFILES[(stage-128)/12][2]:stage>=124?3:stage>=100?PROFILES[(stage-100)/6][2]:stage >= 96 ? 3 : stage >= 68 ? PROFILES[(stage-68)/7][2] : stage >= 42 ? 3 : stage >= 22 ? PROFILES[(stage-22)/5][2] : stage < 8 ? p[2] : 3);
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
            if(stage>=128) preparePolish(client);
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
            String name = index>=128?polishCaptureName(index):index>=100?safetyCaptureName(index):index>=96 ? new String[]{"library-context-635x320","library-drafts-635x320","library-trash-635x320","rename-635x320"}[index-96] : index>=68 ? new String[]{"library","templates","settings","cape-animation-studio","elytra-animation-studio","preview-open","preview-gliding"}[(index-68)%7]+"-"+PROFILES[(index-68)/7][0]+"x"+PROFILES[(index-68)/7][1]+"-gui"+PROFILES[(index-68)/7][2] : index>=42 ? new String[]{"cape-windowed-gui3","elytra-windowed-gui3","cape-properties-windowed-gui3","elytra-animation-windowed-gui3","home-windowed-gui3","share-windowed-gui3","smart-import-windowed-gui3","cape-palette-windowed-gui3","elytra-palette-windowed-gui3","circle-live-windowed-gui3","circle-committed-windowed-gui3","cape-middle-pan-windowed-gui3","elytra-middle-pan-windowed-gui3","cape-transparent-guide-windowed-gui3","elytra-transparent-guide-windowed-gui3","cape-635x320-gui3","elytra-properties-635x320-gui3","cape-expanded-635x320-gui3","elytra-expanded-635x320-gui3","circle-filled-live-gui3","circle-filled-committed-gui3","cape-3d-pan-gui3","elytra-3d-pan-gui3","home-3d-pan-gui3","smart-import-3d-pan-gui3","share-3d-pan-gui3"}[index-42] : index >= 22 ? new String[]{"home","share-export","share-import","smart-import-placement","smart-import-processing"}[(index-22)%5] + "-" + PROFILES[(index-22)/5][0]+"x"+PROFILES[(index-22)/5][1]+"-gui"+PROFILES[(index-22)/5][2]
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
        System.out.println("LOOM_UI_INPUT_PREVIEW PASS: Screen canvas/3D/expanded middle pan, palette close, isolated snapshot, cosmetic assets, 20-frame hash/skin reuse and Cape/Elytra alpha isolation");
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
                throw new IllegalStateException("Applied import differs from candidate preview");
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
        System.out.println("LOOM_UI_LIBRARY PASS: right click actions, delete, Trash restore, double click edit, isolated draft recovery and explicit save cleanup");
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
            for(int i=0;i<6;i++){final int n=i;ClientProjectWorkspace.apply(p->wing?dev.loomstudios.project.ProjectEdits.addElytraLayer(p,"Detail "+n):dev.loomstudios.project.ProjectEdits.addCapeLayer(p,"Detail "+n));}
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
        var lib=new dev.loomstudios.client.screen.LoomLibraryScreen(home);client.setScreen(lib);Method filtered=lib.getClass().getDeclaredMethod("filtered");filtered.setAccessible(true);@SuppressWarnings("unchecked") var list=(java.util.List<dev.loomstudios.client.project.ProjectDescriptor>)filtered.invoke(lib);
        boolean nonFavorite=false;for(var d:list){boolean favorite=dev.loomstudios.client.project.LoomPreferences.get().favorite(d.projectId());if(favorite&&nonFavorite)throw new IllegalStateException("Favorites are not first");if(!favorite)nonFavorite=true;}
        var cards=lib.children().stream().filter(c->c instanceof dev.loomstudios.client.ui.LoomProjectCard).map(c->(AbstractWidget)c).limit(2).toList();for(var c:cards)lib.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(c.getX()+10,c.getY()+10,new net.minecraft.client.input.MouseButtonInfo(0,2)),false);
        if(((java.util.Set<?>)field(lib,"multi").get(lib)).size()!=2)throw new IllegalStateException("Ctrl-click bulk selection failed");
        call(lib,"bulkActions");if(!(client.screen instanceof dev.loomstudios.client.screen.LoomBulkScreen))throw new IllegalStateException("Bulk action navigation failed");
        var manager=new dev.loomstudios.client.screen.LoomLayerManagerScreen(home,false,fixtureProject.cape().layers().getFirst().id());client.setScreen(manager);
        Method act=manager.getClass().getDeclaredMethod("act",int.class);act.setAccessible(true);act.invoke(manager,0);act.invoke(manager,3);if(ClientProjectWorkspace.project().cape().layers().stream().anyMatch(dev.loomstudios.project.LoomLayer::visible))throw new IllegalStateException("Layer batch hide failed");manager.keyPressed(new net.minecraft.client.input.KeyEvent(90,0,2));if(!ClientProjectWorkspace.project().cape().equals(fixtureProject.cape()))throw new IllegalStateException("Layer batch undo failed");
        var descriptor=dev.loomstudios.client.project.ProjectLibraryIndex.find(fixtureProject.projectId()).orElseThrow();lib.executeAction(descriptor,6);if(!ClientProjectWorkspace.isCurrentProjectEquipped())throw new IllegalStateException("Library Equip failed");
        System.out.println("LOOM_UI_POLISH PASS: inline Home menu/Escape, favorites-first, Ctrl bulk selection, batch hide/undo, library Equip");
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
        System.out.println("LOOM_UI_SAFETY PASS: unsaved cancel/save/keep/discard, delete cancel/Undo, design palette, background isolation, private diagnostics");
    }
    private static Object invokeCandidate(Object screen) throws Exception {Method m=screen.getClass().getDeclaredMethod("candidateProject");m.setAccessible(true);return m.invoke(screen);}
    private static Field field(Object object,String name) throws Exception { Field f=object.getClass().getDeclaredField(name); f.setAccessible(true); return f; }
    private static void set(Object object,String name,Object value) throws Exception { field(object,name).set(object,value); }
    @SuppressWarnings({"unchecked","rawtypes"}) private static Object enumValue(Object object,String name,String value) throws Exception { return Enum.valueOf((Class)field(object,name).getType(),value); }
    private static void call(Object object,String name) throws Exception { Method m=object.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(object); }
}
