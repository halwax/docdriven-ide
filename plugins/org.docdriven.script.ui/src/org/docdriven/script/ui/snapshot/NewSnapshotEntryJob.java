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
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.docdriven.script.ui.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class NewSnapshotEntryJob extends Job implements ISchedulingRule {

	private ImageData imageData;
	private String controlType;
	private ShellInfo shellInfo;

	public NewSnapshotEntryJob(ImageData imageData) {
		super("Create a new snapshot");
		this.imageData = imageData;
		setSystem(true);
		setRule(this);
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	public String getControlType() {
		return controlType;
	}

	public ShellInfo getShellInfo() {
		return shellInfo;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof NewSnapshotEntryJob) {
			return true;
		}
		return false;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			
			String newFileName = generateNewFileName();
			String snapshotDir = System.getProperty("script.snapshot.dir");
			if(snapshotDir==null) {				
				snapshotDir = System.getProperty("java.io.tmpdir");
			}
			
			File snapshotFile = new File(snapshotDir, newFileName);
			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { imageData };

			FileOutputStream fos = new FileOutputStream(snapshotFile);
			imageLoader.save(fos, SWT.IMAGE_PNG);
			fos.close();

		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}

		return Status.OK_STATUS;
	}

	private String generateNewFileName() {
		String prefix = null;
		int lastPeriod = controlType.lastIndexOf(".");
		prefix = controlType.substring(lastPeriod + 1);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		prefix += "_" + format.format(new Date());

		return prefix + ".png";
	}

	public void setControlType(String controlType) {
		this.controlType = controlType;
	}

	public void setShellInfo(ShellInfo shellInfo) {
		this.shellInfo = shellInfo;
	}

}