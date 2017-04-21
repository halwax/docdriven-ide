package org.docdriven.diagram.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class FXDiagramBrowser implements IDiagramBrowser {

	private FXCanvas canvas;
	private WebView browser;

	public FXDiagramBrowser(Composite parent, String baseUrl, DiagramEditor diagramEditor) {
		canvas = new FXCanvas(parent, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		BorderPane borderPane = new BorderPane();
		Scene scene = new Scene(borderPane);
		canvas.setScene(scene);

		browser = new WebView();
		WebEngine webEngine = browser.getEngine();
		
		JSObject jsobj = (JSObject) webEngine.executeScript("window");
		jsobj.setMember("javaEditorObj", new EditorObject(diagramEditor));
		
		borderPane.setCenter(browser);

		webEngine.load(baseUrl + "/editor/index.html" + "?file=" + diagramEditor.getFilePath());
	}

	@Override
	public void dispose() {
		canvas.dispose();
	}

	@Override
	public String getDiagramXML() {
		return (String) browser.getEngine().executeScript("return editorUi.getXml()");
	}
	
	
	
	public static class EditorObject {
		
		private DiagramEditor diagramEditor;

		EditorObject(DiagramEditor diagramEditor) {
			this.diagramEditor = diagramEditor;
		}

		public void markEditorDirty(boolean dirty) {
			diagramEditor.setDirty(dirty);
			diagramEditor.firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		
		public void saveEditor(String content) {
			diagramEditor.doSave(diagramEditor.getProgressMonitor(),content);
		}
	}

}
