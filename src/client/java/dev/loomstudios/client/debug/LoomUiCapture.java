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
            if (stage >= 42) { verifyWorkflows(client); System.out.println("LOOM_UI_CAPTURE COMPLETE"); client.stop(); return; }
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
                int[] target = stage == 21 ? new int[]{854,480,2} : stage >= 22 ? PROFILES[(stage-22)/5] : stage < 8 ? p : PROFILES[1];
                client.options.guiScale().set(target[2]);
                org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.getWindow().handle(), target[0], target[1]);
                client.resizeDisplay(); prepared = true; wait = 10; return;
            }
            if (stage != 20) {
                client.options.guiScale().set(stage == 21 ? 2 : stage >= 22 ? PROFILES[(stage-22)/5][2] : stage < 8 ? p[2] : 3);
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
            if (stage >= 22) {
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
                if (w.getX() < 0 || w.getY() < 0 || w.getRight() > client.screen.width || w.getBottom() > client.screen.height - 20)
                    throw new IllegalStateException("Out of bounds: " + w.getClass().getSimpleName() + " " + w.getMessage().getString());
            }
            var widgets=client.screen.children().stream().filter(c -> c instanceof AbstractWidget w && w.visible).map(c -> (AbstractWidget)c).toList();
            for(int a=0;a<widgets.size();a++) for(int b=a+1;b<widgets.size();b++) {
                var x=widgets.get(a); var y=widgets.get(b);
                if(x.getX()<y.getRight() && x.getRight()>y.getX() && x.getY()<y.getBottom() && x.getBottom()>y.getY())
                    throw new IllegalStateException("Overlapping controls: "+x.getMessage().getString()+" / "+y.getMessage().getString());
            }
            Path dir = Path.of("../docs/verification/editor-workspace"); Files.createDirectories(dir);
            String name = index >= 22 ? new String[]{"home","share-export","share-import","smart-import-placement","smart-import-processing"}[(index-22)%5] + "-" + PROFILES[(index-22)/5][0]+"x"+PROFILES[(index-22)/5][1]+"-gui"+PROFILES[(index-22)/5][2]
                    : index < 8 ? (index % 2 == 0 ? "cape" : "elytra") + "-" + PROFILES[index/2][0] + "x" + PROFILES[index/2][1] + "-gui" + PROFILES[index/2][2]
                    : new String[]{"cape-color-compact","cape-properties-compact","elytra-animation-compact","elytra-playback-compact","cape-gradient-compact","cape-transform-compact","cape-stops-compact","elytra-properties-compact","elytra-color-compact","cape-many-layers-compact","elytra-many-tracks-compact","cape-single-pixel-live-200percent","cape-single-pixel-committed-200percent","cape-small-window-guidance"}[index-8];
            Screenshot.takeScreenshot(client.getMainRenderTarget(), image -> {
                try { image.writeToFile(dir.resolve(name+".png")); System.out.println("LOOM_UI_CAPTURE " + name + " " + client.screen.width + "x" + client.screen.height); }
                catch(Exception ex) { ex.printStackTrace(); } finally { image.close(); stage++; wait=5; pending=false; prepared=false; }
            });
        } catch (Exception ex) { ex.printStackTrace(); client.stop(); }
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
    private static Field field(Object object,String name) throws Exception { Field f=object.getClass().getDeclaredField(name); f.setAccessible(true); return f; }
    private static void set(Object object,String name,Object value) throws Exception { field(object,name).set(object,value); }
    @SuppressWarnings({"unchecked","rawtypes"}) private static Object enumValue(Object object,String name,String value) throws Exception { return Enum.valueOf((Class)field(object,name).getType(),value); }
    private static void call(Object object,String name) throws Exception { Method m=object.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(object); }
}
