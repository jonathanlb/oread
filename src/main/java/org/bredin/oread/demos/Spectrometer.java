package org.bredin.oread.demos;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.Signals;
import org.bredin.oread.TimePacket;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

/**
 * To poll microphone demo, run with:
 * mvn exec:java -Dexec.mainClass=org.bredin.oread.demos.Spectrometer
 */
public class Spectrometer {
  private static Logger log = LogManager.getLogger();
  private static final int height = 600;
  private static final int width = 800;
  private static final int MS_SAMPLE_PERIOD = 10;
  private static final double INTENSITY_DECAY = 0.999;

  private final SwingWrapper<XYChart> sw;
  private final XYChart chart;
  private final FastFourierTransformer fft;
  private double maxIntensity = 0.0;

  /**
   * Default constructor to build and show chart as free-standing frame.
   */
  public Spectrometer() {
    chart = buildChart();
    sw = new SwingWrapper<>(chart);
    fft = new FastFourierTransformer(DftNormalization.STANDARD);
  }

  private XYChart buildChart() {
    XYChart chart =
        new XYChartBuilder()
          .width(width)
          .height(height)
          .title("Frequency")
          .theme(Styler.ChartTheme.Matlab)
          .build();

    chart.getStyler().setYAxisMax(100.0);
    chart.getStyler().setYAxisMin(0.0);
    // chart.getStyler().setYAxisLogarithmic(true); // needs error bars?
    chart.getStyler().setLegendVisible(false);
    chart.getStyler().setHasAnnotations(false);
    double[] as = {0.0};
    double[] fs = {1.0};
    XYSeries series = chart.addSeries("Freq", fs, as);
    series.setMarker(SeriesMarkers.NONE);

    return chart;
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
    double inten = as[modeIdx];
    log.info("Max intensity idx: {}/{} {}", modeIdx, as.length, inten);
    if (inten >= maxIntensity) {
      maxIntensity = inten;
    } else {
      maxIntensity = Math.max(maxIntensity * INTENSITY_DECAY, inten);
    }
    chart.getStyler().setYAxisMax(maxIntensity);
  }

  public Disposable start(Flowable<SamplePacket> samples) {
    sw.displayChart();
    return samples.subscribe(this::updateChart);
  }

  /**
   * Start plotting.
   */
  public Disposable start() throws LineUnavailableException {
    Flowable<TimePacket> time = TimePacket.clockTime(MS_SAMPLE_PERIOD, TimeUnit.MILLISECONDS);
    Flowable<LpcmPacket> mic = MicSource.mic(time);
    Flowable<SamplePacket> micSamples = LpcmToSamples.LpcmToSamples(mic);
    return start(micSamples);
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

    chart.updateXYSeries("Freq", fs, as, null);
    scaleChart(as);
    sw.repaintChart();
  }

  /** Entry point ignores CLI arguments. */
  public static void main(String[] args) throws Exception {
    Spectrometer s = new Spectrometer();
    // microphone
    s.start();

    // Throw a sin wave at it
    /*
    Flowable<TimePacket> time = TimePacket.logicalTime(1, TimeUnit.SECONDS, 10);
    Flowable<SamplePacket> sin = SinSource.sinSrc(time, 196);
    Flowable<SamplePacket> hannSin = Signals.hannWindow(sin);
    s.start(hannSin);
    */
  }
}
