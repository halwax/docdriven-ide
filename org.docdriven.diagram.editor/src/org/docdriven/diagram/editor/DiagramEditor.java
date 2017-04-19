package org.docdriven.diagram.editor;

import static org.docdriven.diagram.editor.Activator.CFG_PORT;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * The main editor.
 * 
 */
public class DiagramEditor extends EditorPart {
	/** Browser. */
	private Browser browser;
	private BrowserFunction function;
	private boolean dirty;
	
	/** Base url of embedded web server.*/
	private final String baseUrl;
	
	/**
	 * Constructor.
	 */
	public DiagramEditor() {
		this.baseUrl = "http://localhost:" + Activator.getDefault().getPreferenceStore().getInt(CFG_PORT);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		
		dirty = false;
		
		firePropertyChange(IEditorPart.PROP_DIRTY);
		
		String content = getDiagramXML();
		try {
			((FileEditorInput) getEditorInput()).getFile().setContents(new ByteArrayInputStream(content.getBytes()),
					true, true, monitor);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doSaveAs() {
		performSaveAs(getProgressMonitor());
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setJavascriptEnabled(true);
		browser.setUrl(baseUrl + "/editor/index.html" + 
				"?file=" + ((FileEditorInput) getEditorInput()).getFile().getFullPath());
		function = new MarkEditorDirtyCallback(this, browser, "javaMarkEditorDirty");
	}

	@Override
	public void dispose() {
		function.dispose();
		browser.dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
		// empty block
	}

	static class MarkEditorDirtyCallback extends BrowserFunction {
		private DiagramEditor editor;

		MarkEditorDirtyCallback(DiagramEditor editor, Browser browser, String name) {
			super(browser, name);
			this.editor = editor;
		}

		public Object function(Object[] arguments) {
			editor.dirty = (boolean) arguments[0];
			editor.firePropertyChange(IEditorPart.PROP_DIRTY);
			return null;
		}
	}

	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		final IEditorInput input = getEditorInput();

		final IEditorInput newInput;

		SaveAsDialog dialog = new SaveAsDialog(shell);

		IFile original = (input instanceof IFileEditorInput) ? ((IFileEditorInput) input).getFile() : null;
		if (original != null) {
			dialog.setOriginalFile(original);
		} else {
			dialog.setOriginalName(input.getName());
		}

		dialog.create();

		if (dialog.open() == Window.CANCEL) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}

		IPath filePath = dialog.getResult();
		if (filePath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile file = workspace.getRoot().getFile(filePath);

		dirty = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);
		String content = getDiagramXML();
		try {
			if (file.exists()) {
				file.setContents(new ByteArrayInputStream(content.getBytes()), true, true, progressMonitor);
			} else {
				file.create(new ByteArrayInputStream(content.getBytes()), true, progressMonitor);
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		newInput = new FileEditorInput(file);
		setInput(newInput);
		setPartName(newInput.getName());
	}

	private String getDiagramXML() {
		return (String) browser.evaluate("return editorUi.getXml()");
	}

	protected IProgressMonitor getProgressMonitor() {

		IProgressMonitor pm = null;

		IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
		if (manager != null) {
			pm = manager.getProgressMonitor();
		}

		return pm != null ? pm : new NullProgressMonitor();
	}

}