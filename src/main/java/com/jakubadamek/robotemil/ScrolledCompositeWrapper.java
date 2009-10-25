package com.jakubadamek.robotemil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

public class ScrolledCompositeWrapper {
	final ScrolledComposite sc1;
	final Composite c1;

	public ScrolledCompositeWrapper(Composite parent) {
		 sc1 = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		 c1 = new Composite(sc1, SWT.NONE);
		 sc1.setContent(c1);
	}

	public Composite getComposite() {
		return c1;
	}

	public ScrolledComposite getParent() {
		return sc1;
	}

	public void setSize() {
		 c1.setSize(c1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
}
