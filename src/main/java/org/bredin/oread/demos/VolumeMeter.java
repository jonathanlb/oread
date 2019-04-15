package org.bredin.oread.demos;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.TimePacket;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;

/**
 * Plot a bar graph representing volume/signal-strength changing over time.
 * <p>
 * To poll microphone demo, run with:
 * mvn exec:java -Dexec.mainClass=org.bredin.oread.demos.VolumeMeter
 * </p>
 */
public class VolumeMeter {
  private static final Logger log = LogManager.getLogger();

  private static final int numSamples = 100;
  private static final int height = 600;
  private static final int width = 800;
  private static final int MS_SAMPLE_PERIOD = 10;

  private final LinkedList<Double> times;
  private final LinkedList<Double> volumes;
  private final CategoryChart chart;

  private SwingWrapper<CategoryChart> sw;

  /** Build and display free-standing chart. */
  public VolumeMeter() {
    chart = buildChart();
    volumes = new LinkedList<>();
    times = new LinkedList<>();
    initVolume(chart);
  }

  private CategoryChart buildChart() {
    CategoryChart chart =
        new CategoryChartBuilder()
          .width(width)
          .height(height)
          .title("Volume")
          .xAxisTitle("Time")
          .yAxisTitle("Volume")
          .build();

    chart.getStyler().setYAxisMax(1.1);
    chart.getStyler().setYAxisMin(0.0);
    chart.getStyler().setXAxisTicksVisible(false);
    chart.getStyler().setLegendVisible(false);
    chart.getStyler().setHasAnnotations(false);
    chart.getStyler().setPlotGridLinesVisible(false);

    return chart;
  }

  private void initVolume(CategoryChart chart) {
    for (int i = numSamples; i > 0; --i) {
      volumes.add(0.0);
      times.add(-1.0 * i);
    }
    chart.addSeries("Mic", times, volumes);
  }

  /**
   * Run with the input sequence.
   * @param samples the sequence to plot
   */
  Disposable start(Flowable<SamplePacket> samples) {
    sw = new SwingWrapper<>(chart);
    sw.displayChart();
    return samples.subscribe(this::updateChart);
  }

  /**
   * Use the default microphone and 10ms sampling period.
   * @throws LineUnavailableException if we cannot find the default mic.
   */
  Disposable start() throws LineUnavailableException {
    Flowable<TimePacket> time = TimePacket.clockTime(MS_SAMPLE_PERIOD, TimeUnit.MILLISECONDS);
    Flowable<LpcmPacket> mic = MicSource.mic(time);
    Flowable<SamplePacket> micSamples = LpcmToSamples.LpcmToSamples(mic);
    return start(micSamples);
  }

  private static double[] toArray(List<Double> list) {
    double[] result = new double[list.size()];
    int i = 0;
    for (Double v : list) {
      result[i++] = v;
    }
    return result;
  }

  /**
   * Redraw the volume chart given a new sample.
   * @param p the new sample.
   */
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

    log.debug("Update {} {} ()", t, v, vs.length);

    // CategoryChart requires non-mutating list or array.  At 10 updates/second or
    // faster we're in danger of getting a ConcurrentModificationException in the
    // first second or two of operation.  Copy to fresh arrays...
    double[] timesArray = toArray(times);
    double[] volumesArray = toArray(volumes);
    chart.updateCategorySeries("Mic", timesArray, volumesArray, null);
    sw.repaintChart();
  }

  /** CLI ignores arguments. */
  public static void main(String[] args) throws Exception {
    VolumeMeter vm = new VolumeMeter();
    Disposable d = vm.start();
    // d.dispose();
  }
}

