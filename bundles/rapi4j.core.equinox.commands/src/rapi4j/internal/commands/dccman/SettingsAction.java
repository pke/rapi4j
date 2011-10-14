package rapi4j.internal.commands.dccman;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.osgi.framework.console.action.CommandAction;
import org.eclipse.osgi.framework.console.action.CommandActionContext;

import com.microsoft.rapi.IDccMan;

public class SettingsAction implements CommandAction {

	private final AtomicReference<IDccMan> dccManRef = new AtomicReference<IDccMan>();

	protected void bind(final IDccMan dccMan) {
		this.dccManRef.set(dccMan);
	}

	public void execute(final CommandActionContext context) {
		context.println("Displaying the ActiveSync communications settings dialog. This can take a while to show up...");
		this.dccManRef.get().ShowCommSettings();
	}

}
