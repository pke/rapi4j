package rapi4j.ui.advisor.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.IViewDescriptor;

import rapi4j.ActiveSyncDevice;

/**
 * OSGi service that reacts on Mobile device attachments.
 * 
 * <p>
 * It consumes advisor extensions and displays them in a (filterable) list.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public class Advisor implements IAdapterFactory {

	private String title = "Mobile Device attached";
	private IWorkbench workbench;

	private class Adapter extends WorkbenchAdapter {
		@Override
		public ImageDescriptor getImageDescriptor(final Object object) {
			final CommandItem item = (CommandItem) object;
			return item.getImageDescriptor();
		}

		@Override
		public String getLabel(final Object object) {
			final CommandItem item = (CommandItem) object;
			return item.getLabel();
		}
	};

	private Adapter adapter;
	private ActiveSyncDevice device;

	protected void bind(final IAdapterManager adapterManager) {
		adapterManager.registerAdapters(this, CommandItem.class);
	}

	protected void unbind(final IAdapterManager adapterManager) {
		adapterManager.unregisterAdapters(this);
	}

	protected void bind(final ActiveSyncDevice device) {
		this.device = device;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof CommandItem) {
			if (this.adapter == null) {
				this.adapter = new Adapter();
			}
			return this.adapter;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class<?>[] { IWorkbenchAdapter.class };
	}

	protected void activate(final Map<String, Object> properties) {
		if (properties.containsKey("dialog.title")) { //$NON-NLS-1$
			this.title = (String) properties.get("dialog.title"); //$NON-NLS-1$
		}

		final ICommandService commandService = (ICommandService) this.workbench.getService(ICommandService.class);
		final ICommandImageService imageService = (ICommandImageService) this.workbench
				.getService(ICommandImageService.class);
		final IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
				"rapi4j.ui.advisor", "actions"); //$NON-NLS-1$//$NON-NLS-2$

		final List<CommandItem> items = new ArrayList<CommandItem>();

		for (final IConfigurationElement config : extensionPoint.getConfigurationElements()) {
			if ("command".equals(config.getName())) { //$NON-NLS-1$
				try {
					final ParameterizedCommand command = commandService.deserialize(config.getAttribute("command")); //$NON-NLS-1$
					if (Boolean.getBoolean(config.getAttribute("hidden"))) { //$NON-NLS-1$
						executeCommand(command);
					} else {
						String label = config.getAttribute("label"); //$NON-NLS-1$
						if (label == null || "".equals(label)) { //$NON-NLS-1$
							label = command.getName();
						}
						final String desc = config.getAttribute("description"); //$NON-NLS-1$
						if ("org.eclipse.ui.views.showView".equals(command.getId())) { //$NON-NLS-1$
							final IViewDescriptor viewDesc = Advisor.this.workbench.getViewRegistry().find(
									(String) command.getParameterMap().get("org.eclipse.ui.views.showView.viewId")); //$NON-NLS-1$
							if (viewDesc != null) {
								items.add(new ViewCommandItem(command, viewDesc, desc, imageService));
							}
						} else {
							items.add(new CommandItem(command, label, desc, imageService));
						}
					}
				} catch (final NotDefinedException e) {
				} catch (final SerializationException e) {
				} catch (final InvalidRegistryObjectException e) {
				}
			}
		}

		if (items.isEmpty()) {
			return;
		}

		final ElementListSelectionDialog dialog = new ElementListSelectionDialog(null, new WorkbenchLabelProvider());
		dialog.setElements(items.toArray());
		dialog.setMultipleSelection(true);
		dialog.setStatusLineAboveButtons(true);
		dialog.setTitle(this.title);
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(final Object[] items) {
				if (items.length == 1) {
					final CommandItem item = (CommandItem) items[0];
					return new Status(IStatus.OK, "rapi4j", item.getDescription()); //$NON-NLS-1$
				}
				return new Status(IStatus.OK, "rapi4j", NLS.bind("{0} actions selected", items.length)); //$NON-NLS-1$
			}
		});
		dialog.setMessage(NLS.bind("A mobile device {0} was attached. What do you want to do now?", this.device
				.getName()));

		new WorkbenchJob("Displaying device attachment advisor dialog") { //$NON-NLS-1$
			{
				setSystem(true);
			}

			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				dialog.open();
				final Object[] results = dialog.getResult();
				if (results != null) {
					for (final Object result : results) {
						if (result instanceof CommandItem) {
							executeCommand(((CommandItem) result).getCommand());
						}
					}
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	private void executeCommand(final ParameterizedCommand command) {
		try {
			new WorkbenchJob("Executing " + command.getName()) {

				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor) {
					try {
						final IHandlerService handlerService = (IHandlerService) Advisor.this.workbench
								.getService(IHandlerService.class);
						handlerService.executeCommand(command, null);
					} catch (final Exception e) {
						e.printStackTrace();
					}
					return Status.OK_STATUS;
				}

			}.schedule();
		} catch (final NotDefinedException e) {
		}
	}

	protected void bind(final IWorkbench workbench) {
		this.workbench = workbench;
	}
}
