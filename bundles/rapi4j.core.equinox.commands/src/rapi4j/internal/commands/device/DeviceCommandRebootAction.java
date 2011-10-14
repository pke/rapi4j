package rapi4j.internal.commands.device;

import java.io.IOException;

import org.eclipse.osgi.framework.console.action.CommandActionContext;

import rapi4j.ActiveSyncDevice;

public class DeviceCommandRebootAction extends BaseDeviceAction {

	@Override
	public void execute(final ActiveSyncDevice device, final CommandActionContext context) {
		try {
			device.reboot();
		} catch (final IOException e) {
			// Expected
		}
	}

}
