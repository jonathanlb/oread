package org.bredin.oread;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Route linear pulse code modulation signals to the speaker.
 * <p>
 * TODO: separate start from constructor
 * TODO: check timing for gaps
 * </p>
 */
public class LpcmSink implements AutoCloseable {
  private static final Logger log = LogManager.getLogger();

  private final Disposable disposable;
  private final SourceDataLine line;
  private final Thread audioPlayerThread;
  private final BlockingQueue<LpcmPacket> buffer;
  private volatile boolean done = false;

  /**
   * Start sending the signal to the default speaker.
   */
  public LpcmSink(Flowable<LpcmPacket> src) throws LineUnavailableException {
    buffer = new LinkedBlockingQueue<>(); // XXX limit!
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, LpcmPacket.AUDIO_FORMAT);
    line = (SourceDataLine) AudioSystem.getLine(info);
    line.open(LpcmPacket.AUDIO_FORMAT);
    audioPlayerThread = new Thread(() -> {
      log.info("Starting audio-player thread");
      line.start();
      try {
        while (!done) {
          final LpcmPacket p = buffer.take();
          final byte[] data = p.getData();
          line.write(data, 0, data.length);
        }
      } catch (InterruptedException e) {
        log.info("Shutting down audio-player thread");
      }
    });
    audioPlayerThread.start();
    disposable = src.subscribe(buffer::put);
  }

  @Override
  public void close() {
    log.info("Closing");
    done = true;
    audioPlayerThread.interrupt();
    disposable.dispose();
    line.close();
  }
}
