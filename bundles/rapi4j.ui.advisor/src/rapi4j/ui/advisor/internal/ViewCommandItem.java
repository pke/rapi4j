package rapi4j.ui.advisor.internal;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.views.IViewDescriptor;

class ViewCommandItem extends CommandItem {

	private final IViewDescriptor viewDescriptor;

	public ViewCommandItem(final ParameterizedCommand command, final IViewDescriptor viewDescriptor, final String desc,
			final ICommandImageService commandImageService) {
		super(command, null, desc, commandImageService);
		this.viewDescriptor = viewDescriptor;
	}

	@Override
	public String getLabel() {
		return "Show " + this.viewDescriptor.getLabel();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return this.viewDescriptor.getImageDescriptor();
	}

	@Override
	public String getDescription() {
		return super.getDescription() != null ? super.getDescription() : this.viewDescriptor.getDescription();
	}
}