package rapi4j.ui.softreset.internal;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import rapi4j.ActiveSyncDevice;

/**
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public class ResetHandler extends AbstractHandler {

	ActiveSyncDevice device;

	public void setActiveSyncDevice(final ActiveSyncDevice device) {
		this.device = device;
	}

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			this.device.reboot();
		} catch (final IOException e) {
			// Thats expected. Sometimes this throws errors from Rapi.invoke
		}
		return null;
	}

}
