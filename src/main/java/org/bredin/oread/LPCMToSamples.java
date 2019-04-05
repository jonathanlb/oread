package org.bredin.oread;

import io.reactivex.Flowable;

public class LPCMToSamples {
	static final double SIGNAL_NORM = 1.0 / SamplesToLPCM.SIGNAL_NORM;

	public static Flowable<SamplePacket> LPCMToSamples(Flowable<LPCMPacket> src) {
		return src.map(lpcm -> {
			final byte[] bytes = lpcm.getData();
			final int numSamples = bytes.length >> 1; // XXX hardcode from LPCMPacket
			final double[] data = new double[numSamples];
			for (int i = 0; i < numSamples; ++i) {
				final int i2 = i << 1;
				data[i] = SIGNAL_NORM * (bytes[i2] + (((int)bytes[i2+1]) << (LPCMPacket.BITS_PER_SAMPLE >> 1)));
			}
			return new SamplePacket(lpcm.getStart(), lpcm.getEnd(), data);
		});
	}
}
