package rapi4j.internal;

import java.net.InetAddress;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import rapi4j.ActiveSyncDevice;
import rapi4j.ActiveSyncDeviceEventConstants;
import rapi4j.ActiveSyncDeviceListener;

public class EventProvider implements ActiveSyncDeviceListener {

	private EventAdmin eventAdmin;

	protected void activate(final ComponentContext context) {
		this.eventAdmin = (EventAdmin) context.locateService("EventAdmin"); //$NON-NLS-1$		
	}

	protected void deactivate(final ComponentContext context) {
	}

	public void onConnected(final ActiveSyncDevice device) {
	}

	public void onNewIpAddress(final ActiveSyncDevice device, final InetAddress ip) {
		final Map<String, String> props = new HashMap<String, String>();
		props.put("device.ip", ip.getHostAddress());
		this.eventAdmin.postEvent(new Event(ActiveSyncDeviceEventConstants.TOPIC_IP_UPDATED, props));
	}

	public void onDisconnected(final ActiveSyncDevice device) {
	}

	public void onEvent(final EventObject event) {
	}
}
