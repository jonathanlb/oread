package org.bredin.oread;

import static org.junit.Assert.*;

import io.reactivex.Flowable;
import org.apache.commons.math3.complex.Complex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bredin.oread.panels.SpectrometerPanel;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.bredin.oread.Signals.*;

public class SignalsTest {
  private static Logger log = LogManager.getLogger();

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

  /**
   * Broken: over estimates frequencies
   */
	@Test
  public void fftTest() {
    Flowable<TimePacket> time = TimePacket.logicalTime(100, TimeUnit.MILLISECONDS, 1);
    Flowable<SamplePacket> sin = MathSources.sinSrc(time, (float)SpectrometerPanel.C3);
    Flowable<SamplePacket> hann = Signals.hannWindow(sin);
    Flowable<ComplexPacket> fs = Signals.fft(hann);

    double maxIntensity = 0.0;
    int maxIdx = -1;
    Complex[] f = fs.blockingFirst().getData();
    for (int i = 0; i < (f.length >> 1); ++i) {
      final double intensity = f[i].abs();
      if (intensity >= maxIntensity) {
        maxIntensity = intensity;
        maxIdx = i;
      }
    }

    final double EPS = 0.5 * LpcmPacket.SAMPLE_RATE / f.length;
    double freq = (maxIdx + 1) * LpcmPacket.SAMPLE_RATE / f.length;
    log.info("fft test: max f: {} idx: {} delta: {}", freq, maxIdx, EPS);
    assertEquals(SpectrometerPanel.C3, freq, EPS);
  }

  @Test
  public void ifftTest() {
	  int nFreqs = 1024;
	  int peakIdx = 10;
	  Complex[] data = new Complex[nFreqs];
	  for (int i = 0; i < data.length; ++i) {
	    data[i] = Complex.ZERO;
    }
    data[peakIdx] = Complex.ONE.multiply(1000);
    data[nFreqs - peakIdx - 1] = data[peakIdx];

    ComplexPacket spectrum = new ComplexPacket(1, 100, data);
    Flowable<SamplePacket> signal = Signals.ifft(Flowable.just(spectrum));
    ComplexPacket spectrum0 = Signals.fft(signal).blockingFirst();
    Complex[] data0 = spectrum0.getData();

    int maxIdx = -1;
    double maxIntensity = 0.0;
    for (int i = 0; i < nFreqs >> 1; ++i) {
      final double intensity = data0[i].abs();
      if (intensity >= maxIntensity) {
        maxIdx = i;
        maxIntensity = intensity;
      }
    }

    double inputFrequency = (peakIdx + 1.0) * 0.1 / nFreqs;
    double recoveredFrequency = (maxIdx + 1.0) * 0.1 / data0.length;
    double EPS = 0.1 * Math.max(1.0 / nFreqs, 1.0 / data0.length);
    assertEquals(inputFrequency, recoveredFrequency, EPS);
  }

  /**
   * Error increases as we diminish frequencies to be halved.
   * A4 down is within 1Hz. D3 down is within 1.3Hz which is greater than available accuracy.
   */
	@Test
  public void octaveDownTest() {
    Flowable<TimePacket> time = TimePacket.logicalTime(100, TimeUnit.MILLISECONDS, 1);
    Flowable<SamplePacket> sin = MathSources.sinSrc(time, (float)SpectrometerPanel.A4);
    Flowable<SamplePacket> octaveSin = Signals.octaveDown(sin);
    Complex[] fs = Signals.fft(octaveSin)
      .blockingFirst().getData();

    int maxIdx = -1;
    double maxIntensity = 0.0;
    for (int i = 0; i < fs.length >> 1; ++i) {
      final double intensity = fs[i].abs();
      if (intensity >= maxIntensity) {
        maxIdx = i;
        maxIntensity = intensity;
      }
    }

    final double EPS = LpcmPacket.SAMPLE_RATE / fs.length;
    double freq = (maxIdx + 1) * LpcmPacket.SAMPLE_RATE / fs.length;
    log.info("octave test: max f: {} idx: {} delta: {}", freq, maxIdx, EPS);
    assertEquals(0.5 * SpectrometerPanel.A4, freq, EPS);
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
