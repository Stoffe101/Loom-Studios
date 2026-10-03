package dev.loomstudios.project;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Friendly recipes compile to ordinary editable tracks; existing projects/codecs stay compatible. */
public enum AnimationPreset {
    GLOW("Glow", AnimationEffectType.EMISSIVE_GLOW, "Soft glowing highlights. Best on a separate detail layer."),
    PULSE("Pulse", AnimationEffectType.PULSE, "Fade the selected layer gently in and out."),
    SHIMMER("Shimmer", AnimationEffectType.SPARKLE, "Twinkling pixels. Use a highlight layer; keep the base solid."),
    STARS("Blinking stars", AnimationEffectType.PULSE, "Blinks the selected star/detail layer. Draw stars on their own layer first."),
    HUE("Color cycle", AnimationEffectType.HUE_SHIFT, "Cycle the selected layer through the rainbow."),
    SCROLL("Horizontal scroll", AnimationEffectType.SCROLL, "Move the selected pattern sideways and wrap it around."),
    WAVE("Vertical wave", AnimationEffectType.MOVING_GRADIENT, "Move the selected pattern vertically and wrap it around.");

    private final String label, description;
    private final AnimationEffectType effect;
    AnimationPreset(String label,AnimationEffectType effect,String description){this.label=label;this.effect=effect;this.description=description;}
    public String label(){return label;}
    public String description(){return description;}
    public AnimationEffectType effect(){return effect;}
    public AnimationPreset next(){return values()[(ordinal()+1)%values().length];}

    public AnimationTrack create(UUID layer,AnimationChannel channel,int duration,float cyclesPerSecond){
        if(!Float.isFinite(cyclesPerSecond)||cyclesPerSecond<.25F||cyclesPerSecond>2F)throw new IllegalArgumentException("Rate must be between 0.25 and 2 cycles per second");
        int cycles=Math.max(1,Math.min(24,(int)Math.floor(duration*cyclesPerSecond/20)));
        float speed=duration*cyclesPerSecond/(20*cycles);
        if(speed>8)throw new IllegalArgumentException("Shorten the timeline or choose a slower preset rate");
        List<AnimationKeyframe> frames=new ArrayList<>();
        for(int c=0;c<cycles;c++){
            int start=Math.round(c*duration/(float)cycles),end=Math.round((c+1)*duration/(float)cycles),middle=(start+end)/2;
            frames.add(new AnimationKeyframe(start,low()));
            if(this==STARS){frames.add(new AnimationKeyframe(middle-1,low()));frames.add(new AnimationKeyframe(middle,1));frames.add(new AnimationKeyframe(end-1,1));}
            else if(this==HUE||this==SCROLL||this==WAVE){frames.add(new AnimationKeyframe(end-1,1));}
            else frames.add(new AnimationKeyframe(middle,1));
        }
        frames.add(new AnimationKeyframe(duration,low()));
        return new AnimationTrack(UUID.randomUUID(),layer,channel,effect,true,speed,true,frames);
    }
    private float low(){return switch(this){case GLOW->.2F;case PULSE->.5F;case SHIMMER->.65F;default->0;};}
}
