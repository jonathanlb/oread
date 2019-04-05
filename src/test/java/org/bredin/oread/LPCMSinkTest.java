package org.bredin.oread;

import io.reactivex.Flowable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class LPCMSinkTest {
	@Test
	public void a440Test() throws Exception {
		final int numTicks = 100;
		final int period = 10;
		final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
		Flowable<TimePacket> time = TimePacket.clockTime(period, timeUnit);
		// Flowable<TimePacket> time = TimePacket.logicalTime(period, timeUnit, numTicks);
		Flowable<SamplePacket> sin = SinSource.sinSrc(time, 0.8, 440);
		Flowable<LPCMPacket> wav = SamplesToLPCM.samplesToLPCM(sin);
		LPCMSink sink = new LPCMSink(wav);
		Thread.sleep(timeUnit.toMillis(numTicks * period));
		sink.close();
	}
}
