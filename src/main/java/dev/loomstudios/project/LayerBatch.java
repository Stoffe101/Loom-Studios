package dev.loomstudios.project;
import java.util.*;
public final class LayerBatch {
    public enum Action { SHOW,HIDE,LOCK,UNLOCK,DUPLICATE,DELETE,UP,DOWN }
    private LayerBatch(){}
    public static LoomProject apply(LoomProject p,boolean wing,Set<UUID> ids,Action action){
        List<LoomLayer> layers=(wing?p.elytra():p.cape()).layers();Set<UUID> valid=new HashSet<>(ids);valid.retainAll(layers.stream().map(LoomLayer::id).toList());
        if(action==Action.DELETE&&valid.size()>=layers.size())throw new IllegalArgumentException("Keep at least one layer");
        if(action==Action.DUPLICATE&&layers.size()+valid.size()>LoomProjectCodec.MAX_LAYER_COUNT)throw new IllegalArgumentException("Layer limit reached");
        List<LoomLayer> ordered=new ArrayList<>(layers);if(action==Action.UP)Collections.reverse(ordered);
        for(LoomLayer layer:ordered)if(valid.contains(layer.id()))p=switch(action){
            case SHOW,HIDE->wing?ProjectEdits.setElytraLayerVisible(p,layer.id(),action==Action.SHOW):ProjectEdits.setCapeLayerVisible(p,layer.id(),action==Action.SHOW);
            case LOCK,UNLOCK->wing?ProjectEdits.setElytraLayerLocked(p,layer.id(),action==Action.LOCK):ProjectEdits.setCapeLayerLocked(p,layer.id(),action==Action.LOCK);
            case DUPLICATE->wing?ProjectEdits.duplicateElytraLayer(p,layer.id()):ProjectEdits.duplicateCapeLayer(p,layer.id());
            case DELETE->wing?ProjectEdits.removeElytraLayer(p,layer.id()):ProjectEdits.removeCapeLayer(p,layer.id());
            case UP,DOWN->{int index=0;var list=(wing?p.elytra():p.cape()).layers();for(int i=0;i<list.size();i++)if(list.get(i).id().equals(layer.id()))index=i;int next=index+(action==Action.UP?1:-1);if(next>=0&&next<list.size()&&!valid.contains(list.get(next).id()))yield wing?ProjectEdits.moveElytraLayer(p,layer.id(),action==Action.UP?1:-1):ProjectEdits.moveCapeLayer(p,layer.id(),action==Action.UP?1:-1);yield p;}
        };return p;
    }
}
