package org.docdriven.script.ui.bot;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.ui.PlatformUI;

public class UIBot {

	public final static UIBot INSTANCE = new UIBot();
	private SWTWorkbenchBot swtBot;

	public UIBot() {
	}
	
	public static UIBot getInstance() {
		return INSTANCE;
	}

	public void activateWindow() {
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
			}
		});
	}

	public SWTWorkbenchBot getSWTBot() {
		if(swtBot==null) {
			swtBot = new SWTWorkbenchBot();
		}
		return swtBot;
	}

}
