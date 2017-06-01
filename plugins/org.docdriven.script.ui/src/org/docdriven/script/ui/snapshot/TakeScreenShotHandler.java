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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

public class TakeScreenShotHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		new SnapshotHook(Display.getDefault()).start();
		return null;
	}

}