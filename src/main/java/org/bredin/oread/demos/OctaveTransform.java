package org.bredin.oread.demos;

import io.reactivex.Flowable;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bredin.oread.LpcmSink;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MathSources;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.Signals;
import org.bredin.oread.TimePacket;
import org.bredin.oread.diagnostic.Reliability;
import org.bredin.oread.panels.SpectrometerPanel;
import org.bredin.oread.panels.VolumePanel;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

/**
 * Transform a signal an octave down.
 * There's some hiss/distortion in the signal output...
 */
public class OctaveTransform {
  private static final int MILLIS_CLOCK_TICK = 100;

  final Flowable<SamplePacket> octave;

  OctaveTransform(Flowable<TimePacket> time) throws LineUnavailableException {
    this(time, LpcmToSamples.lpcmToSamples(MicSource.mic(time)));
  }

  OctaveTransform(Flowable<TimePacket> time, Flowable<SamplePacket> input) {
    octave = Signals.windowDeoverlap(
      Signals.hannWindow( // experiment with triangular window?
        Signals.ifft(
          Signals.octaveDown(
            Signals.fft(
              Signals.windowOverlap(input, 0.5))))),
      0.5);
  }

  static CommandLine buildCli(String[] args) throws ParseException {
    Options opt = new Options();
    opt.addOption("D", "diagnostic", false, "output diagnostics to stdout");
    opt.addOption("T", "sine", false, "sine wave debug");
    opt.addOption("s", "spectal", false, "graphical spectral output");
    opt.addOption("S", "silent", false, "silent");
    opt.addOption("v", "volume", false, "graphical volume output");

    return new DefaultParser().parse(opt, args);
  }
  /**
   * Take the input mic down an octave and play to speakers.
   * @param args ignored
   */
  public static void main(String[] args) throws Exception {
    CommandLine opt = buildCli(args);
    Flowable<TimePacket> time = TimePacket.clockTime(MILLIS_CLOCK_TICK, TimeUnit.MILLISECONDS);
    OctaveTransform p;

    if (opt.hasOption("T")) {
      Flowable<SamplePacket> sine = MathSources.sinSrc(time, 440);
      p = new OctaveTransform(time, sine);
    } else {
      p = new OctaveTransform(time);
    }

    if (!opt.hasOption("S")) {
      LpcmSink.fromSamples(p.octave);
    }

    if (opt.hasOption("D")) {
      Reliability reliability = new Reliability(p.octave, System.out);
      Reliability.poll(reliability, 10);
    }

    final int panelWidth = 250;
    LinkedList<Chart> charts = new LinkedList<>();
    SpectrometerPanel spectrometerOut;
    VolumePanel volumeOut;
    if (opt.hasOption("s")) {
      spectrometerOut = new SpectrometerPanel(
        p.octave, panelWidth, panelWidth);
      spectrometerOut.setTitle("Output Spectrum");
      spectrometerOut.start();
      charts.add(spectrometerOut);
    }

    if (opt.hasOption("v")) {
      volumeOut = new VolumePanel(
        p.octave, panelWidth, panelWidth);
      volumeOut.setTitle("Output Volume");
      volumeOut.start();
      charts.add(volumeOut);
    }

    final int numCharts = charts.size();
    if (numCharts > 0) {
      SwingWrapper<Chart> sw = new SwingWrapper<>(charts);
      sw.displayChartMatrix();
      time.subscribe(t -> {
        for(int i = numCharts - 1; i >= 0; --i) {
          sw.repaintChart(i);
        }
      });
    }
  }
}
