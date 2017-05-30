package org.docdriven.script;

import java.net.ServerSocket;
import java.text.MessageFormat;

import org.docdriven.script.engines.DefaultJsScriptEngine;
import org.docdriven.script.engines.IJsScriptEngine;
import org.docdriven.script.engines.RhinoJsScriptEngine;
import org.docdriven.script.server.ScriptWebServer;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin implements BundleActivator {
	
	public static final String PLUGIN_ID = "org.docdriven.script";
	/** Config param key for HTTP port. */
	public static final String CFG_PORT = "script.port";
	public static final String JS_ENGINE_DEFAULT = "js.engine.default";
	
	private static Activator fInstance;

	public static Activator getDefault() {
		return fInstance;
	}

	private BundleContext fContext;
	private ScriptWebServer server;
	private int webServerPort;
	private ILog scriptLog;
	private boolean useJsEngineDefault;
	
	public void setScriptLog(ILog scriptLog) {
		this.scriptLog = scriptLog;
	}
	
	public ILog getScriptLog() {
		if(scriptLog==null) {
			return getLog();
		}
		return scriptLog;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
		useJsEngineDefault = Boolean.parseBoolean(System.getProperty(JS_ENGINE_DEFAULT, "false"));
		
		webServerPort = detectFreePort();
		server = new ScriptWebServer(webServerPort, this);
		server.start();
		
		getScriptLog().log(new Status(IStatus.INFO, PLUGIN_ID, MessageFormat.format("Script server started on port {0}.",webServerPort)));

		fContext = context;
		fInstance = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		fInstance = null;
		fContext = null;
		
		server.stop();

		super.stop(context);
	}

	public BundleContext getContext() {
		return fContext;
	}
	
	public IJsScriptEngine createScriptEngine() {
		if(useJsEngineDefault) {
			return new DefaultJsScriptEngine();
		}
		
		return new RhinoJsScriptEngine();
	}
	
	/**
	 * Try to find an unused port, in the Dynamic and/or Private Ports Range (49152-65535).
	 * See https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml.
	 * Give up after 100 tries to find an unused port.
	 * 
	 * @return unused port.
	 * @throws Exception
	 *             if port cannot be found
	 */
	private int detectFreePort() throws Exception {
		int port = Integer.parseInt(System.getProperty(CFG_PORT, "0"));
		if (port == 0) {
			port = 49152;
		}

		boolean found = false;
		int tries = 0;
		while (!found && tries < 100) {
			try {
				ServerSocket socket = new ServerSocket(port);
				socket.close();
				found = true;
			} catch (Exception e) {
				port++;
				// ignore exception
			}
			tries++;
		}
		if (!found) {
			throw new Exception("Plugin cannot initialize ports");
		}
		return port;
	}
	
	
	
	public int getWebServerPort() {
		return webServerPort;
	}

}
