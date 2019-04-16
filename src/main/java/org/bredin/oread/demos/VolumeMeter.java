package org.bredin.oread.demos;

import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.TimePacket;
import org.bredin.oread.panels.VolumePanel;
import org.knowm.xchart.SwingWrapper;

/**
 * Plot a bar graph representing volume/signal-strength changing over time.
 * <p>
 * To poll microphone demo, run with:
 * mvn exec:java -Dexec.mainClass=org.bredin.oread.demos.VolumeMeter
 * </p>
 */
public class VolumeMeter {
  private static final int HEIGHT = 600;
  private static final int WIDTH = 800;
  private static final int MS_SAMPLE_PERIOD = 100;

  /** CLI ignores arguments. */
  public static void main(String[] args) throws Exception {
    Flowable<TimePacket> time = TimePacket.clockTime(MS_SAMPLE_PERIOD, TimeUnit.MILLISECONDS);
    Flowable<LpcmPacket> mic = MicSource.mic(time);
    Flowable<SamplePacket> micSamples = LpcmToSamples.lpcmToSamples(mic);
    VolumePanel volume = new VolumePanel(micSamples, WIDTH, HEIGHT);

    SwingWrapper<VolumePanel> sw = new SwingWrapper<>(volume);
    sw.displayChart();
    volume.start();
    time.subscribe(t -> sw.repaintChart());
  }
}

