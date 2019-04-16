package org.bredin.oread;

import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Signals {
  private static Logger log = LogManager.getLogger();

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

  public static int nextPowerOf2(int x) {
    return (int)Math.pow(2, Math.ceil(Math.log(x) / Math.log(2.0)));
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
