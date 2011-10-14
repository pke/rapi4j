package rapi4j.examples.filesview.ui.internal;

import java.net.URL;
import java.text.DateFormat;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.FrameworkUtil;

import rapi4j.ActiveSyncDevice;
import eu.wwuk.eclipse.extsvcs.core.ComponentContext;
import eu.wwuk.eclipse.extsvcs.core.InjectedComponent;

public class FileBrowser extends ViewPart implements InjectedComponent {

	public static final String ID = "rapi4j.examples.filesview.ui.Explorer"; //$NON-NLS-1$

	private TreeViewer treeViewer;
	private ComponentContext context;

	public void setComponentContext(final ComponentContext context) {
		this.context = context;
	}

	private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	protected void initContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		final Menu menu = menuMgr.createContextMenu(this.treeViewer.getControl());
		this.treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, this.treeViewer);
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());

		this.treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		getSite().setSelectionProvider(this.treeViewer);
		this.treeViewer.getTree().setHeaderVisible(true);
		this.treeViewer.getTree().setLinesVisible(true);
		this.treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		this.treeViewer.addTreeListener(new ITreeViewerListener() {

			public void treeExpanded(final TreeExpansionEvent event) {
				FileBrowser.this.treeViewer.getTree().getColumn(1).pack();
			}

			public void treeCollapsed(final TreeExpansionEvent event) {
			}
		});

		this.treeViewer.setContentProvider(new MyContentProvider(this.treeViewer, getViewSite()));

		initContextMenu();

		// Sorts the tree.
		this.treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int category(final Object element) {
				return element instanceof FolderModel ? 0 : 1;
			}
		});

		this.treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				if (!event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection) {
					FileBrowser.this.treeViewer.expandToLevel(((IStructuredSelection) event.getSelection())
							.getFirstElement(), 1);
				}
			}
		});

		final LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

		final URL url = FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path(
				"$nl$/icons/e16/Folder Closed.png"), null); //$NON-NLS-1$
		final Image folderImage = resourceManager.createImage(ImageDescriptor.createFromURL(url));

		TreeViewerColumn column = new TreeViewerColumn(this.treeViewer, SWT.LEFT);
		column.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final IWorkbenchAdapter fileModel = (IWorkbenchAdapter) cell.getElement();
				if (fileModel instanceof FolderModel) {
					// cell.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
				}
				cell.setText(fileModel.getLabel(fileModel));
				if (fileModel instanceof FolderModel) {
					cell.setImage(folderImage);
				}
			}
		});
		column.getColumn().setText("Name");
		column.getColumn().setWidth(200);

		column = new TreeViewerColumn(this.treeViewer, SWT.LEFT);
		column.getColumn().setText("Size");
		column.getColumn().setWidth(100);
		column.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(final ViewerCell cell) {
				final IWorkbenchAdapter adapter = (IWorkbenchAdapter) cell.getElement();
				if (adapter instanceof FileModel && !(adapter instanceof FolderModel)) {
					final long size = ((FileModel) adapter).getFileInfo().getSize();

					if ((int) size == -1) {
						cell.setText("unknown");
					} else {
						cell.setText(String.valueOf(size));
					}
				}
			}
		});

		column = new TreeViewerColumn(this.treeViewer, SWT.LEFT);
		column.getColumn().setText("Created");
		column.getColumn().setWidth(100);
		column.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final IWorkbenchAdapter adapter = (IWorkbenchAdapter) cell.getElement();
				if (adapter instanceof FileModel) {
					cell.setText(FileBrowser.this.dateFormat.format(((FileModel) adapter).getFileInfo().getCreatedAt()));
				}
			}
		});
		column = new TreeViewerColumn(this.treeViewer, SWT.LEFT);
		column.getColumn().setText("Accessed");
		column.getColumn().setWidth(100);
		column.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(final ViewerCell cell) {
				final IWorkbenchAdapter adapter = (IWorkbenchAdapter) cell.getElement();
				if (adapter instanceof FileModel) {
					cell.setText(FileBrowser.this.dateFormat
							.format(((FileModel) adapter).getFileInfo().getAccessedAt()));
				}
			}
		});
	}

	@Override
	public void setFocus() {
		this.treeViewer.getControl().setFocus();
	}

	public void setActiveSyncDevice(final ActiveSyncDevice device) {
		new UIJob("") {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				FileBrowser.this.treeViewer.setInput(new RootModel(device));
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	public void unsetActiveSyncDevice(final ActiveSyncDevice device) {
		new UIJob("") {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				FileBrowser.this.treeViewer.setInput(null);
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void dispose() {
		this.context.disposed();
		super.dispose();
	}
}
