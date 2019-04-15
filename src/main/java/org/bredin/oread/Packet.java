package org.bredin.oread;

public interface Packet<T> {
  int getStartMillis();

  int getEndMillis();

  T getData();
}
