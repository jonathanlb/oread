package org.bredin.oread;

import io.reactivex.Flowable;

public class SinSource {
	public static Flowable<SamplePacket> sinSrc(
		Flowable<TimePacket> timeSrc,
		double freq)
	{
		return sinSrc(timeSrc, 1, freq, 0);
	}

	public static Flowable<SamplePacket> sinSrc(
		Flowable<TimePacket> timeSrc,
		double amp,
		double freq)
	{
		return sinSrc(timeSrc, amp, freq, 0);
	}

	public static Flowable<SamplePacket> sinSrc(
		final Flowable<TimePacket> timeSrc,
		final double amp,
		final double freq,
		final double phase)
	{
		final double w = 2*Math.PI*freq;
		return timeSrc.map(time -> {
			long start = time.getStart();
			long end = time.getEnd();
			// If all samples had same length, we could eliminate numSamples calculation
			// XXX hardcode ms
			int numSamples = (int)(1e-3 * LPCMPacket.SAMPLE_RATE * (end - start));
			double[] data = new double[numSamples];
			for (int i = 0; i < numSamples; ++i) {
				final double t = start + i * LPCMPacket.SAMPLE_CYCLE_MS;
				data[i] = amp * Math.sin(w*t+phase);
			}
			return new SamplePacket(start, end, data);
		});
	}
}
