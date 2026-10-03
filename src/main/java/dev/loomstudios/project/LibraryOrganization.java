package dev.loomstudios.project;

import java.io.IOException;
import java.util.*;

/** Local organization never changes exported artwork or runtime composition. */
public final class LibraryOrganization {
    private final EditorPreferences prefs;
    public LibraryOrganization(EditorPreferences prefs){this.prefs=prefs;}
    public String folder(UUID id){return prefs.choice("folder."+id,"");}
    public List<String> tags(UUID id){String s=prefs.choice("tags."+id,"");return s.isBlank()?List.of():List.of(s.split(","));}
    private static String label(String name,int limit){String s=name.strip();if(s.length()>limit||s.chars().anyMatch(c->Character.isISOControl(c)||c==','))throw new IllegalArgumentException("Use short names without commas or control characters");return s;}
    public void folder(UUID id,String name)throws IOException{prefs.set("folder."+id,label(name,48));}
    private static String normalizedTags(String names){TreeSet<String> tags=new TreeSet<>();for(String s:names.split(","))if(!s.isBlank())tags.add(label(s.toLowerCase(Locale.ROOT),24));if(tags.size()>12)throw new IllegalArgumentException("Maximum 12 tags");return String.join(",",tags);}
    public void tags(UUID id,String names)throws IOException{prefs.set("tags."+id,normalizedTags(names));}
    public void organize(Collection<UUID> ids,String folder,String tags)throws IOException{String f=label(folder,48),t=normalizedTags(tags);Map<String,String> updates=new HashMap<>();for(UUID id:ids){updates.put("folder."+id,f);updates.put("tags."+id,t);}prefs.setAll(updates);}
    public boolean matches(UUID id,String name,String query){String text=name+" "+folder(id)+" "+String.join(" ",tags(id));return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));}
    public String group(UUID project,UUID layer){return prefs.choice("group."+project+"."+layer,"");}
    public void group(UUID project,Collection<UUID> layers,String name)throws IOException{String s=label(name,32);Map<String,String> updates=new HashMap<>();for(UUID id:layers)updates.put("group."+project+"."+id,s);prefs.setAll(updates);}
}
