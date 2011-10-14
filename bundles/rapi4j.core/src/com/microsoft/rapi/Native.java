package com.microsoft.rapi;

import java.util.EventListener;


import com4j.COM4J;

public class Native {
	//private static final Logger logger = LoggerFactory.getLogger(Native.class);

	static {
		System.loadLibrary("rapi4j"); //$NON-NLS-1$
	}

	public static native int createDccManSink(EventListener sink);

	public static IDccManSink createIDccManSink(final EventListener sink) {
		final int dccManSink = Native.createDccManSink(sink);
		//logger.debug("Sink: {}", dccManSink); //$NON-NLS-1$
		return COM4J.wrapInstance(IDccManSink.class, dccManSink);
	}
}
