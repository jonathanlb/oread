package org.bredin.oread.panels;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.Signals;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class SpectrometerPanel extends XYChart {
  private static Logger log = LogManager.getLogger();

  private static final int HEIGHT = 65;
  private static final int WIDTH = 100;
  private static final String INPUT_SERIES = "Freq";

  private Disposable disposable;
  private final Flowable<SamplePacket> input;

  private final FastFourierTransformer fft;
  private double maxIntensity = 0.0;
  private static final double INTENSITY_DECAY = 0.999;

  public SpectrometerPanel(Flowable<SamplePacket> input, int width, int height) {
    super(width, height);

    getStyler().setYAxisMax(100.0);
    getStyler().setYAxisMin(0.0);
    getStyler().setLegendVisible(false);
    double[] as = {0.0};
    double[] fs = {1.0};
    XYSeries series = addSeries(INPUT_SERIES, fs, as);
    series.setMarker(SeriesMarkers.NONE);

    fft = new FastFourierTransformer(DftNormalization.STANDARD);
    this.input = input;
  }

  public SpectrometerPanel(Flowable<SamplePacket> input) {
    this(input, WIDTH, HEIGHT);
  }

  public void addTunerHairlines() {
    // TODO
  }

  /** Return the index of the most intense frequency. */
  private int findMaxFreqIdx(double[] as) {
    double max = -1;
    int midx = 0;

    // Only look at first half of spectrum.
    for (int i = as.length >> 1; i > 0; --i) {
      final double f = as[i];
      if (f >= max) {
        max = f;
        midx = i;
      }
    }
    return midx;
  }

  private void scaleChart(double[] as) {
    int modeIdx = findMaxFreqIdx(as);
    double intensity = as[modeIdx];
    log.debug("Max intensity idx: {}/{} {}", modeIdx, as.length, intensity);
    if (intensity >= maxIntensity) {
      maxIntensity = intensity;
    } else {
      maxIntensity = Math.max(maxIntensity * INTENSITY_DECAY, intensity);
    }
    getStyler().setYAxisMax(maxIntensity);
  }

  public synchronized Disposable start() {
    if (disposable != null) {
      stop();
    }
    disposable = input.subscribe(this::updateChart);
    return disposable;
  }

  public synchronized void stop() {
    disposable.dispose();
    disposable = null;
  }

  private void updateChart(SamplePacket s) {
    float[] input = s.getData();
    double[] data = Signals.resampleToDouble(
      input, Signals.prevPowerOf2(input.length));

    Complex[] f = fft.transform(data, TransformType.FORWARD);
    int n = f.length >> 1; // display only half the frequencies (avoid sym dups)
    double[] fs = new double[n];
    double[] as = new double[n];
    double df = LpcmPacket.SAMPLE_RATE / f.length;
    for (int i = 0; i < n; ++i) {
      as[i] = f[i].abs();
      fs[i] = df * i;
    }

    updateXYSeries(INPUT_SERIES, fs, as, null);
    scaleChart(as);
    // repaint externally
  }

}
