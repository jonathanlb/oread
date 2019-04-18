package org.bredin.oread;

import org.apache.commons.math3.complex.Complex;

public class ComplexPacket implements Packet<Complex[]> {
  final int startMillis;
  final int endMillis;
  final Complex[] data;

  /**
   * Create a new packet without copying data (just keep a reference).
   */
  public ComplexPacket(int startMillis, int endMillis, Complex[] data) {
    this.startMillis = startMillis;
    this.endMillis = endMillis;
    this.data = data;
  }

  @Override
  public int getStartMillis() {
    return startMillis;
  }

  @Override
  public int getEndMillis() {
    return endMillis;
  }

  @Override
  public Complex[] getData() {
    return data;
  }
}
