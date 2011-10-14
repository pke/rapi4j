package rapi4j.internal.commands.device;

import org.eclipse.osgi.framework.console.action.CommandActionContext;

import rapi4j.ActiveSyncDevice;

public class DeviceCommandIdAction extends BaseDeviceAction {

	@Override
	public void execute(final ActiveSyncDevice device, final CommandActionContext context) {
		context.println(device.toString());
	}
}
