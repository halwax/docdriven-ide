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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CaptureBoundsComputer {
	
	public Rectangle compute(Control control){
		Rectangle result = new Rectangle(0, 0, 0, 0);
		Composite parent = control.getParent();
		if(parent==null) {
			return result;
		}
		Point location = parent.toDisplay(control.getLocation());
		SWTExtensions.setLocation(result, location);
		SWTExtensions.setSize(result, control.getSize());
		return result;
	}
	
	public Rectangle compute(Shell shell){
		Rectangle result = new Rectangle(0, 0, 0, 0);
		SWTExtensions.setSize(result, SWTExtensions.getSize(shell.getClientArea()));
		SWTExtensions.setLocation(result, shell.toDisplay(0, 0));
		return result;
	}
	


}