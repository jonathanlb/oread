package org.bredin.oread;

import io.reactivex.Flowable;

public class SamplesToLpcm {
  static final float SIGNAL_NORM = (1 << (LpcmPacket.BITS_PER_SAMPLE - 1)) - 1; // 2^15 - 1

  /**
   * Convert floating-point samples to byte signal.
   */
  public static Flowable<LpcmPacket> samplesToLpcm(Flowable<SamplePacket> src) {
    // If we knew all sample groups were of same size, we could pull some multiplication out.
    return src.map(sample -> {
      float[] samples = sample.getData();
      byte[] data = new byte[(LpcmPacket.BITS_PER_SAMPLE >> 3) * samples.length];
      for (int i = 0; i < samples.length; ++i) {
        final int xi = (int)(SIGNAL_NORM * samples[i]);
        final int j = i << 1;
        // XXX BITS_PER_SAMPLE is hardcoded into following statement pair.
        data[j] = (byte)xi;
        data[j + 1] = (byte)(xi >>> 8);
      }

      return new LpcmPacket(sample.getStartMillis(), sample.getEndMillis(), data);
    });
  }
}
