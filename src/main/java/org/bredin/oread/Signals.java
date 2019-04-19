package org.bredin.oread;

import io.reactivex.Flowable;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Signals {
  private static Logger log = LogManager.getLogger();

  private static final FastFourierTransformer fft =
      new FastFourierTransformer(DftNormalization.STANDARD);

  static class PolarCoordinate {
    public final double radius;
    public final double angle;

    PolarCoordinate(double radius, double angle) {
      this.radius = radius;
      this.angle = angle;
    }
  }

  /**
   * Convert complex cartesian to polar.
   */
  public static PolarCoordinate complexToPolar(Complex x) {
    double re = x.getReal();
    double im = x.getImaginary();
    double theta;

    if (im == 0.0) { // XXX threshold?
      if (re >= 0) {
        theta = 0.0;
      } else {
        theta = Math.PI;
      }
    } else {
      theta = Math.atan2(im, re); // to pm PI
    }

    return new PolarCoordinate(x.abs(), theta);
  }

  /**
   * Return the complex-valued spectrum sampled from the real-valued time series.
   */
  public static Flowable<ComplexPacket> fft(Flowable<SamplePacket> input) {
    return input.map(packet -> {
      float[] samples = packet.getData();
      int n = samples.length;
      // pad input to fft a) to get power of 2; b) expand to improve accuracy of peaks
      double[] data = new double[nextPowerOf2(n) << 2];
      for (int i = 0; i < n; ++i) {
        data[i] = samples[i];
      }

      Complex[] f = fft.transform(data, TransformType.FORWARD);
      return new ComplexPacket(packet.getStartMillis(), packet.getEndMillis(), f);
    });
  }

  /**
   * https://en.wikipedia.org/wiki/Hann_function
   * @param input samples
   * @return the input scaled by the Hann function.
   */
  public static float[] hannWindow(float[] input) {
    int n = input.length;
    float cosScale = (float)(2 * Math.PI / (n - 1));
    float[] result = new float[n];
    for (int i = 0; i < n; ++i) {
      final float w = i * cosScale;
      result[i] = (float)(input[i] * 0.5 * (1 - Math.cos(w)));
    }
    return result;
  }

  /**
   * https://en.wikipedia.org/wiki/Hann_function
   * @param input samples
   * @return the input scaled by the Hann function.
   */
  public static Flowable<SamplePacket> hannWindow(Flowable<SamplePacket> input) {
    return input.map(p ->
      new SamplePacket(
        p.getStartMillis(), p.getEndMillis(), hannWindow(p.getData()), p.getSampleRate()));
  }

  /**
   * Return the real-valued signal from Fourier Series.
   * Ignores phase and end-point discontinuities....
   */
  public static Flowable<SamplePacket> ifft(Flowable<ComplexPacket> input) {
    return input.map(spectrum -> {
      int startMs = spectrum.getStartMillis();
      int endMs = spectrum.getEndMillis();
      Complex[] signal = fft.transform(spectrum.getData(), TransformType.INVERSE);
      int n = signal.length;
      int numSamples = (int)(1e-3 * LpcmPacket.SAMPLE_RATE * (endMs - startMs));
      float[] samples = new float[numSamples];
      // XXX ignores endpoint discos.
      for (int i = 0; i < numSamples; ++i) {
        final Complex x = signal[i % n];
        // ignore imaginary
        samples[i] = (float)x.getReal();
      }
      return new SamplePacket(startMs, endMs, samples, LpcmPacket.SAMPLE_RATE);
    });
  }

  public static int nextPowerOf2(int x) {
    return (int)Math.pow(2, Math.ceil(Math.log(x) / Math.log(2.0)));
  }

  /**
   * Transform the input signal an octave down.
   * We need better phase vocoding.  Currently, we just rotate the spectrum components
   * from Laroche and Dolson, 1999, but the units are unclear.... The current implementation
   * is the result of blind search and luck.
   *
   * <p>https://www.ee.columbia.edu/~dpwe/papers/LaroD99-pvoc.pdf
   */
  public static Flowable<ComplexPacket> octaveDown(Flowable<ComplexPacket> input) {
    return input.map(packet -> {
      Complex[] allSpectrum = packet.getData();
      int n = allSpectrum.length;
      Complex[] lowSpectrum = new Complex[n];
      int half = n >> 2;

      for (int i = 0; i < half; ++i) {
        final Complex a = allSpectrum[i << 1];
        final Complex b = allSpectrum[(i << 1) + 1];
        // need phase vocoding preserve phase information
        // oddly sounds best for sine wave
        // scale up to preserve volume lost in frequency scaling
        lowSpectrum[i] = new Complex(
          a.getReal() + b.getReal(),
          a.getImaginary() + b.getImaginary())
                           .multiply(ComplexUtils.polar2Complex(2.0, 0.5 * i));
        // .multiply(ComplexUtils.polar2Complex(2.0, 0.5 * mxi * TWO_PI / n));
        // .multiply(ComplexUtils.polar2Complex(2.0, 0.5 * i * TWO_PI / n));
        lowSpectrum[n - i - 1] = lowSpectrum[i];
      }

      int n1 = n - half;
      for (int i = half; i < n1; ++i) {
        lowSpectrum[i] = Complex.ZERO;
      }

      return new ComplexPacket(
        packet.getStartMillis(),
        packet.getEndMillis(),
        lowSpectrum);
    });
  }

  public static int prevPowerOf2(int x) {
    return (int)Math.pow(2, Math.floor(Math.log(x) / Math.log(2.0)));
  }

  /**
   * Take an array of samples an interpolate to create a signal composted of a
   * different number of samples.
   * @param input The input to approximate.
   * @param sz The desired output size.
   * @return The signal of size sz of floats.
   */
  public static float[] resample(float[] input, int sz) {
    float[] result = new float[sz];
    result[0] = input[0];
    int n = input.length;
    int sz1 = sz - 1;
    float m = (n - 1) / (float)sz1;
    log.debug("resampling {} to {}", n, sz);
    for (int i = 1; i < sz1; ++i) {
      final float p = i * m;
      final int a = (int)Math.floor(p);
      final int b = (int)Math.ceil(p);
      final float wa = b - p;
      final float wb = p - a;
      result[i] = wa * input[a] + wb * input[b];
    }
    result[sz1] = input[n - 1];
    return result;
  }

  /**
   * Take an array of samples an interpolate to create a signal composted of a
   * different number of double-precision samples.
   * @param input The input to approximate.
   * @param sz The desired output size.
   * @return The signal of size sz of floats.
   */
  public static double[] resampleToDouble(float[] input, int sz) {
    double[] result = new double[sz];
    result[0] = input[0];
    int n = input.length;
    int sz1 = sz - 1;
    double m = (n - 1) / (double)sz1;
    log.debug("resampling {} to {}", n, sz);
    for (int i = 1; i < sz1; ++i) {
      final double p = i * m;
      final int a = (int)Math.floor(p);
      final int b = (int)Math.ceil(p);
      final double wa = b - p;
      final double wb = p - a;
      result[i] = wa * input[a] + wb * input[b];
    }
    result[sz1] = input[n - 1];
    return result;
  }
}
