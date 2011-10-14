package de.topsystem.activesync.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

import rapi4j.ActiveSyncDevice;

public class AdapterFactory implements IAdapterFactory {

	private class DeviceAdapter extends WorkbenchAdapter {
		@Override
		public String getLabel(final Object object) {
			return ((ActiveSyncDevice) object).getName();
		}
	}

	private IWorkbenchAdapter deviceAdapter;

	IWorkbenchAdapter getDeviceAdapter() {
		if (this.deviceAdapter == null) {
			this.deviceAdapter = new DeviceAdapter();

		}

		return this.deviceAdapter;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof ActiveSyncDevice) {
			if (adapterType == IWorkbenchAdapter.class) {
				return getDeviceAdapter();
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return null;
	}

}
