package dev.loomstudios.project;

import java.util.*;

/** Lane values are normalized [0,1], so angle/color/integer lanes share one bounded key format. */
public enum AnimationParameter {
  MIN_OPACITY("Min opacity", 0, 1),
  MAX_OPACITY("Max opacity", 0, 1),
  DIRECTION_X("Direction X", -1, 1),
  DIRECTION_Y("Direction Y", -1, 1),
  DISTANCE("Distance", 0, 4),
  ANGLE("Angle", -360, 360),
  WIDTH("Width", .01f, 4),
  OFFSET("Offset", -4, 4),
  FIRST_R("Color A red", 0, 255),
  FIRST_G("Color A green", 0, 255),
  FIRST_B("Color A blue", 0, 255),
  SECOND_R("Color B red", 0, 255),
  SECOND_G("Color B green", 0, 255),
  SECOND_B("Color B blue", 0, 255),
  DENSITY("Density", 0, 1),
  SEED("Seed", 0, 65535),
  SIZE("Size", 1, 8),
  BRIGHTNESS("Brightness", 0, 4),
  INTENSITY("Intensity", 0, 4),
  FALLOFF("Falloff", 0, 1),
  CYCLES("Hue cycles", -4, 4);
  public final String label;
  public final float min, max;

  AnimationParameter(String label, float min, float max) {
    this.label = label;
    this.min = min;
    this.max = max;
  }

  public float decode(float value) {
    return min + Math.max(0, Math.min(1, value)) * (max - min);
  }

  public float normalize(float value) {
    return Math.max(0, Math.min(1, (value - min) / (max - min)));
  }

  public static List<AnimationParameter> forEffect(AnimationEffectType e) {
    return switch (e) {
      case PULSE -> List.of(MIN_OPACITY, MAX_OPACITY);
      case SCROLL -> List.of(DIRECTION_X, DIRECTION_Y, DISTANCE);
      case MOVING_GRADIENT ->
          List.of(ANGLE, WIDTH, OFFSET, FIRST_R, FIRST_G, FIRST_B, SECOND_R, SECOND_G, SECOND_B);
      case SPARKLE -> List.of(DENSITY, SEED, SIZE, BRIGHTNESS);
      case EMISSIVE_GLOW -> List.of(INTENSITY, FALLOFF);
      case HUE_SHIFT -> List.of(CYCLES);
    };
  }

  public float read(EffectParameters p) {
    return switch (p) {
      case EffectParameters.Pulse v -> this == MIN_OPACITY ? v.minOpacity() : v.maxOpacity();
      case EffectParameters.Scroll v ->
          switch (this) {
            case DIRECTION_X -> v.directionX();
            case DIRECTION_Y -> v.directionY();
            default -> v.distance();
          };
      case EffectParameters.Gradient v ->
          switch (this) {
            case ANGLE -> v.angle();
            case WIDTH -> v.width();
            case OFFSET -> v.offset();
            default ->
                component(this.name().startsWith("FIRST") ? v.firstColor() : v.secondColor());
          };
      case EffectParameters.Sparkle v ->
          switch (this) {
            case DENSITY -> v.density();
            case SEED -> v.seed();
            case SIZE -> v.size();
            default -> v.brightness();
          };
      case EffectParameters.Glow v -> this == INTENSITY ? v.intensity() : v.falloff();
      case EffectParameters.Hue v -> v.cycles();
    };
  }

  private int shift() {
    return name().endsWith("_R") ? 16 : name().endsWith("_G") ? 8 : 0;
  }

  private float component(int c) {
    return (c >>> shift()) & 255;
  }

  private int color(int c, float v) {
    return (c & ~(255 << shift())) | Math.round(v) << shift();
  }

  public EffectParameters set(EffectParameters p, float value) {
    float n = Math.max(min, Math.min(max, value));
    return switch (p) {
      case EffectParameters.Pulse v ->
          this == MIN_OPACITY
              ? new EffectParameters.Pulse(Math.min(n, v.maxOpacity()), v.maxOpacity())
              : new EffectParameters.Pulse(v.minOpacity(), Math.max(n, v.minOpacity()));
      case EffectParameters.Scroll v ->
          new EffectParameters.Scroll(
              this == DIRECTION_X ? n : v.directionX(),
              this == DIRECTION_Y ? n : v.directionY(),
              this == DISTANCE ? n : v.distance());
      case EffectParameters.Gradient v ->
          new EffectParameters.Gradient(
              this == ANGLE ? n : v.angle(),
              this == WIDTH ? n : v.width(),
              name().startsWith("FIRST") ? color(v.firstColor(), n) : v.firstColor(),
              name().startsWith("SECOND") ? color(v.secondColor(), n) : v.secondColor(),
              this == OFFSET ? n : v.offset(),
              false);
      case EffectParameters.Sparkle v ->
          new EffectParameters.Sparkle(
              this == DENSITY ? n : v.density(),
              this == SEED ? Math.round(n) : v.seed(),
              this == SIZE ? Math.round(n) : v.size(),
              this == BRIGHTNESS ? n : v.brightness());
      case EffectParameters.Glow v ->
          new EffectParameters.Glow(
              this == INTENSITY ? n : v.intensity(), this == FALLOFF ? n : v.falloff());
      case EffectParameters.Hue v -> new EffectParameters.Hue(n);
    };
  }
}
