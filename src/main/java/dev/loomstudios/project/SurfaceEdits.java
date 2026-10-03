package dev.loomstudios.project;
import java.util.UUID;
import java.util.function.UnaryOperator;
/** Preserves unrelated UV faces and immutable locked/typed layer contracts. */
public final class SurfaceEdits {
    private SurfaceEdits(){}
    public static PixelPatch cape(LoomProject p,UUID id,CapeUvRegion face){return read(p.cape(),id,face.atlasX(0,p.cape().width()/64),face.atlasY(0,p.cape().width()/64),face.width(p.cape().width()/64),face.height(p.cape().width()/64));}
    public static PixelPatch wing(LoomProject p,UUID id,ElytraWing wing){int s=p.elytra().width()/64;return read(p.elytra(),id,wing.atlasX(0,s),wing.atlasY(0,s),wing.width(s),wing.height(s));}
    private static LoomLayer layer(LoomCanvas c,UUID id){return c.layers().stream().filter(l->l.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Missing layer"));}
    private static PixelPatch read(LoomCanvas c,UUID id,int ax,int ay,int w,int h){LoomLayer l=layer(c,id);if(!l.editableAsPaint())throw new IllegalArgumentException("Select an unlocked Paint layer");int[] data=l.pixels(),out=new int[w*h];for(int y=0;y<h;y++)System.arraycopy(data,(ay+y)*c.width()+ax,out,y*w,w);return new PixelPatch(w,h,out);}
    public static LoomProject cape(LoomProject p,UUID id,CapeUvRegion face,UnaryOperator<PixelPatch> change){int s=p.cape().width()/64;PixelPatch before=cape(p,id,face),after=change.apply(before);return p.withCape(write(p.cape(),id,face.atlasX(0,s),face.atlasY(0,s),before,after,null,false));}
    public static LoomProject wing(LoomProject p,UUID id,ElytraWing wing,boolean linked,UnaryOperator<PixelPatch> change){int s=p.elytra().width()/64;PixelPatch before=wing(p,id,wing),after=change.apply(before);return p.withElytra(write(p.elytra(),id,wing.atlasX(0,s),wing.atlasY(0,s),before,after,wing,linked));}
    private static LoomCanvas write(LoomCanvas c,UUID id,int ax,int ay,PixelPatch before,PixelPatch after,ElytraWing wing,boolean linked){if(after.width()!=before.width()||after.height()!=before.height())throw new IllegalArgumentException("Face size cannot change");LoomLayer layer=layer(c,id);int[] p=layer.pixels(),a=after.data(),b=before.data();boolean changed=false;int s=c.width()/64;
        for(int y=0;y<before.height();y++)for(int x=0;x<before.width();x++){int i=y*before.width()+x;if(a[i]==b[i])continue;changed=true;p[(ay+y)*c.width()+ax+x]=a[i];if(linked){ElytraWing other=wing.opposite();p[other.atlasY(y,s)*c.width()+other.atlasX(other.mirroredLocalX(x,s),s)]=a[i];}}
        return changed?c.replaceLayer(id,layer.withPixels(p)):c;
    }
}
