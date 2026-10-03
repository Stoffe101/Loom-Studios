package dev.loomstudios.project;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AnimationPresetTest {
    @Test void recipesRemainBoundedEditableAndCodecCompatible(){
        var project=LoomProjectFactory.blank("Presets",1L);
        for(var preset:AnimationPreset.values())for(var channel:AnimationChannel.values())
            for(int duration:new int[]{20,80,200,7200})for(float rate:new float[]{.25F,.5F,1,2}){
                try{
                    var track=preset.create((channel==AnimationChannel.CAPE?project.cape():project.elytra()).layers().getFirst().id(),channel,duration,rate);
                    var animation=new LoomAnimation(duration,true,1,java.util.List.of(track));
                    assertTrue(track.keyframes().size()<=AnimationTrack.MAX_KEYFRAMES);
                    assertEquals(animation,LoomProjectCodec.decode(LoomProjectCodec.encode(project.withAnimation(animation))).animation());
                    assertEquals(track.keyframes().getFirst().value(),track.keyframes().getLast().value());
                }catch(IllegalArgumentException e){assertTrue(duration*rate/20>24*8,"Unexpected invalid recipe: "+e);}
            }
    }
    @Test void starBlinkRateChangesVisibleTiming(){
        UUID layer=UUID.randomUUID();var animation=new LoomAnimation(80,true,1,java.util.List.of());
        var slow=AnimationPreset.STARS.create(layer,AnimationChannel.CAPE,80,.5F);
        var fast=AnimationPreset.STARS.create(layer,AnimationChannel.CAPE,80,1);
        assertEquals(0,AnimationEvaluator.valueAt(slow,animation,15));
        assertEquals(1,AnimationEvaluator.valueAt(fast,animation,15));
        assertEquals(0,AnimationEvaluator.valueAt(fast,animation,20));
    }
    @Test void invalidRatesAreRejected(){
        for(float rate:new float[]{0,Float.NaN,Float.POSITIVE_INFINITY,3})assertThrows(IllegalArgumentException.class,()->AnimationPreset.PULSE.create(UUID.randomUUID(),AnimationChannel.CAPE,80,rate));
    }
}
