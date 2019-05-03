package org.bredin.oread.diagnostic;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.bredin.oread.Packet;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class Reliability implements Disposable {
  private final PrintStream snk;
  private final Disposable disposable;

  private int numPackets = 0;
  private int numGaps = 0;
  private int expectedMillis = 0;
  private long started = 0;

  @Override
  public void dispose() {
    disposable.dispose();
  }

  @Override
  public boolean isDisposed() {
    return disposable.isDisposed();
  }

  public Reliability(Flowable<? extends Packet> src, PrintStream snk) {
    this.snk = snk;
    this.disposable = src.subscribe(
      this::accept,
      this::onError,
      this::complete);
  }

  public synchronized void complete() {
    long now = System.currentTimeMillis();
    double packetsPSec = 1000 * numPackets / (double)(now - started);
    snk.println("   #Packets: " + numPackets);
    snk.println("Packets/sec: " + packetsPSec);
    snk.println("      #Gaps: " + numGaps);
    // TODO: time duration comparison
  }

  synchronized void accept(Packet packet) {
    int start = packet.getStartMillis();
    int end = packet.getEndMillis();

    ++numPackets;
    if (started == 0) {
      started = System.currentTimeMillis();
      expectedMillis = start;
    }

    if (start != expectedMillis) {
      ++numGaps;
    }
    expectedMillis = end;
  }

  void onError(Throwable err) {
    dispose();
  }

  public static void poll(Reliability r, int periodS) {
    Flowable.interval(periodS, TimeUnit.SECONDS).subscribe(
      t -> r.complete());
  }
}
