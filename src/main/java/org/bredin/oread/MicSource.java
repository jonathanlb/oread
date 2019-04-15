package org.bredin.oread;

import io.reactivex.Flowable;
import java.io.IOException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MicSource {
  private static final Logger log = LogManager.getLogger();

  /**
   * Build a signal source from the microphone.
   */
  public static Flowable<LpcmPacket> mic(Flowable<TimePacket> time)
      throws LineUnavailableException {

    DataLine.Info info = new DataLine.Info(TargetDataLine.class, LpcmPacket.AUDIO_FORMAT);
    final TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);
    line.open(LpcmPacket.AUDIO_FORMAT);
    line.start();
    return time.doFinally(() -> {
      log.info("Closing mic {}", info);
      line.close();
    }).map(t -> {
      final int start = t.getStartMillis();
      final int end = t.getEndMillis();
      final int byteTime = (int)((end - start)
                                   * (LpcmPacket.BITS_PER_SAMPLE >> 3)
                                   * LpcmPacket.SAMPLE_RATE * 1e-3);
      final byte[] data = new byte[byteTime];
      final int numRead = line.read(data, 0, data.length);
      if (numRead < byteTime) {
        String msg = "Read " + numRead + " of " + byteTime + " bytes from mic: " + info;
        log.error(msg);
        line.close();
        throw new IOException(msg);
      }
      return new LpcmPacket(start, end, data);
    });
  }
}
