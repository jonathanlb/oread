package org.bredin.oread;

import io.reactivex.Flowable;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SamplesToLPCMTest {
	@Test
	public void decodeEncodingTest() {
		double[] data = new double[(int)(LPCMPacket.SAMPLE_RATE * 1e-3)];
		data[0] = 0;
		data[1] = 0.5;
		data[2] = 0.75;
		data[3] = 1.0;
		data[4] = -0.5;
		data[5] = -0.75;
		data[6] = -1.0;
		SamplePacket sample = new SamplePacket(1, 2, data);
		Flowable<LPCMPacket> lpcm = SamplesToLPCM.samplesToLPCM(Flowable.just(sample));
		Flowable<SamplePacket> samples = LPCMToSamples.LPCMToSamples(lpcm);
		SamplePacket result = samples.blockingFirst();

		for (int i = 0; i < 7; ++i) {
			assertEquals("Comparing " + i + "th", data[i], result.getData()[i], 1e-2);
		}
	}

	@Test
	public void encodeByteTest() {
		double[] data = new double[(int)(LPCMPacket.SAMPLE_RATE * 1e-3)];
		data[0] = 0;
		data[1] = 0.5;
		data[2] = 0.75;
		data[3] = 1.0;
		data[4] = -0.5;
		data[5] = -0.75;
		data[6] = -1.0;
		SamplePacket sample = new SamplePacket(1, 2, data);
		Flowable<SamplePacket> samples = Flowable.just(sample);
		Flowable<LPCMPacket> wav = SamplesToLPCM.samplesToLPCM(samples);
		byte[] bytes = wav.blockingFirst().getData();

		assertEquals(data.length*2, bytes.length);
		byte[] expected0 = {0x0, 0x0}; // 0.0
		byte[] actual0 = {bytes[0], bytes[1]};
		assertEquals(Arrays.toString(expected0), Arrays.toString(actual0));

		byte[] expected1 = {-0x1, 0x3f}; // 0.5
		byte[] actual1 = {bytes[2], bytes[3]};
		assertEquals(Arrays.toString(expected1), Arrays.toString(actual1));

		byte[] expected2 = {-0x01, 0x5f}; // 0.75
		byte[] actual2 = {bytes[4], bytes[5]};
		assertEquals(Arrays.toString(expected2), Arrays.toString(actual2));

		byte[] expected3 = {-0x01, 0x7f}; // 1.0
		byte[] actual3 = {bytes[6], bytes[7]};
		assertEquals(Arrays.toString(expected3), Arrays.toString(actual3));

		byte[] expected4 = {0x01, -0x40}; // -0.5
		byte[] actual4 = {bytes[8], bytes[9]};
		assertEquals(Arrays.toString(expected4), Arrays.toString(actual4));

		byte[] expected5 = {0x01, -0x60}; // -0.75
		byte[] actual5 = {bytes[10], bytes[11]};
		assertEquals(Arrays.toString(expected5), Arrays.toString(actual5));

		byte[] expected6 = {0x01, -0x80}; // -1.0
		byte[] actual6 = {bytes[12], bytes[13]};
		assertEquals(Arrays.toString(expected6), Arrays.toString(actual6));
	}

}