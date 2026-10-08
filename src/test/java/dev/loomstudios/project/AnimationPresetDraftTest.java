package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class AnimationPresetDraftTest {
  @Test void everySimplePreviewIsPureRoundTrippableAndTargetsOnlyChosenLayer() {
    for (var preset: AnimationPreset.values()) {
      LoomProject source=LoomProjectFactory.blank("Try is read only",2);
      UUID id=source.cape().layers().getFirst().id();
      byte[] before=source.encode();
      var draft=AnimationPresetDraft.compose(source,id,AnimationChannel.CAPE,null,preset,.5f);
      assertArrayEquals(before,source.encode(),"Try must not alter source for "+preset);
      assertNotEquals(source.animation(),draft.animation());
      assertEquals(1,draft.animation().tracks().size());
      assertEquals(id,draft.animation().tracks().getFirst().layerId());
      assertEquals(preset.effect(),draft.animation().tracks().getFirst().effect());
      assertEquals(draft,LoomProjectCodec.decode(draft.encode()));
      assertEquals(draft,LoomProjectCode.decodePortable(LoomProjectCode.encodePortable(draft)));
    }
  }

  @Test void previewUsesSelectedTrackIdentityAndLeavesAdvancedSourceDataUnmodified() {
    var p=LoomProjectFactory.blank("Custom lane",3);
    UUID id=p.cape().layers().getFirst().id();
    var a=AnimationAuthoring.addTrack(p.animation(),id,AnimationChannel.CAPE,AnimationEffectType.SCROLL);
    var original=a.tracks().getFirst().withLanes(List.of(new ParameterLane(
        AnimationParameter.DIRECTION_X,List.of(
            new AnimationKeyframe(0,0),new AnimationKeyframe(80,1)))));
    p=p.withAnimation(a.withTracks(List.of(original)));
    var before=p;
    var draft=AnimationPresetDraft.compose(p,id,AnimationChannel.CAPE,
        original.id(),AnimationPreset.STARS,1f);
    assertEquals(before,p);
    assertEquals(original,before.animation().tracks().getFirst());
    assertEquals(1,draft.animation().tracks().size());
    assertEquals(original.id(),draft.animation().tracks().getFirst().id());
    assertEquals(AnimationEffectType.PULSE,draft.animation().tracks().getFirst().effect());
    assertTrue(draft.animation().tracks().getFirst().lanes().isEmpty(),
        "Applying explicitly replaces the prior custom effect but preview cannot mutate it");
    assertEquals(before,LoomProjectCodec.decode(before.encode()));
    assertEquals(draft,LoomProjectCodec.decode(draft.encode()));
  }

  @Test void illegalLayerAndRatesRejectWithoutChangingOriginal() {
    var p=LoomProjectFactory.blank("Safe",3);
    UUID id=p.cape().layers().getFirst().id();
    assertThrows(IllegalArgumentException.class,()->AnimationPresetDraft.compose(p,
        UUID.randomUUID(),AnimationChannel.CAPE,null,AnimationPreset.STARS,.5f));
    assertThrows(IllegalArgumentException.class,()->AnimationPresetDraft.compose(p,
        id,AnimationChannel.CAPE,null,AnimationPreset.STARS,0));
    assertThrows(IllegalArgumentException.class,()->AnimationPresetDraft.compose(p,
        id,AnimationChannel.CAPE,null,AnimationPreset.STARS,Float.NaN));
    assertTrue(p.animation().tracks().isEmpty());
  }
}
