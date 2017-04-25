package org.docdriven.diagram.editor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import fi.iki.elonen.NanoHTTPD;

/**
 * Embedded web server for manage resources for the Js Diagram Editor.
 */
public class WebServer extends NanoHTTPD {
	/** Reference to bundle. */
	private final Bundle bundle;

	public WebServer(int port, Bundle bundle) {
		super(port);
		this.bundle = bundle;
	}

	@Override
	public Response serve(String uri, Method method,
			Map<String, String> headers, Map<String, String> parms,
			Map<String, String> files) {
		
		// request file to edit
		if (parms.containsKey("file")) {
			return getEditorFile(parms.get("file"));

		// request project relative resources
		} else if (uri.startsWith("/_ws_")) {
			return getRelativeProjectResource(uri.substring(5));
		}
		
		URL resource = bundle.getResource(uri);
		if (resource != null) {
			try {
				return newChunkedResponse(Response.Status.OK, getMimeType(uri), resource.openStream());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		// in other case display debug info
		
        return newFixedLengthResponse(Response.Status.NOT_FOUND, getMimeType(uri), "");
	}
	
	private Response getRelativeProjectResource(String uri) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();  
		IFile file = workspace.getRoot().getFile(new Path(uri));  
		try {
			return newChunkedResponse(Response.Status.OK, getMimeType(uri), file.getContents());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private Response getEditorFile(String f) {
		try {
			
			URL resource = bundle.getResource("/editor/index.html");
			String editorHtml = getStringFromStream(resource.openStream());
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			
			IFile file = workspace.getRoot().getFile(new Path(f));
			if (file.exists()) {
				String content = getStringFromStream(file.getContents(), file.getCharset());
				String javaContent = StringEscapeUtils.escapeJava(content);
				editorHtml = editorHtml.replace("var xmlImportData = null;", "var xmlImportData = \"" + javaContent + "\"");
			}
			
			return newFixedLengthResponse(Response.Status.OK, "text/html", editorHtml);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getMimeType(String uri) {

		if (uri.endsWith(".html") || uri.endsWith(".htm")) {
			return "text/html";
		}

		if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
			return "image/jpeg";
		}

		if (uri.endsWith(".png")) {
			return "image/png";
		}

		if (uri.endsWith(".gif")) {
			return "image/gif";
		}

		if (uri.endsWith(".js")) {
			return "application/javascript";
		}
		
		if (uri.endsWith(".css")) {
			return "text/css";
		}
		
		
		return "text/plain";
	}
	
	public static String getStringFromStream(InputStream input) {
		return getStringFromStream(input, "UTF-8");
	}
	
	public static String getStringFromStream(InputStream input, String charset) {
        try (Scanner scanner = new Scanner(input, charset)){
            scanner.useDelimiter("\\Z");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}