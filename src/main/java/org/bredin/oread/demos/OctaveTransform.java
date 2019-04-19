package org.bredin.oread.demos;

import io.reactivex.Flowable;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import org.bredin.oread.LpcmPacket;
import org.bredin.oread.LpcmSink;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MathSources;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.Signals;
import org.bredin.oread.TimePacket;
import org.bredin.oread.panels.SpectrometerPanel;
import org.bredin.oread.panels.VolumePanel;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

/**
 * Transform a signal an octave down.
 * - There's quite a bit of distortion.
 * - Fails to keep up in real time.
 */
public class OctaveTransform {
  private static final int MILLIS_CLOCK_TICK = 100;

  private final SwingWrapper<Chart> sw;
  private final Flowable<TimePacket> time;
  private final Flowable<SamplePacket> input;
  final Flowable<SamplePacket> octave;

  private final VolumePanel volumeOut;
  private final SpectrometerPanel spectrometerOut;

  OctaveTransform(Flowable<TimePacket> time) throws LineUnavailableException {
    this(time, LpcmToSamples.lpcmToSamples(MicSource.mic(time)));
  }

  OctaveTransform(Flowable<TimePacket> time, Flowable<SamplePacket> input) {
    final int panelWidth = 250;
    this.time = time;
    this.input = input;
    octave = Signals.ifft(Signals.octaveDown(Signals.fft(input)));

    LinkedList<Chart> charts = new LinkedList<>();
    volumeOut = new VolumePanel(octave, panelWidth, panelWidth);
    volumeOut.setTitle("Output Volume");
    charts.add(volumeOut);

    spectrometerOut = new SpectrometerPanel(octave, panelWidth, panelWidth);
    spectrometerOut.setTitle("Output Spectrum");
    charts.add(spectrometerOut);

    sw = new SwingWrapper<>(charts);
  }

  /**
   * Display the charts.
   */
  public void start() {
    sw.displayChartMatrix();
    volumeOut.start();
    spectrometerOut.start();
    time.subscribe(t -> {
      sw.repaintChart(0);
      sw.repaintChart(1);
    });
  }

  /**
   * Take the input mic down an octave and play to speakers.
   * @param args ignored
   */
  public static void main(String[] args) throws Exception {
    Flowable<TimePacket> time = TimePacket.clockTime(MILLIS_CLOCK_TICK, TimeUnit.MILLISECONDS);
    // Flowable<SamplePacket> sine = MathSources.sinSrc(time, 440);
    OctaveTransform p = new OctaveTransform(time);
    p.start();
    LpcmSink sink = LpcmSink.fromSamples(p.octave);
  }
}
