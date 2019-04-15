package org.bredin.oread;

import io.reactivex.Flowable;
import static org.junit.Assert.*;

import io.reactivex.disposables.Disposable;
import org.junit.Test;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class SinSourceTest {
	@Test
	public void a440Test() {
		final int NUM_TICKS = 3;
		LinkedList<SamplePacket> data = new LinkedList<>();
		Flowable<TimePacket> time = TimePacket.logicalTime(1, TimeUnit.MILLISECONDS, NUM_TICKS);
		Flowable<SamplePacket> sin = SinSource.sinSrc(time, 440);
		Disposable disposable = sin.subscribe(data::add);

		assertEquals(NUM_TICKS, data.size());

		SamplePacket p = data.getLast();
		assertEquals(2, p.getStartMillis());
		assertEquals(3, p.getEndMillis());
		assertEquals(LpcmPacket.SAMPLE_RATE / 1000, p.getData().length);

		disposable.dispose();
	}
}
