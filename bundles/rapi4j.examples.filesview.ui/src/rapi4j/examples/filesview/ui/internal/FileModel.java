package rapi4j.examples.filesview.ui.internal;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

import rapi4j.ActiveSyncDevice;
import rapi4j.ActiveSyncDevice.FileInfo;
import rapi4j.ActiveSyncDevice.FileVisitor;

class FileModel extends WorkbenchAdapter implements IDeferredWorkbenchAdapter {
	private final FolderModel parent;
	private final ActiveSyncDevice device;
	private final FileInfo fileInfo;

	public FileModel(final ActiveSyncDevice device, final FolderModel parent, final FileInfo fileInfo) {
		this.fileInfo = fileInfo;
		this.device = device;
		this.parent = parent;
	}

	public FileInfo getFileInfo() {
		return this.fileInfo;
	}

	public String getName() {
		return new File(getFileInfo().getName()).getName();
	}

	public FileModel[] children() {
		final ArrayList<FileModel> items = new ArrayList<FileModel>();
		this.device.findFiles(getFileInfo().getName() + "/*", new FileVisitor<Object>() { //$NON-NLS-1$
					public Object found(final FileInfo fileInfo) {
						final FileModel item = fileInfo.isDirectory() ? new FolderModel(FileModel.this.device,
								FileModel.this.parent, fileInfo) : new FileModel(FileModel.this.device,
								FileModel.this.parent, fileInfo);
						items.add(item);
						return null;
					}
				});
		return items.toArray(new FileModel[items.size()]);
	}

	public void fetchDeferredChildren(final Object object, final IElementCollector collector,
			final IProgressMonitor monitor) {

		final FileModel items[] = new FileModel[100];
		final int itemCounter[] = new int[] { 0 };

		this.device.findFiles(getFileInfo().getName() + "/*", new FileVisitor<Object>() { //$NON-NLS-1$
					public Object found(final FileInfo fileInfo) {
						final FileModel item = fileInfo.isDirectory() ? new FolderModel(FileModel.this.device,
								FileModel.this.parent, fileInfo) : new FileModel(FileModel.this.device,
								FileModel.this.parent, fileInfo);
						if (monitor.isCanceled()) {
							return monitor;
						}
						items[itemCounter[0]++] = item;
						if (itemCounter[0] == items.length) {
							collector.add(items, monitor);
							itemCounter[0] = 0;
						}
						return null;
					}
				});
		if (itemCounter[0] > 0) {
			final FileModel dest[] = new FileModel[itemCounter[0]];
			System.arraycopy(items, 0, dest, 0, itemCounter[0]);
			collector.add(dest, monitor);
		}
	}

	public boolean isContainer() {
		return false;
	}

	public ISchedulingRule getRule(final Object object) {
		return null;
	}

	@Override
	public String getLabel(final Object o) {
		return getName();
	}

	@Override
	public Object getParent(final Object o) {
		return this.parent;
	}
}