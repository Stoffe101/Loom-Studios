package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AnimationPresetBatchTest {
  private LoomProject triple() {
    var p=LoomProjectFactory.blank("Three stars",4);
    p=ProjectEdits.addCapeLayer(p,"Cloud");
    return ProjectEdits.addCapeLayer(p,"Moon");
  }

  @Test void threeSelectedLayersGetThreeIndependentTracksAndSourceNeverChanges() {
    var source=triple();
    List<UUID> ids=source.cape().layers().stream().map(LoomLayer::id).toList();
    var original=source.encode();
    var preview=AnimationPresetBatch.compose(source,AnimationChannel.CAPE,
        ids,AnimationPreset.PULSE,.5f,source.animation().loop());
    assertArrayEquals(original,source.encode(),"Try must never mutate the source project");
    assertEquals(3,preview.animation().tracks().size());
    for(int i=0;i<3;i++){
      assertEquals(ids.get(i),preview.animation().tracks().get(i).layerId());
      assertEquals(AnimationChannel.CAPE,preview.animation().tracks().get(i).channel());
    }
    assertEquals(3,preview.animation().tracks().stream().map(AnimationTrack::id).distinct().count());
    assertEquals(preview,LoomProjectCodec.decode(preview.encode()));
    assertEquals(preview,LoomProjectCode.decodePortable(LoomProjectCode.encodePortable(preview)));
  }

  @Test void repeatedBatchPreservesPriorAdvancedTracksRatherThanOverwritingThem() {
    var source=triple();
    UUID first=source.cape().layers().getFirst().id();
    var advanced=AnimationAuthoring.addTrack(source.animation(),first,
        AnimationChannel.CAPE,AnimationEffectType.SCROLL);
    var original=advanced.tracks().getFirst();
    source=source.withAnimation(advanced);
    var preview=AnimationPresetBatch.compose(source,AnimationChannel.CAPE,
        source.cape().layers().stream().map(LoomLayer::id).toList(),
        AnimationPreset.STARS,1f,true);
    assertEquals(4,preview.animation().tracks().size());
    assertEquals(original,preview.animation().tracks().getFirst());
  }

  @Test void rejectsInvalidAndDuplicateSelectionsBeforeConstructingPartialDrafts() {
    var source=triple();
    UUID first=source.cape().layers().getFirst().id();
    assertThrows(IllegalArgumentException.class,()->AnimationPresetBatch.compose(source,
        AnimationChannel.CAPE,List.of(),AnimationPreset.PULSE,1f,true));
    assertThrows(IllegalArgumentException.class,()->AnimationPresetBatch.compose(source,
        AnimationChannel.CAPE,List.of(first,first),AnimationPreset.PULSE,1f,true));
    assertThrows(IllegalArgumentException.class,()->AnimationPresetBatch.compose(source,
        AnimationChannel.ELYTRA,List.of(first),AnimationPreset.PULSE,1f,true));
    assertThrows(IllegalArgumentException.class,()->AnimationPresetBatch.compose(source,
        AnimationChannel.CAPE,List.of(first),AnimationPreset.PULSE,Float.NaN,true));
    assertEquals(0,source.animation().tracks().size());
  }
}
