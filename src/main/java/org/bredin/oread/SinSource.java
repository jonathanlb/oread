package org.bredin.oread;

import io.reactivex.Flowable;

public class SinSource {

  /** Create a sine signal. */
  public static Flowable<SamplePacket> sinSrc(Flowable<TimePacket> timeSrc, float freq) {
    return sinSrc(timeSrc, 1, freq, 0);
  }

  /** Create a sine signal. */
  public static Flowable<SamplePacket> sinSrc(Flowable<TimePacket> timeSrc, float amp, float freq) {
    return sinSrc(timeSrc, amp, freq, 0);
  }

  /** Create a sine signal. */
  public static Flowable<SamplePacket> sinSrc(
      final Flowable<TimePacket> timeSrc,
      final float amp,
      final float freq,
      final float phase) {
    final double w = 2 * Math.PI * freq;
    final float sampleTime = 1.0f / LpcmPacket.SAMPLE_RATE;

    return timeSrc.map(time -> {
      int startMs = time.getStartMillis();
      int endMs = time.getEndMillis();
      float start = startMs * 1e-3f;
      // If all samples had same length, we could eliminate numSamples calculation
      int numSamples = (int)(1e-3 * LpcmPacket.SAMPLE_RATE * (endMs - startMs));
      float[] data = new float[numSamples];
      for (int i = 0; i < numSamples; ++i) {
        final float t = start + i * sampleTime;
        data[i] = amp * (float)Math.sin(w * t + phase);
      }
      return new SamplePacket(startMs, endMs, data, LpcmPacket.SAMPLE_RATE);
    });
  }
}
