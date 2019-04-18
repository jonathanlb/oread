package org.bredin.oread.demos;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.bredin.oread.LpcmPacket;
import org.bredin.oread.MathSources;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.TimePacket;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

/**
 * Plot a time series signal.
 */
public class TimeSignal {
  private static final int height = 600;
  private static final int width = 800;

  private final SwingWrapper<XYChart> sw;
  private final XYChart chart;
  private final LinkedList<Double> xs;
  private final LinkedList<Double> ys;

  /**
   * Build and display a free-standing chart.
   */
  public TimeSignal() {
    chart = new XYChartBuilder()
      .width(width)
      .height(height)
      .theme(Styler.ChartTheme.Matlab)
      .build();
    chart.getStyler().setLegendVisible(false);
    sw = new SwingWrapper<>(chart);
    xs = new LinkedList<>();
    ys = new LinkedList<>();
    double[] x = {0};
    double[] y = {0};
    XYSeries series = chart.addSeries("f", x, y);
    series.setMarker(SeriesMarkers.NONE);
  }

  /** CLI entry point ignores arguments. */
  public static void main(String[] args) {
    TimeSignal ts = new TimeSignal();

    Flowable<TimePacket> time = TimePacket.logicalTime(10, TimeUnit.MILLISECONDS, 2);
    Flowable<SamplePacket> sin = MathSources.sinSrc(time, 330);
    // ts.start(Signals.hannWindow(sin));
    ts.start(sin);
  }

  public Disposable start(Flowable<SamplePacket> samples) {
    sw.displayChart();
    return samples.subscribe(this::updateChart);
  }

  private void updateChart(SamplePacket s) {
    final double t = 1e-3 * s.getStartMillis();
    final float[] d = s.getData();
    for (int i = 0; i < d.length; ++i) {
      xs.add(t + 1e-3 * i * LpcmPacket.SAMPLE_CYCLE_MS);
      ys.add((double)d[i]);
    }

    chart.updateXYSeries("f", xs, ys, null);
    sw.repaintChart();
  }
}
