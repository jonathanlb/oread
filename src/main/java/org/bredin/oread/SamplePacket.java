package org.bredin.oread;

/**
 * Data package representing a snippet of a sampled signal with values
 * in the range [-1,1].
 */
public class SamplePacket implements Packet<double[]> {
	final long start;
	final long end;
	final double[] data;

	public SamplePacket(long start, long end, double[] data) {
		this.start = start;
		this.end = end;
		this.data = data;
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
	public double[] getData() {
		return data;
	}
}
