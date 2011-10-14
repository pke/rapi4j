package rapi4j.examples.filesview.ui.internal;

import rapi4j.ActiveSyncDevice;
import rapi4j.ActiveSyncDevice.FileInfo;

class FolderModel extends FileModel {

	public FolderModel(final ActiveSyncDevice device, final FolderModel parent, final FileInfo fileInfo) {
		super(device, parent, fileInfo);
	}
}