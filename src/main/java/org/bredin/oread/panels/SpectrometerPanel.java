package org.bredin.oread.panels;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.apache.commons.math3.complex.Complex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bredin.oread.ComplexPacket;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.Signals;
// import org.knowm.xchart.internal.chartpart.components.ChartLine;
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

  private double maxIntensity = 0.0;
  private static final double INTENSITY_DECAY = 0.999;

  // private List<ChartLine> referenceFreqs;

  public static final double C3 = 130.8128;
  public static final double G3 = 195.9977;
  public static final double D4 = 293.6648;
  public static final double A4 = 440.000;
  public static final double E5 = 659.2551;

  public static final double[] VIOLA_NOTES = { C3, G3, D4, A4 };
  public static final double[] VIOLIN_NOTES = { G3, D4, A4, E5 };

  /**
   * Create the panel to listen to the input with the specified dimensions.
   */
  public SpectrometerPanel(Flowable<SamplePacket> input, int width, int height) {
    super(width, height);

    getStyler().setYAxisMax(100.0);
    getStyler().setYAxisMin(0.0);
    getStyler().setLegendVisible(false);
    double[] as = {0.0};
    double[] fs = {1.0};
    XYSeries series = addSeries(INPUT_SERIES, fs, as);
    series.setMarker(SeriesMarkers.NONE);

    this.input = input;
  }

  /**
   * Create the panel to listen to the input with the default dimensions.
   */
  public SpectrometerPanel(Flowable<SamplePacket> input) {
    this(input, WIDTH, HEIGHT);
  }

  public void addTunerHairlines(double[] freqs) {
    // TODO
    /*
    BasicStroke stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
      10.0f, new float[] { 3.0f, 0.0f }, 0.0f);
    for (int i = 0; i < freqs.length; ++i) {
      ChartLine xLine = new ChartLine(freqs[i], true, false);
      xLine.setColor(Color.MAGENTA);
      xLine.setStroke(stroke);
      xLine.init(this);
    }
    */
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

  /**
   * Begin processing the input.
   * @return Return a hook to stop processing.
   */
  public synchronized Disposable start() {
    if (disposable != null) {
      stop();
    }
    disposable = Signals.fft(input)
                   .subscribe(this::updateChart);
    return disposable;
  }

  public synchronized void stop() {
    disposable.dispose();
    disposable = null;
  }

  private void updateChart(ComplexPacket packet) {
    Complex[] fft = packet.getData();
    int n = fft.length >> 1; // display only half the frequencies (avoid sym dups)
    double[] fs = new double[n];
    double[] as = new double[n];
    double df = LpcmPacket.SAMPLE_RATE / fft.length;
    for (int i = 0; i < n; ++i) {
      as[i] = fft[i].abs();
      fs[i] = df * (i + 1);
    }

    updateXYSeries(INPUT_SERIES, fs, as, null);
    scaleChart(as);
    // repaint externally
  }

}
