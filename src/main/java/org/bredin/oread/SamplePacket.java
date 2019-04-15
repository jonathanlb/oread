package org.bredin.oread;

/**
 * Data package representing a snippet of a sampled signal with values
 * in the range [-1,1].
 */
public class SamplePacket implements Packet<float[]> {
  final int start;
  final int end;
  final float[] data;
  final int sampleRate;

  /**
   * Build a new sample without copying the input data.
   */
  public SamplePacket(int start, int end, float[] data, int sampleRate) {
    this.start = start;
    this.end = end;
    this.data = data;
    this.sampleRate = sampleRate;
  }

  public int getSampleRate() {
    return sampleRate;
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
  public float[] getData() {
    return data;
  }
}
