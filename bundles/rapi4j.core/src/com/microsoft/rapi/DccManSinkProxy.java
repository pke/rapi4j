package com.microsoft.rapi;


public class DccManSinkProxy {
	private final DccManSink javaObject;

	/**
	 * Pointer to the native proxy.
	 */
	int nativeProxy;

	public DccManSinkProxy(final DccManSink javaObject) {
		this.javaObject = javaObject;
	}

	void onLogIpAddr(final int ip) {
		this.javaObject.onLogIpAddr(ip);
	}

	void onLogTerminated() {
		this.javaObject.onLogTerminated();
	}

	void onLogActive() {
		this.javaObject.onLogActive();
	}

	void onLogInactive() {
		this.javaObject.onLogInactive();
	}

	void onLogAnswered() {
		this.javaObject.onLogAnswered();
	}

	void onLogListen() {
		this.javaObject.onLogListen();
	}

	void onLogDisconnection() {
		this.javaObject.onLogDisconnection();
	}

	void onLogError() {
		this.javaObject.onLogError();
	}
}
