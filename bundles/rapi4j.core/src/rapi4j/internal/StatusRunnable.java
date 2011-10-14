package rapi4j.internal;

public interface StatusRunnable<T> {
	T getCurrentStatus();

	void statusChanged(T status);
}