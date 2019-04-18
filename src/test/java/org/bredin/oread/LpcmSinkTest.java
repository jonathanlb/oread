package org.bredin.oread;

import io.reactivex.Flowable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class LpcmSinkTest {
	@Test
	public void a440Test() throws Exception {
		final int numTicks = 100;
		final int period = 10;
		final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
		Flowable<TimePacket> time = TimePacket.clockTime(period, timeUnit);
		// Flowable<TimePacket> time = TimePacket.logicalTime(period, timeUnit, numTicks);
		Flowable<SamplePacket> sin = MathSources.sinSrc(time, 0.8f, 440);
		Flowable<LpcmPacket> wav = SamplesToLpcm.samplesToLpcm(sin);
		LpcmSink sink = new LpcmSink(wav);
		Thread.sleep(timeUnit.toMillis(numTicks * period));
		sink.close();
	}
}
