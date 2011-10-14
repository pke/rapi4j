package rapi4j;

@SuppressWarnings("nls")
public final class ActiveSyncDeviceEventConstants {

	public static final String TOPIC_BASE = "rapi4j/device/";

	public static final String TOPIC_CONNECTED = TOPIC_BASE + "CONNECTED";
	public static final String TOPIC_DICONNECTED = TOPIC_BASE + "DISCONNECTED";
	public static final String TOPIC_IP_UPDATED = TOPIC_BASE + "IP_UPDATED";
	public static final String TOPIC_LISTENING = TOPIC_BASE + "LISTENING";
	public static final String TOPIC_ERROR = TOPIC_BASE + "ERROR";
}
