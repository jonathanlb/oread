package org.bredin.oread;

import io.reactivex.Flowable;

public class SamplesToLPCM {
	static final double SIGNAL_NORM = (1 << (LPCMPacket.BITS_PER_SAMPLE - 1)) - 1 ; // 2^15 - 1

	public static Flowable<LPCMPacket> samplesToLPCM(Flowable<SamplePacket> src) {
		// If we knew all sample groups were of same size, we could pull some multiplication out.
		return src.map(sample -> {
			double[] samples = sample.getData();
			byte[] data = new byte[(LPCMPacket.BITS_PER_SAMPLE >> 3)* samples.length];
			for (int i = 0; i < samples.length; ++i) {
				final int xi = (int)(SIGNAL_NORM * samples[i]);
				final int j = i << 1;
				// XXX BITS_PER_SAMPLE is hardcoded into following statement pair.
				data[j] = (byte)xi;
				data[j + 1] = (byte)(xi >>> 8);
			}

			return new LPCMPacket(sample.getStart(), sample.getEnd(), data);
		});
	}
}
