package org.bredin.oread;

import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;

public class TimePacket implements Packet<Integer> {
  private final int end;
  private final int start;

  public TimePacket(int start, int period) {
    this.start = start;
    this.end = start + period;
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
  public Integer getData() {
    return start;
  }

  /**
   * Tick every period units.
   * @param period the duration between each tick.
   * @param unit the amount of time represented by period.
   * @return A sequence of time packets.
   */
  public static Flowable<TimePacket> clockTime(int period, TimeUnit unit) {
    final int periodMs = (int)TimeUnit.MILLISECONDS.convert(period, unit);
    return Flowable.interval(period, unit)
      .map(t -> new TimePacket(t.intValue() * periodMs, periodMs));
  }

  /** Create a stream of time values immediately. */
  public static Flowable<TimePacket> logicalTime(int period, TimeUnit unit, int numTicks) {
    final int periodMs = (int)TimeUnit.MILLISECONDS.convert(period, unit);
    return Flowable.rangeLong(0, numTicks)
      .map(t -> new TimePacket(t.intValue() * periodMs, periodMs));
  }
}
