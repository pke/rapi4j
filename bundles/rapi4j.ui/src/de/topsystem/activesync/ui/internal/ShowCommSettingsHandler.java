package de.topsystem.activesync.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import rapi4j.ActiveSyncDevice;

public class ShowCommSettingsHandler extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		final ServiceReference serviceReference = bundleContext.getServiceReference(ActiveSyncDevice.class.getName());
		try {
			final ActiveSyncDevice device = (ActiveSyncDevice) bundleContext.getService(serviceReference);
			if (device != null) {
				final Dialog dialog = PreferencesUtil.createPropertyDialogOn(HandlerUtil.getActiveShell(event), device,
						null, null, null, 0);
				if (dialog != null) {
					dialog.open();
				}
			}
		} finally {
			bundleContext.ungetService(serviceReference);
		}

		return null;
	}
}
