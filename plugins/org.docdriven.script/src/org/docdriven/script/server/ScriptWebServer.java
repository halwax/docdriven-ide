package org.docdriven.script.server;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import javax.script.ScriptException;

import org.docdriven.script.Activator;

import fi.iki.elonen.NanoHTTPD;

/**
 * Embedded script web server for manage resources for the Js Diagram Editor.
 */
public class ScriptWebServer extends NanoHTTPD {

	private static final String CONTENT_TYPE = "content-type";
	private static final String POST_DATA = "postData";
	
	private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
	private static final String CONTENT_TYPE_APPLICATION_JAVASCRIPT = "application/javascript";
	private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	private static final String CONTENT_TYPE_TEXT_CSS = "text/css";
	private static final String CONTENT_TYPE_IMAGE_GIF = "image/gif";
	private static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
	private static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
	private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	
	private static final String URI_SCRIPTS = "/scripts";

	private final Activator activator;

	public ScriptWebServer(int port, Activator activator) {
		super(port);
		this.activator = activator;
	}

	@Override
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
			Map<String, String> files) {
	
		if(Method.GET.equals(method)) {
			URL resource = activator.getBundle().getResource(uri);
			if (resource != null) {
				try {
					return newChunkedResponse(Response.Status.OK, getMimeType(uri), resource.openStream());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		if(Method.POST.equals(method) && URI_SCRIPTS.equals(uri) && CONTENT_TYPE_APPLICATION_JAVASCRIPT.equalsIgnoreCase(headers.get(CONTENT_TYPE))) {
			String script = files.get(POST_DATA);
			try {
				String json = activator.createScriptEngine().runAndReturnJSON(script);
				return newFixedLengthResponse(Response.Status.OK, CONTENT_TYPE_APPLICATION_JSON, json);
			} catch (ScriptException e) {
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				
				return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, CONTENT_TYPE_TEXT_PLAIN, sw.toString());
			}
		}
		
		return newFixedLengthResponse(Response.Status.NOT_FOUND, getMimeType(uri), "");
	}

	private static String getMimeType(String uri) {

		if (uri.endsWith(".html") || uri.endsWith(".htm")) {
			return CONTENT_TYPE_TEXT_HTML;
		}

		if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
			return CONTENT_TYPE_IMAGE_JPEG;
		}

		if (uri.endsWith(".png")) {
			return CONTENT_TYPE_IMAGE_PNG;
		}

		if (uri.endsWith(".gif")) {
			return CONTENT_TYPE_IMAGE_GIF;
		}

		if (uri.endsWith(".js")) {
			return CONTENT_TYPE_APPLICATION_JAVASCRIPT;
		}

		if (uri.endsWith(".css")) {
			return CONTENT_TYPE_TEXT_CSS;
		}
		
		if(uri.endsWith(".json")) {
			return CONTENT_TYPE_APPLICATION_JSON;
		}

		return CONTENT_TYPE_TEXT_PLAIN;
	}

	public static String getStringFromStream(InputStream input) {
		return getStringFromStream(input, "UTF-8");
	}

	public static String getStringFromStream(InputStream input, String charset) {
		try (Scanner scanner = new Scanner(input, charset)) {
			scanner.useDelimiter("\\Z");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}
}