package rapi4j.internal.commands.dccman;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.action.ActionCommand;

public class DccManCommands extends ActionCommand {

	public void _dccman(final CommandInterpreter ci) {
		execute("dccman", ci); //$NON-NLS-1$
	}
}
