package org.bredin.oread.demos;

import io.reactivex.Flowable;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.TimePacket;
import org.bredin.oread.panels.SpectrometerPanel;
import org.bredin.oread.panels.VolumePanel;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

public class Pipeline {
  private static final int MILLIS_CLOCK_TICK = 10;
  private static final int NUM_ROWS = 2;
  private static final int NUM_COLUMNS = 3;

  private final SwingWrapper<Chart> sw;
  private final Flowable<TimePacket> time;
  private final Flowable<LpcmPacket> mic;
  private final Flowable<SamplePacket> input;

  private final VolumePanel volume;
  private final SpectrometerPanel spectrometer;

  Pipeline() throws LineUnavailableException {
    time = TimePacket.clockTime(MILLIS_CLOCK_TICK, TimeUnit.MILLISECONDS);
    mic = MicSource.mic(time);
    input = LpcmToSamples.lpcmToSamples(mic);

    LinkedList<Chart> charts = new LinkedList<>();
    volume = new VolumePanel(input, 100, 100);
    charts.add(volume);
    spectrometer = new SpectrometerPanel(input, 100, 100);
    charts.add(spectrometer);

    sw = new SwingWrapper<>(charts);
  }

  /**
   * Display the charts.
   */
  public void start() {
    sw.displayChartMatrix();
    volume.start();
    spectrometer.start();
    time.subscribe(t -> {
      for (int i = 1; i >= 0; --i) {
        sw.repaintChart(i);
      }
    });
  }

  public static void main(String[] args) throws Exception {
    Pipeline p = new Pipeline();
    p.start();
  }
}
