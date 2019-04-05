package org.bredin.oread;

public interface Packet<T> {
	long getStart();
	long getEnd();
	T getData();
}
