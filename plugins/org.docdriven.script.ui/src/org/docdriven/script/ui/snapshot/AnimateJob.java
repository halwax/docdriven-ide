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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

public abstract class AnimateJob extends Job {
	private boolean isStop = false;

	private long length = 300;

	private ProgressModifier progressModifier = new ProgressModifier() {
		@Override
		public double modify(double progress) {
			return progress;
		}
	};

	public AnimateJob() {
		super("animate");
		setUser(false);
		setSystem(true);
	}

	private final void animate(final double p) {
		if (Display.getDefault().getThread() == Thread.currentThread()) {
			doAnimate(p);
		} else {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						doAnimate(p);
					} catch (Exception e) {
						stop();
					}
				}
			});
		}
	}

	protected abstract void doAnimate(double p);

	public long getLength() {
		return this.length;
	}

	public ProgressModifier getProgressModifier() {
		return this.progressModifier;
	}

	protected void onEnd() {

	}

	protected void onStart() {

	}

	@Override
	protected final IStatus run(IProgressMonitor monitor) {
		this.isStop = false;

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					onStart();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		long start = System.currentTimeMillis();
		for (long elsp = 0; elsp <= this.length && !this.isStop; elsp = System.currentTimeMillis() - start) {
			double p = elsp / (double) this.length;
			p = this.progressModifier.modify(p);
			p = Math.min(1d, p);
			p = Math.max(0, p);
			animate(p);
		}

		if (this.isStop) {
			return Status.OK_STATUS;
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					animate(1d);
					onEnd();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		return Status.OK_STATUS;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public void setProgressModifier(ProgressModifier progressModifier) {
		this.progressModifier = progressModifier;
	}

	protected void stop() {
		this.isStop = true;
	}

}