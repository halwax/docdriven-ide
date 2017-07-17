package org.docdriven.diagram.editor;

import static org.docdriven.diagram.editor.Activator.CFG_PORT;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The main editor.
 */
public class DiagramEditor extends EditorPart {

	private static final String XML_UTF8_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	private boolean dirty;

	/** Base url of embedded web server. */
	private final String baseUrl;

	private IDiagramBrowser diagramBrowser;
	
	private static final String GET_XML_SCRIPT = "editorUi.getXml()";
	private static final String GET_JSON_SCRIPT = "editorUi.getJsonStr(true)";
	private static final String GET_EXPORT_JSON_SCRIPT = "getExportJson()";
	private static final String GET_SVG_SCRIPT = "mxUtils.getXml(editorUi.editor.graph.getSvg('#ffffff', 1, 0))";	

	/**
	 * Constructor.
	 */
	public DiagramEditor() {
		this.baseUrl = "http://localhost:" + Activator.getDefault().getPreferenceStore().getInt(CFG_PORT);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

		String content = "";
		
		if(isJavaScriptInput()) {
			content = getDiagramJson();
		} else {			
			content = getDiagramXML();
		}
		doSave(monitor, content);

	}

	public void doSave(IProgressMonitor monitor, String content) {
		dirty = false;
		boolean js = isJavaScriptInput();
		firePropertyChange(IEditorPart.PROP_DIRTY);
		try {
			if(!js && !content.startsWith(XML_UTF8_HEADER)) {
				String xmlContent = XML_UTF8_HEADER + content;
				content = xmlContent;
			}
			getFile().setContents(new ByteArrayInputStream(content.getBytes()), true, true, monitor);
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

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		form.setText("Diagram");

		IToolBarManager toolBarManager = form.getToolBarManager();
		ImageDescriptor webLinkImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.browser",
				"$nl$/icons/obj16/external_browser.png");
		ImageDescriptor binaryFileImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.ide",
				"$nl$/icons/full/etool16/build_exec.png");
		ImageDescriptor textFileImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.ide",
				"$nl$/icons/full/obj16/welcome_editor.png");

		String url = baseUrl + "/editor/index.html" + "?file=" + getFilePath();
		
		toolBarManager.add(new Action("Open URL", webLinkImageDesc) {
			@Override
			public void run() {

				org.eclipse.swt.program.Program.launch(url);

			}
		});
		
		toolBarManager.add(new Action("Export SVG", textFileImageDesc) {
			@Override
			public void run() {

				String diagramSVG = getDiagramSVG();
				IContainer folder = getFile().getParent();
				IFile svgFile = folder.getFile(new Path(getFile().getName() + ".svg"));

				Job job = new Job("Export " + svgFile.getName()) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							ByteArrayInputStream svgStream = new ByteArrayInputStream(diagramSVG.getBytes());
							if(svgFile.exists()) {
								svgFile.setContents(svgStream, true, true, monitor);
							} else {								
								svgFile.create(svgStream, true, monitor);
							}
						} catch (CoreException e) {
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();

			}
		});
		toolBarManager.add(new Action("Export PNG", binaryFileImageDesc) {
			@Override
			public void run() {
				
				IContainer folder = getFile().getParent();
				IFile pngFile = folder.getFile(new Path(getFile().getName() + ".png"));
				
				final GraphExport graphExport = getGraphExport();

				Job job = new Job("Export " + pngFile.getName()) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						
						try {
							URI locationURI = pngFile.getLocationURI();
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							new Renderer().renderImage(locationURI.toURL().toString(), "png", graphExport.getWidth(), graphExport.getHeight(), Color.WHITE, graphExport.getExportXml(), out);
							InputStream input = new ByteArrayInputStream(out.toByteArray());
							if(pngFile.exists()) {
								pngFile.setContents(input, true, true, monitor);
							} else {								
								pngFile.create(input, true, monitor);
							}
						} catch (CoreException e) {
							return e.getStatus();
						} catch (IOException | SAXException | ParserConfigurationException e) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format("Can't export diagram png to {0}!", pngFile.getLocationURI()), e);
						}
						return Status.OK_STATUS;						
					}
					
				};
				job.schedule();
			}
		});
		toolBarManager.update(true);

		Composite body = form.getBody();
		body.setLayout(new FillLayout());
		final Composite browserComposite = toolkit.createComposite(body, SWT.BORDER);
		browserComposite.setLayout(new FillLayout());

		if (OsCheck.OSType.Windows.equals(OsCheck.getOperatingSystemType())) {
			diagramBrowser = new FXDiagramBrowser(browserComposite, url, this);
		} else {
			diagramBrowser = new SWTDiagramBrowser(browserComposite, url, this);
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
		String content = "";
		if(isJsonFile(file)) {
			content = getDiagramJson();
		} else {			
			content = getDiagramXML();
		}
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

	private boolean isJavaScriptInput() {
		return isJsonFile(getFile());
	}	
	
	private boolean isJsonFile(IFile file) {
		return file.getFileExtension().equalsIgnoreCase("js");
	}

	private String getDiagramXML() {
		return diagramBrowser.executeScript(GET_XML_SCRIPT);
	}
	
	private String getDiagramJson() {
		return diagramBrowser.executeScript(GET_JSON_SCRIPT);
	}

	private String getDiagramSVG() {
		return diagramBrowser.executeScript(GET_SVG_SCRIPT);
	}
	
	private GraphExport getGraphExport() {
		return GraphExport.fromJson(diagramBrowser.executeScript(GET_EXPORT_JSON_SCRIPT));
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
	
	public static class GraphExport {

		private String exportXml;

		private int width;

		private int height;

		private GraphExport(String exportXml, int width, int height) {
			this.exportXml = exportXml;
			this.width = width;
			this.height = height;
		}

		public String getExportXml() {
			return exportXml;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public static GraphExport fromJson(String json) {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode;
			try {
				jsonNode = mapper.readTree(json);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			String xml = jsonNode.get("xml").asText();
			int width = jsonNode.get("w").asInt();
			int height = jsonNode.get("h").asInt();

			return new GraphExport(xml, width, height);
		}

	}

}