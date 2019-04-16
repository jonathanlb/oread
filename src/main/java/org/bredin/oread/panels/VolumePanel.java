package org.bredin.oread.panels;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bredin.oread.SamplePacket;
import org.knowm.xchart.CategoryChart;

import java.util.LinkedList;
import java.util.List;

public class VolumePanel extends CategoryChart {
  private static final Logger log = LogManager.getLogger();

  private static final int HEIGHT = 65;
  private static final int WIDTH = 100;

  private static final int NUM_SAMPLES = 100;
  private static final String INPUT_SERIES = "input";

  private final Flowable<SamplePacket> input;

  private final LinkedList<Double> times;
  private final LinkedList<Double> volumes;
  private Disposable disposable;

  public VolumePanel(Flowable<SamplePacket> input) {
    this(input, WIDTH, HEIGHT);
  }

  public VolumePanel(Flowable<SamplePacket> input, int width, int height) {
    super(width, height);

    getStyler().setYAxisMax(1.1);
    getStyler().setYAxisMin(0.0);
    getStyler().setXAxisTicksVisible(false);
    getStyler().setLegendVisible(false);
    getStyler().setHasAnnotations(false);
    getStyler().setPlotGridLinesVisible(false);

    times = new LinkedList<>();
    volumes = new LinkedList<>();
    for (int i = NUM_SAMPLES; i > 0; --i) {
      volumes.add(0.0);
      times.add(-1.0 * i);
    }
    addSeries(INPUT_SERIES, times, volumes);
    disposable = null;

    this.input = input;
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

  private static double[] toArray(List<Double> list) {
    double[] result = new double[list.size()];
    int i = 0;
    for (Double v : list) {
      result[i++] = v;
    }
    return result;
  }

  private void updateChart(SamplePacket p) {
    double v = 0.0;
    float[] vs = p.getData();
    for (int i = vs.length - 1; i >= 0; --i) {
      v = Math.max(v, Math.abs(vs[i]));
    }
    volumes.removeFirst();
    volumes.add(v);

    int t = p.getEndMillis();
    times.removeFirst();
    times.add((double)t);

    // CategoryChart requires non-mutating list or array.  At 10 updates/second or
    // faster we're in danger of getting a ConcurrentModificationException in the
    // first second or two of operation.  Copy to fresh arrays...
    double[] timesArray = toArray(times);
    double[] volumesArray = toArray(volumes);
    updateCategorySeries(INPUT_SERIES, timesArray, volumesArray, null);
    // need to signal to repaint externally
  }

}
