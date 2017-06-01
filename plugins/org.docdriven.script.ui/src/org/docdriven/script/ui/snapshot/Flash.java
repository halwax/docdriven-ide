/*******************************************************************************
 * Copyright (c) 2017, Jeeeyul Lee.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeeeyul Lee - initial API and implementation and/or initial documentation
 * See https://github.com/jeeeyul/pde-tools.
 *******************************************************************************/
package org.docdriven.script.ui.snapshot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Flash {
	
	private Control target;
	private Shell shell;
	private AnimateJob job;
	private CaptureBoundsComputer boundsComputer = new CaptureBoundsComputer();

	public AnimateJob getJob() {
		if (job == null) {
			job = new AnimateJob() {
				@Override
				protected void doAnimate(double p) {
					if (shell != null && !shell.isDisposed()) {
						int alpha = (int) (255 - (255 * p));
						shell.setAlpha(alpha);
					}
				}

				@Override
				protected void onEnd() {
					shell.dispose();
				}
			};

			job.setLength(600);
		}
		return job;
	}

	public Flash(Control target) {
		create(target.getDisplay());
		setTarget(target);
	}

	private void setTarget(Control target) {
		if (this.target == target) {
			return;
		}
		this.target = target;

		if (target == null) {
			shell.setVisible(false);
			return;
		}

		Rectangle bounds = boundsComputer.compute(target);
		shell.setBounds(bounds);
		shell.setVisible(true);

	}

	private void create(Display display) {
		shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP | SWT.TOOL);
		shell.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		shell.setSize(0, 0);
		shell.setVisible(false);
		shell.update();
	}

	public void start() {
		getJob().schedule();
	}
}