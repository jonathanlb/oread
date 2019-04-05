package org.bredin.oread;

import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

public class MicSource {
	private static Logger log = LogManager.getLogger();

	public static Flowable<LPCMPacket> mic(Flowable<TimePacket> time) throws LineUnavailableException {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, LPCMPacket.AUDIO_FORMAT);
		final TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);
		line.open(LPCMPacket.AUDIO_FORMAT);
		line.start();
		return time.doFinally(() -> {
			log.info("Closing mic {}", info);
			line.close();
		}).map(t -> {
			final long start = t.getStart();
			final long end = t.getEnd();
			final int byteTime = (int)((end - start) * (LPCMPacket.BITS_PER_SAMPLE >> 3) * LPCMPacket.SAMPLE_RATE * 1e-3);
			final byte[] data = new byte[byteTime];
			final int numRead = line.read(data, 0, data.length);
			if (numRead < byteTime) {
				String msg = "Read " + numRead + " of " + byteTime + " bytes from mic: " + info ;
				log.error(msg);
				line.close();
				throw new IOException(msg);
			}
			return new LPCMPacket(start, end, data);
		});
	}
}
