package dev.loomstudios.project;

/** Monotone cubic Bezier timing curve. X handles stay ordered; no overshoot. */
public record KeyframeCurve(float x1, float y1, float x2, float y2) {
  public static final KeyframeCurve DEFAULT = new KeyframeCurve(.25f, .1f, .25f, 1f);

  public KeyframeCurve {
    for (float v : new float[] {x1, y1, x2, y2})
      if (!Float.isFinite(v) || v < 0 || v > 1)
        throw new IllegalArgumentException("Curve handle out of range");
    if (x1 > x2) throw new IllegalArgumentException("Curve X handles must be ordered");
  }

  private static float cubic(float t, float a, float b) {
    float u = 1 - t;
    return 3 * u * u * t * a + 3 * u * t * t * b + t * t * t;
  }

  public float apply(float x) {
    float lo = 0, hi = 1;
    for (int i = 0; i < 24; i++) {
      float t = (lo + hi) / 2;
      if (cubic(t, x1, x2) < x) lo = t;
      else hi = t;
    }
    return cubic((lo + hi) / 2, y1, y2);
  }
}
