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
    assertThrows(IllegalStateException.class,()->AnimationPresetDraft.compose(
        p,id,AnimationChannel.CAPE,original.id(),AnimationPreset.STARS,1f));
    assertEquals(before,p,"Simple must not flatten custom Advanced parameter lanes");
    assertEquals(original,before.animation().tracks().getFirst());
    assertEquals(before,LoomProjectCodec.decode(before.encode()));
  }

  @Test void basicPresetReplacementKeepsTrackIdentityAndIsSerializable() {
    var p=LoomProjectFactory.blank("Recipe change",4);
    UUID id=p.cape().layers().getFirst().id();
    p=p.withAnimation(AnimationAuthoring.addTrack(
        p.animation(),id,AnimationChannel.CAPE,AnimationEffectType.PULSE));
    var old=p.animation().tracks().getFirst();
    var draft=AnimationPresetDraft.compose(p,id,AnimationChannel.CAPE,
        old.id(),AnimationPreset.SHIMMER,.5f);
    assertEquals(old.id(),draft.animation().tracks().getFirst().id());
    assertEquals(AnimationEffectType.SPARKLE,draft.animation().tracks().getFirst().effect());
    assertEquals(p,LoomProjectCodec.decode(p.encode()));
    assertEquals(draft,LoomProjectCodec.decode(draft.encode()));
  }

  @Test void loopToggleIsOnlyPresentInDraftUntilExplicitApply() {
    var source=LoomProjectFactory.blank("Preview loop",9);
    UUID id=source.cape().layers().getFirst().id();
    boolean before=source.animation().loop();
    var temporary=AnimationPresetDraft.compose(source,id,AnimationChannel.CAPE,
        null,AnimationPreset.STARS,1f,!before);
    assertEquals(before,source.animation().loop(),"Try must not persist loop settings");
    assertEquals(!before,temporary.animation().loop());
    assertEquals(source,LoomProjectCodec.decode(source.encode()));
    assertEquals(temporary,LoomProjectCodec.decode(temporary.encode()));
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
