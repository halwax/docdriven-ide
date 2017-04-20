package org.docdriven.diagram.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;

public class SWTDiagramBrowser implements IDiagramBrowser {

	/** Browser. */
	private Browser browser;
	private BrowserFunction markEditorDirtyFunction;
	private SaveEditorFunction saveEditorFunction;
	
	public SWTDiagramBrowser(Composite parent, String baseUrl, DiagramEditor diagramEditor) {
		browser = new Browser(parent, SWT.NONE);
		browser.setJavascriptEnabled(true);
		browser.setUrl(baseUrl + "/editor/index.html" + 
				"?file=" + diagramEditor.getFilePath());		
		
		markEditorDirtyFunction = new MarkEditorDirtyFunction(diagramEditor, browser, "javaMarkEditorDirty");
		saveEditorFunction = new SaveEditorFunction(diagramEditor, browser, "javaSaveEditor");
	}

	public void dispose() {
		markEditorDirtyFunction.dispose();
		saveEditorFunction.dispose();
		browser.dispose();
	}

	public String getDiagramXML() {
		return (String) browser.evaluate("return editorUi.getXml()");
	}
	
	static class SaveEditorFunction extends BrowserFunction {
		private DiagramEditor diagramEditor;

		SaveEditorFunction(DiagramEditor diagramEditor, Browser browser, String name) {
			super(browser, name);
			this.diagramEditor = diagramEditor;
		}

		public Object function(Object[] arguments) {
			String content = (String) arguments[0];
			diagramEditor.doSave(diagramEditor.getProgressMonitor(),content);
			return null;
		}
		
	}
	
	static class MarkEditorDirtyFunction extends BrowserFunction {
		private DiagramEditor diagramEditor;

		MarkEditorDirtyFunction(DiagramEditor diagramEditor, Browser browser, String name) {
			super(browser, name);
			this.diagramEditor = diagramEditor;
		}

		public Object function(Object[] arguments) {
			diagramEditor.setDirty((boolean) arguments[0]);
			diagramEditor.firePropertyChange(IEditorPart.PROP_DIRTY);
			return null;
		}
	}
	
}
