package org.bredin.oread;

import static org.junit.Assert.*;
import org.junit.Test;

import static org.bredin.oread.Signals.*;

public class SignalsTest {
	@Test
	public void hannWindowTest() {
		final float EPS = 1e-7f;
		float[] input = {1, 1, 1};
		float[] expected = {0, 1, 0};
		assertArrayEquals(expected, hannWindow(input), EPS);
	}

	@Test
	public void nextPowerOfTwoTest() {
		assertEquals(0, nextPowerOf2(0));
		assertEquals(1, nextPowerOf2(1));
		assertEquals(2, nextPowerOf2(2));
		assertEquals(4, nextPowerOf2(3));
		assertEquals(4, nextPowerOf2(4));
		assertEquals(8, nextPowerOf2(5));
		assertEquals(64, nextPowerOf2(33));
	}

	@Test
	public void prevPowerOfTwoTest() {
		assertEquals(1, prevPowerOf2(1));
		assertEquals(2, prevPowerOf2(2));
		assertEquals(2, prevPowerOf2(3));
		assertEquals(4, prevPowerOf2(4));
		assertEquals(4, prevPowerOf2(7));
		assertEquals(8, prevPowerOf2(8));
		assertEquals(8, prevPowerOf2(15));
		assertEquals(64, prevPowerOf2(127));
	}

	@Test
	public void resampleDoubleLargerTest() {
		final double EPS = 1e-7;
		float[] input = {0, 1/3.0f, 2/3.0f, 1};
		double[] expected = {0.0, 0.25, 0.50, 0.75, 1.0};
		assertArrayEquals(expected, resampleToDouble(input, 5), EPS);
	}

	@Test
	public void resampleLargerTest() {
		final float EPS = 1e-7f;
		float[] input = {0, 1/3.0f, 2/3.0f, 1};
		float[] expected = {0.0f, 0.25f, 0.50f, 0.75f, 1.0f};
		assertArrayEquals(expected, resample(input, 5), EPS);
	}

	@Test
	public void resampleDoubleSmallerTest() {
		final double EPS = 1e-7;
		float[] input = {0, 0.25f, 0.75f, 1};
		double[] expected = {0.0, 0.5, 1.0};
		assertArrayEquals(expected, resampleToDouble(input, 3), EPS);
	}

	@Test
	public void resampleSmallerTest() {
		final float EPS = 1e-7f;
		float[] input = {0, 0.25f, 0.75f, 1};
		float[] expected = {0.0f, 0.5f, 1.0f};
		assertArrayEquals(expected, resample(input, 3), EPS);
	}

	@Test
	public void resampleDoubleToTwoSamplesTest() {
		final double EPS = 1e-7;
		float[] input = {0, 0.25f, 0.75f, 1};
		double[] expected = {0.0, 1.0};
		assertArrayEquals(expected, resampleToDouble(input, 2), EPS);
	}

	@Test
	public void resampleToTwoSamplesTest() {
		final float EPS = 1e-7f;
		float[] input = {0, 0.25f, 0.75f, 1};
		float[] expected = {0.0f, 1.0f};
		assertArrayEquals(expected, resample(input, 2), EPS);
	}
}
