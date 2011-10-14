package rapi4j.examples.memmonitor.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class MemoryStatusControl extends WorkbenchWindowControlContribution {

	@Override
	protected Control createControl(final Composite parent) {
		return new Label(parent, SWT.NULL);
	}

}
