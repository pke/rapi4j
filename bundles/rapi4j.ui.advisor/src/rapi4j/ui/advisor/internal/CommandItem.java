package rapi4j.ui.advisor.internal;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.ICommandImageService;

class CommandItem extends PlatformObject {
	private final ParameterizedCommand command;
	private final String label;
	private final String desc;
	private final ICommandImageService imageService;

	public CommandItem(final ParameterizedCommand command, final String label, final String desc,
			final ICommandImageService imageService) {
		this.command = command;
		this.label = label;
		this.desc = desc;
		this.imageService = imageService;
	}

	public ParameterizedCommand getCommand() {
		return this.command;
	}

	public String getLabel() {
		return this.label;
	}

	public ImageDescriptor getImageDescriptor() {
		final String id = getCommand().getId();
		final ImageDescriptor imageDescriptor = this.imageService.getImageDescriptor(id, "advisor"); //$NON-NLS-1$
		if (null == imageDescriptor) {
			return this.imageService.getImageDescriptor(id);
		}
		return imageDescriptor;
	}

	public String getDescription() {
		return this.desc;
	}
}
