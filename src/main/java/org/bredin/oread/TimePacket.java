package org.bredin.oread;

import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;

public class TimePacket implements Packet<Long> {
	private final long end;
	private final long start;

	public TimePacket(long start, long period) {
		this.start = start;
		this.end = start + period;
	}

	@Override
	public long getStart() {
		return start;
	}

	@Override
	public long getEnd() {
		return end;
	}

	@Override
	public Long getData() {
		return start;
	}

	/**
	 * Tick every period units.
	 * @param period the duration between each tick.
	 * @param unit the amount of time represented by period.
	 * @return A sequence of time packets.
	 */
	public static Flowable<TimePacket> clockTime(long period, TimeUnit unit) {
		final long periodMs = TimeUnit.MILLISECONDS.convert(period, unit);
		return Flowable.interval(period, unit).
			map(t -> new TimePacket(t * periodMs, periodMs));
	}

	public static Flowable<TimePacket> logicalTime(long period, TimeUnit unit, long numTicks) {
		final long periodMs = TimeUnit.MILLISECONDS.convert(period, unit);
		return Flowable.rangeLong(0, numTicks).
			map(t -> new TimePacket(t * periodMs, periodMs));
	}
}
