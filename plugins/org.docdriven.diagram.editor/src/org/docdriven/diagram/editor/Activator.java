package org.docdriven.diagram.editor;

import java.net.ServerSocket;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {
	
	/** Config param key for HTTP port. */
	public static final String CFG_PORT = "port";

	// The plug-in ID
	public static final String PLUGIN_ID = "org.docdriven.diagram.editor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/** Embedded web server for resources. */
	private WebServer server;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		int port = detectFreePort();
		server = new WebServer(port, this.getBundle());
		server.start();
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
		IPreferenceStore pref = getPreferenceStore();
		int port = pref.getInt(CFG_PORT);
		if(port==0) {
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

		pref.setValue(CFG_PORT, port);
		return port;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		server.stop();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}