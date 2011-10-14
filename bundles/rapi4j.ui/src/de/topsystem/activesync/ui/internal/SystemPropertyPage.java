package de.topsystem.activesync.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import rapi4j.ActiveSyncDevice;

public class SystemPropertyPage extends PropertyPage implements IWorkbenchPropertyPage, IExecutableExtension {

	public SystemPropertyPage() {
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(final Composite parent) {
		final ActiveSyncDevice device = (ActiveSyncDevice) getElement().getAdapter(ActiveSyncDevice.class);
		return new Composite(parent, SWT.NONE);
	}

	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data)
			throws CoreException {
	}
}
