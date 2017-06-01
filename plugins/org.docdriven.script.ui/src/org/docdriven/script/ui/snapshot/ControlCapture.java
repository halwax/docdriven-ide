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

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ControlCapture {
	
	private CaptureBoundsComputer boundsComputer = new CaptureBoundsComputer();
	
	public Image capture(Control control){
		control.redraw();
		control.update();
		
		Rectangle bounds = boundsComputer.compute(control);

		Image image = new Image(getDisplay(), bounds.width, bounds.height);
		GC gc = new GC(getDisplay());
		gc.copyArea(image, bounds.x, bounds.y);
		gc.dispose();
		return image;
	}

	public Image capture(Shell shell){
		shell.redraw();
		shell.update();
		
		Rectangle clientArea = shell.getClientArea();
		SWTExtensions.setLocation(clientArea, shell.toDisplay(SWTExtensions.getLocation(clientArea)));
		if(shell.getMenuBar() != null) {
			SWTExtensions.resize(SWTExtensions.translate(clientArea, 0, -SWTExtensions.getMenubarHeight()), 0, SWTExtensions.getMenubarHeight());
		}
		Image image = new Image(getDisplay(), clientArea.width, clientArea.height);
		GC gc = new GC(getDisplay());
		gc.copyArea(image, clientArea.x, clientArea.y);
		gc.dispose();
		return image;
	}

	private Display getDisplay(){
		if(Display.getCurrent() == null) {
			throw new SWTException("Invalid Thread Exception");
		}
		return Display.getCurrent();
	}
}