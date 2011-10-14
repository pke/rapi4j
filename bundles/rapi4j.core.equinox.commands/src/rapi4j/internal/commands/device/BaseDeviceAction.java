package rapi4j.internal.commands.device;

import org.eclipse.osgi.framework.console.action.CommandAction;
import org.eclipse.osgi.framework.console.action.CommandActionContext;

import rapi4j.ActiveSyncDevice;

public abstract class BaseDeviceAction implements CommandAction {
	private ActiveSyncDevice device;

	void bindActiveSyncDevice(final ActiveSyncDevice device) {
		this.device = device;
	}

	public void execute(final CommandActionContext context) throws Exception {
		execute(this.device, context);
	}

	protected abstract void execute(ActiveSyncDevice device, CommandActionContext context);
}
