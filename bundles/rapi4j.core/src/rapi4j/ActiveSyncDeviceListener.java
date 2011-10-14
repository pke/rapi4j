package rapi4j;

import java.net.InetAddress;
import java.util.EventObject;

public interface ActiveSyncDeviceListener {
	void onConnected(ActiveSyncDevice device);

	void onNewIpAddress(ActiveSyncDevice device, InetAddress ip);

	void onDisconnected(ActiveSyncDevice device);

	void onEvent(EventObject event);
}
