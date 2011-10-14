package rapi4j.internal.commands.device;

import java.io.File;

import org.eclipse.osgi.framework.console.action.CommandActionContext;

import rapi4j.ActiveSyncDevice;
import rapi4j.ActiveSyncDevice.FileInfo;
import rapi4j.ActiveSyncDevice.FileVisitor;

public class DeviceCommandListAction extends BaseDeviceAction {

	@Override
	public void execute(final ActiveSyncDevice device, final CommandActionContext context) {
		final String spec = context.getArgument(0);
		if (null == spec) {
			throw new IllegalArgumentException("Please specify which files to find. Wildcards (* and ?) are allowed");
		}
		final boolean isVerbose = context.isVerbose();
		final boolean recursive = context.hasOption("r"); //$NON-NLS-1$
		final String specWildcards = new File(spec).getName();
		device.findFiles(spec, new FileVisitor<Object>() {
			public Object found(final FileInfo fileInfo) {
				if (fileInfo.isDirectory()) {
					context.print('[' + fileInfo.getName() + ']');
				} else {
					context.print(fileInfo.getName());
				}
				if (isVerbose) {
					context.print(" " + fileInfo.getWrittenAt()); //$NON-NLS-1$
				}
				context.println(""); //$NON-NLS-1$
				if (fileInfo.isDirectory() && recursive) {
					// TODO: Use the specs given in the argument
					if (spec.equals(specWildcards)) {
						device.findFiles(fileInfo.getName() + "/*", this); //$NON-NLS-1$
					} else {
						device.findFiles(fileInfo.getName() + "/" + specWildcards, this); //$NON-NLS-1$
					}
				}
				return null;
			}
		});
	}
}
