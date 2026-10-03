package dev.loomstudios.client.project;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.util.ArrayDeque;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.Pattern;

/** Opt-in copy only: no paths, chat, project pixels, usernames or raw log messages. */
public final class LoomDiagnostics {
    private static final ArrayDeque<String> errors=new ArrayDeque<>();
    private LoomDiagnostics() { }
    public static void record(String action,Exception e){if(errors.size()>=8)errors.removeFirst();errors.addLast(action+": "+e.getClass().getSimpleName());}
    private static String version(String id){return FabricLoader.getInstance().getModContainer(id).map(m->m.getMetadata().getVersion().getFriendlyString()).orElse("not installed");}
    public static String report(Minecraft client){
        StringBuilder s=new StringBuilder("Loom Studios diagnostics\n");
        for(String id:new String[]{"loom-studios","minecraft","fabricloader","fabric-api","sodium","sodium-extra","iris","skinlayers3d"})s.append(id).append(": ").append(version(id)).append('\n');
        s.append("Java: ").append(System.getProperty("java.version")).append("\nOS: ").append(System.getProperty("os.name"));
        s.append("\nDisplay: ").append(client.getWindow().getWidth()).append('x').append(client.getWindow().getHeight());
        s.append("\nGUI scale setting: ").append(client.options.guiScale().get()).append("\nScreen: ").append(client.screen==null?"none":client.screen.getClass().getSimpleName());
        s.append("\nWorkspace dirty: ").append(ClientProjectWorkspace.isInitialized()&&ClientProjectWorkspace.isDirty());
        s.append("\nPreview background: ").append(LoomPreferences.get().choice("previewBackground","SCENIC"));
        s.append("\nRecent action errors (classifications only):\n");for(String error:errors)s.append(error).append('\n');
        try(var channel=Files.newByteChannel(FabricLoader.getInstance().getGameDir().resolve("logs/latest.log"),StandardOpenOption.READ)){
            long end=channel.size();channel.position(Math.max(0,end-65536));ByteBuffer b=ByteBuffer.allocate((int)Math.min(65536,end));channel.read(b);b.flip();
            Pattern exception=Pattern.compile("[A-Za-z_$][A-Za-z0-9_$.]*(?:Exception|Error)\\b");int count=0;
            for(String line:StandardCharsets.UTF_8.decode(b).toString().split("\\R"))if(line.toLowerCase(java.util.Locale.ROOT).contains("loom")&&(line.contains("ERROR")||line.contains("WARN"))){var m=exception.matcher(line);if(m.find()&&count++<8)s.append("Log exception class: ").append(m.group()).append('\n');}
        }catch(Exception e){s.append("Recent log classifications unavailable\n");}
        return s.append("No raw logs, account data or design content included.\n").toString();
    }
    public static void copy(Minecraft client){client.keyboardHandler.setClipboard(report(client));}
}
