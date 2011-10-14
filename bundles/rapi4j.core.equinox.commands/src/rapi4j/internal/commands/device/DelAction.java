package rapi4j.internal.commands.device;

import java.io.File;

import org.eclipse.osgi.framework.console.action.CommandActionContext;

import rapi4j.ActiveSyncDevice;
import rapi4j.ActiveSyncDevice.FileInfo;
import rapi4j.ActiveSyncDevice.FileVisitor;

import com.microsoft.rapi.Rapi;
import com.sun.jna.examples.win32.Kernel32;

public class DelAction extends BaseDeviceAction {

	@Override
	public void execute(final ActiveSyncDevice device, final CommandActionContext context) {
		final String filePath = context.getArgument(0);
		if (filePath == null) {
			throw new IllegalArgumentException("You need to specify a file/directory to delete");
		}
		if ("windows".equalsIgnoreCase(filePath.replace("/", "").replace("\\", ""))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			throw new IllegalArgumentException("Deleting Windows directory is not allowed.");
		}
		final int attributes = Rapi.instance.CeGetFileAttributes(filePath);
		if ((attributes & Kernel32.FILE_ATTRIBUTE_DIRECTORY) == Kernel32.FILE_ATTRIBUTE_DIRECTORY) {
			if (!context.hasOption("r")) { //$NON-NLS-1$
				// Delete directory
			}
		}

		final String path = new File(filePath).getParent();
		device.findFiles(filePath, new FileVisitor<Object>() {
			public Object found(final FileInfo fileInfo) {
				final String file = fileInfo.getName();
				if (fileInfo.isDirectory()) {
					device.findFiles(file + "/*", this); //$NON-NLS-1$
					device.removeDirectory(file);
				} else {
					device.deleteFile(file);
				}
				return null;
			}
		});
	}

}
