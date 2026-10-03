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
    private static boolean pending, prepared, fixturesPrepared;
    private static Path fixturePath;
    private static LoomProject fixtureProject;
    private LoomUiCapture() {}
    public static void tick(Minecraft client) {
        if (!ENABLED || pending) return;
        try {
            if (++ticks > 6000) throw new IllegalStateException("Capture timed out");
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
            if (stage >= 100) { pending=true; verifyWorkflows(client); verifyInputAndPreview(client); verifyLibraryWorkflows(client); System.out.println("LOOM_UI_CAPTURE COMPLETE"); client.stop(); return; }
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
                int[] target = stage == 21 ? new int[]{854,480,2} : stage >= 96 ? new int[]{1904,960,3} : stage >= 68 ? PROFILES[(stage-68)/7] : stage >= 42 ? new int[]{stage>=57?1904:1920,stage>=57?960:1000,3} : stage >= 22 ? PROFILES[(stage-22)/5] : stage < 8 ? p : PROFILES[1];
                client.options.guiScale().set(target[2]);
                org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.getWindow().handle(), target[0], target[1]);
                client.resizeDisplay(); prepared = true; wait = 10; return;
            }
            if (stage != 20) {
                client.options.guiScale().set(stage == 21 ? 2 : stage >= 96 ? 3 : stage >= 68 ? PROFILES[(stage-68)/7][2] : stage >= 42 ? 3 : stage >= 22 ? PROFILES[(stage-22)/5][2] : stage < 8 ? p[2] : 3);
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
            if(stage>=68) prepareMilestone(client);
            client.screen.setFocused(null);
            org.lwjgl.glfw.GLFW.glfwSetCursorPos(client.getWindow().handle(),2,2);
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
            String name = index>=96 ? new String[]{"library-context-635x320","library-drafts-635x320","library-trash-635x320","rename-635x320"}[index-96] : index>=68 ? new String[]{"library","templates","settings","cape-animation-studio","elytra-animation-studio","preview-open","preview-gliding"}[(index-68)%7]+"-"+PROFILES[(index-68)/7][0]+"x"+PROFILES[(index-68)/7][1]+"-gui"+PROFILES[(index-68)/7][2] : index>=42 ? new String[]{"cape-windowed-gui3","elytra-windowed-gui3","cape-properties-windowed-gui3","elytra-animation-windowed-gui3","home-windowed-gui3","share-windowed-gui3","smart-import-windowed-gui3","cape-palette-windowed-gui3","elytra-palette-windowed-gui3","circle-live-windowed-gui3","circle-committed-windowed-gui3","cape-middle-pan-windowed-gui3","elytra-middle-pan-windowed-gui3","cape-transparent-guide-windowed-gui3","elytra-transparent-guide-windowed-gui3","cape-635x320-gui3","elytra-properties-635x320-gui3","cape-expanded-635x320-gui3","elytra-expanded-635x320-gui3","circle-filled-live-gui3","circle-filled-committed-gui3","cape-3d-pan-gui3","elytra-3d-pan-gui3","home-3d-pan-gui3","smart-import-3d-pan-gui3","share-3d-pan-gui3"}[index-42] : index >= 22 ? new String[]{"home","share-export","share-import","smart-import-placement","smart-import-processing"}[(index-22)%5] + "-" + PROFILES[(index-22)/5][0]+"x"+PROFILES[(index-22)/5][1]+"-gui"+PROFILES[(index-22)/5][2]
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
        if(!(client.screen instanceof CapeEditorScreen)||!ClientProjectWorkspace.project().projectId().equals(project.projectId()))throw new IllegalStateException("Double click did not select and edit design");
        ClientProjectWorkspace.apply(p->p.withName("Recovered draft"));dev.loomstudios.client.project.WorkspaceRecovery.checkpoint();
        if(!LocalProjectLibrary.store().load(path).hash().equals(project.hash()))throw new IllegalStateException("Autosave overwrote explicit save");
        var draft=dev.loomstudios.client.project.WorkspaceRecovery.STORE.pathFor(project.projectId());
        ClientProjectWorkspace.recover(draft,client.player.getUUID());
        if(!ClientProjectWorkspace.isDirty()||!ClientProjectWorkspace.project().name().equals("Recovered draft"))throw new IllegalStateException("Draft recovery lost edits");
        ClientProjectWorkspace.save();
        if(Files.exists(draft))throw new IllegalStateException("Explicit save retained stale recovery draft");
        System.out.println("LOOM_UI_LIBRARY PASS: right click actions, delete, Trash restore, double click edit, isolated draft recovery and explicit save cleanup");
    }
    private static Field field(Object object,String name) throws Exception { Field f=object.getClass().getDeclaredField(name); f.setAccessible(true); return f; }
    private static void set(Object object,String name,Object value) throws Exception { field(object,name).set(object,value); }
    @SuppressWarnings({"unchecked","rawtypes"}) private static Object enumValue(Object object,String name,String value) throws Exception { return Enum.valueOf((Class)field(object,name).getType(),value); }
    private static void call(Object object,String name) throws Exception { Method m=object.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(object); }
}
