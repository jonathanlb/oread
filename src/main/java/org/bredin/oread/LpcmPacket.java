package org.bredin.oread;

import javax.sound.sampled.AudioFormat;

/**
 * Linear Pulse-code Modulation packet representing 16-bit samples as a sequence of byte pairs.
 */
public class LpcmPacket implements Packet<byte[]> {
  public static final int BITS_PER_SAMPLE = 16;
  public static final boolean BIG_ENDIAN = false;
  public static final int NUM_CHANNELS = 1;
  public static final boolean SIGNED = true;
  public static final int SAMPLE_RATE = 44100;
  public static final float SAMPLE_CYCLE_MS = (float)1000.0 / SAMPLE_RATE;
  public static final AudioFormat AUDIO_FORMAT =
      new AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, NUM_CHANNELS, SIGNED, BIG_ENDIAN);

  private final int end;
  private final byte[] data;
  private final int start;

  LpcmPacket(int start, int end, byte[] data) {
    this.end = end;
    this.data = data;
    this.start = start;
  }

  @Override
  public int getStartMillis() {
    return start;
  }

  @Override
  public int getEndMillis() {
    return end;
  }

  @Override
  public byte[] getData() {
    return data;
  }
}
