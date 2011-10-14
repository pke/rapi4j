package rapi4j.internal.commands.device;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.action.ActionCommand;

public class DeviceCommands extends ActionCommand {

	public void _wince(final CommandInterpreter ci) {
		execute("wince", ci); //$NON-NLS-1$
	}
}
