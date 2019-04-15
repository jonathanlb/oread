package org.bredin.oread;

import io.reactivex.Flowable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MicSourceTest {
	@Test
	public void openMicTest() throws Exception {
		Flowable<TimePacket> clock = TimePacket.clockTime(1, TimeUnit.MILLISECONDS);
		Flowable<LpcmPacket> mic = MicSource.mic(clock);
		LpcmPacket p = mic.blockingFirst();
		assertTrue(p.getData().length > 0);
	}
}