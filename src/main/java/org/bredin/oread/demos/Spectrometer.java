package org.bredin.oread.demos;

import io.reactivex.Flowable;

import java.util.concurrent.TimeUnit;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.TimePacket;
import org.bredin.oread.panels.SpectrometerPanel;
import org.knowm.xchart.SwingWrapper;

/**
 * To poll microphone demo, run with:
 * mvn exec:java -Dexec.mainClass=org.bredin.oread.demos.Spectrometer
 */
public class Spectrometer {
  private static final int HEIGHT = 600;
  private static final int WIDTH = 800;
  private static final int MS_SAMPLE_PERIOD = 10;

  /** Entry point ignores CLI arguments. */
  public static void main(String[] args) throws Exception {
    Flowable<TimePacket> time = TimePacket.clockTime(MS_SAMPLE_PERIOD, TimeUnit.MILLISECONDS);
    Flowable<LpcmPacket> mic = MicSource.mic(time);
    Flowable<SamplePacket> micSamples = LpcmToSamples.lpcmToSamples(mic);
    SpectrometerPanel s = new SpectrometerPanel(micSamples, WIDTH, HEIGHT);

    SwingWrapper<SpectrometerPanel> sw = new SwingWrapper<>(s);
    sw.displayChart();
    s.start();
    time.subscribe(t -> sw.repaintChart());
  }
}
