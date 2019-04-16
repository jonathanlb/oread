package org.bredin.oread;

import io.reactivex.Flowable;

public class LpcmToSamples {
  static final float SIGNAL_NORM = 1.0f / SamplesToLpcm.SIGNAL_NORM;

  /**
   * Convert LPCM byte signal to floating-point samples.
   */
  public static Flowable<SamplePacket> lpcmToSamples(Flowable<LpcmPacket> src) {
    return src.map(lpcm -> {
      final byte[] bytes = lpcm.getData();
      final int numSamples = bytes.length >> 1; // XXX hardcode from LpcmPacket
      final float[] data = new float[numSamples];
      for (int i = 0; i < numSamples; ++i) {
        final int i2 = i << 1;
        data[i] = SIGNAL_NORM
                    * (bytes[i2] + (((int)bytes[i2 + 1]) << (LpcmPacket.BITS_PER_SAMPLE >> 1)));
      }
      return new SamplePacket(
        lpcm.getStartMillis(), lpcm.getEndMillis(), data, LpcmPacket.SAMPLE_RATE);
    });
  }
}
