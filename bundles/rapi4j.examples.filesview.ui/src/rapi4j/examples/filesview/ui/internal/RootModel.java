package rapi4j.examples.filesview.ui.internal;

import java.util.Date;

import rapi4j.ActiveSyncDevice;
import rapi4j.ActiveSyncDevice.FileInfo;

class RootModel extends FileModel {

	public RootModel(final ActiveSyncDevice device) {
		super(device, null, new FileInfo() {

			public boolean isDirectory() {
				return true;
			}

			public String getName() {
				return ""; //$NON-NLS-1$
			}

			public Date getWrittenAt() {
				return null;
			}

			public Date getAccessedAt() {
				return null;
			}

			public Date getCreatedAt() {
				return null;
			}

			public long getSize() {
				return 0;
			}
		});
	}
}