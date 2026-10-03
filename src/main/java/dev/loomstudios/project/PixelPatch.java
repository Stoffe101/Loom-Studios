package dev.loomstudios.project;

/** Small immutable semantic-face image used by drawing and the internal pixel clipboard. */
public record PixelPatch(int width,int height,int[] data) {
    public PixelPatch { if(width<1||height<1||width>256||height>256||data.length!=width*height)throw new IllegalArgumentException("Invalid pixel patch");data=data.clone(); }
    @Override public int[] data(){return data.clone();}
    public PixelPatch crop(PixelSelection s){validate(s);int[] out=new int[s.width()*s.height()];for(int y=0;y<s.height();y++)System.arraycopy(data,(s.minY()+y)*width+s.minX(),out,y*s.width(),s.width());return new PixelPatch(s.width(),s.height(),out);}
    public PixelPatch paste(PixelPatch patch,int x,int y){if(x<0||y<0||x+patch.width>width||y+patch.height>height)throw new IllegalArgumentException("Selection does not fit this face");int[] out=data.clone();for(int row=0;row<patch.height;row++)System.arraycopy(patch.data,row*patch.width,out,(y+row)*width+x,patch.width);return new PixelPatch(width,height,out);}
    public PixelPatch clear(PixelSelection s){validate(s);int[] out=data.clone();for(int y=s.minY();y<=s.maxY();y++)java.util.Arrays.fill(out,y*width+s.minX(),y*width+s.maxX()+1,0);return new PixelPatch(width,height,out);}
    private void validate(PixelSelection s){if(s.minX()<0||s.minY()<0||s.maxX()>=width||s.maxY()>=height)throw new IllegalArgumentException("Selection does not fit this face");}
    public PixelPatch rotate(){int[] out=new int[data.length];for(int y=0;y<height;y++)for(int x=0;x<width;x++)out[x*height+height-1-y]=data[y*width+x];return new PixelPatch(height,width,out);}
    public PixelPatch flip(boolean horizontal){int[] out=new int[data.length];for(int y=0;y<height;y++)for(int x=0;x<width;x++)out[(horizontal?y:height-1-y)*width+(horizontal?width-1-x:x)]=data[y*width+x];return new PixelPatch(width,height,out);}
}
