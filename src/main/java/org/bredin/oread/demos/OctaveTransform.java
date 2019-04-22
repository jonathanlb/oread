package org.bredin.oread.demos;

import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import org.bredin.oread.LpcmSink;
import org.bredin.oread.LpcmToSamples;
import org.bredin.oread.MathSources;
import org.bredin.oread.MicSource;
import org.bredin.oread.SamplePacket;
import org.bredin.oread.Signals;
import org.bredin.oread.TimePacket;

/**
 * Transform a signal an octave down.
 * There's some hiss/distortion in the signal output...
 */
public class OctaveTransform {
  private static final int MILLIS_CLOCK_TICK = 100;

  private final Flowable<TimePacket> time;
  private final Flowable<SamplePacket> input;
  final Flowable<SamplePacket> octave;

  OctaveTransform(Flowable<TimePacket> time) throws LineUnavailableException {
    this(time, LpcmToSamples.lpcmToSamples(MicSource.mic(time)));
  }

  OctaveTransform(Flowable<TimePacket> time, Flowable<SamplePacket> input) {
    final int panelWidth = 250;
    this.time = time;
    this.input = input;

    octave = Signals.windowDeoverlap(
      Signals.hannWindow( // experiment with triangular window?
        Signals.ifft(
          Signals.octaveDown(
            Signals.fft(
              Signals.windowOverlap(input, 0.5))))),
      0.5);
  }

  /**
   * Take the input mic down an octave and play to speakers.
   * @param args ignored
   */
  public static void main(String[] args) throws Exception {
    Flowable<TimePacket> time = TimePacket.clockTime(MILLIS_CLOCK_TICK, TimeUnit.MILLISECONDS);
    Flowable<SamplePacket> sine = MathSources.sinSrc(time, 440);
    // OctaveTransform p = new OctaveTransform(time, sine);
    OctaveTransform p = new OctaveTransform(time);
    LpcmSink sink = LpcmSink.fromSamples(p.octave);
  }
}
