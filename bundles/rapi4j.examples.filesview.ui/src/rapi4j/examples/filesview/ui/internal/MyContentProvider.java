package rapi4j.examples.filesview.ui.internal;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.PendingUpdateAdapter;

class MyContentProvider extends DeferredTreeContentManager implements ITreeContentProvider {

	private final IWorkbenchSiteProgressService service;

	public MyContentProvider(final AbstractTreeViewer viewer, final IViewSite site) {
		super(viewer, site);
		this.service = (IWorkbenchSiteProgressService) site.getService(IWorkbenchSiteProgressService.class);
		if (this.service != null) {
			addUpdateCompleteListener(new JobChangeAdapter() {
				@Override
				public void done(final IJobChangeEvent event) {
					MyContentProvider.this.service.warnOfContentChange();
				}
			});
		}
	}

	public Object getParent(final Object element) {
		return ((IWorkbenchAdapter) element).getParent(element);
	}

	public boolean hasChildren(final Object element) {
		return element instanceof FolderModel;
	}

	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		viewer.getControl().setEnabled(newInput != null);
		if (newInput == null) {
			this.service.warnOfContentChange();
		}
	}

	@Override
	protected PendingUpdateAdapter createPendingUpdateAdapter() {
		return new PendingUpdateAdapter() {
			@Override
			public String getLabel(final Object o) {
				return "Loading...";
			}
		};
	}

	@Override
	protected String getFetchJobName(final Object parent, final IDeferredWorkbenchAdapter adapter) {
		return NLS.bind("Loading {0}...", adapter.getLabel(parent));
	}
}