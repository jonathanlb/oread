package org.bredin.oread;

import javax.sound.sampled.AudioFormat;

/**
 * Linear Pulse-code Modulation packet representing 16-bit samples as a sequence of byte pairs.
 */
public class LPCMPacket implements Packet<byte[]> {
	static final int BITS_PER_SAMPLE = 16;
	static final boolean BIG_ENDIAN = false;
	static final int NUM_CHANNELS = 1;
	static final boolean SIGNED = true;
	static final int SAMPLE_RATE = 44100;
	static final double SAMPLE_CYCLE_MS = 1000.0/SAMPLE_RATE;
	static final AudioFormat AUDIO_FORMAT =
		new AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, NUM_CHANNELS, SIGNED, BIG_ENDIAN);

	private final long end;
	private final byte[] data;
	private final long start;

	LPCMPacket(long start, long end, byte[] data) {
		this.end = end;
		this.data = data;
		this.start = start;
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
	public byte[] getData() {
		return data;
	}
}
