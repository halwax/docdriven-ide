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
 */
public class DiagramEditor extends EditorPart {

	private boolean dirty;

	/** Base url of embedded web server.*/
	private final String baseUrl;

	private IDiagramBrowser diagramBrowser;

	/**
	 * Constructor.
	 */
	public DiagramEditor() {
		this.baseUrl = "http://localhost:" + Activator.getDefault().getPreferenceStore().getInt(CFG_PORT);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

		String content = getDiagramXML();

		doSave(monitor, content);
	}

	public void doSave(IProgressMonitor monitor, String content) {
		dirty = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);
		try {
			getFile().setContents(new ByteArrayInputStream(content.getBytes()),
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

		if(OsCheck.OSType.Windows.equals(OsCheck.getOperatingSystemType())) {
			diagramBrowser = new FXDiagramBrowser(parent, baseUrl, this);
		} else {
			diagramBrowser = new SWTDiagramBrowser(parent, baseUrl, this);
		}

	}

	protected void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public void dispose() {
		diagramBrowser.dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
		// empty block
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
		return diagramBrowser.getDiagramXML();
	}

	@Override
	protected void firePropertyChange(int propertyId) {
		super.firePropertyChange(propertyId);
	}

	protected IProgressMonitor getProgressMonitor() {

		IProgressMonitor pm = null;

		IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
		if (manager != null) {
			pm = manager.getProgressMonitor();
		}

		return pm != null ? pm : new NullProgressMonitor();
	}

	public IPath getFilePath() {
		return getFile().getFullPath();
	}

	private IFile getFile() {
		return ((FileEditorInput) getEditorInput()).getFile();
	}

}