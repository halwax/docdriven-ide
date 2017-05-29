package org.docdriven.script.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import org.docdriven.script.Activator;
import org.docdriven.script.server.HttpUtils.HttpResponse;
import org.eclipse.core.runtime.ILog;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class ScriptWebServerTest {

	@Test
	public void webRequest() throws Exception {
		
		final BundleContext context = mockBundleContext();	
		Activator activator = initActivator();
		
		activator.start(context);
		
		int webServerPort = activator.getWebServerPort();
		
		String webIndexURL = "http://localhost:"+webServerPort + "/web/index.html";
		HttpResponse response = HttpUtils.sendGet(webIndexURL);
		assertThat(response.getStatus()).isEqualTo(200);
		
		activator.stop(context);
	}

	private Activator initActivator() {
		Activator activator = new Activator();
		ILog scriptLog = mock(ILog.class);
		activator.setScriptLog(scriptLog);
		return activator;
	}
	
	@Test
	public void scriptRequest() throws Exception {
		
		final BundleContext context = mockBundleContext();
		
		Activator activator = initActivator();
		activator.start(context);
		
		int webServerPort = activator.getWebServerPort();
		
		HttpResponse response = HttpUtils.sendPost("http://localhost:"+webServerPort + "/scripts","return {test:1}","application/javascript");
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getContent()).isEqualTo("{\"test\":1}");
		
		activator.stop(context);
	}

	private BundleContext mockBundleContext() {
		
		final BundleContext context = mock(BundleContext.class);
		final Bundle bundle = mock(Bundle.class);
		
		when(context.getBundle()).then(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return bundle;
			}
			
		});
		when(bundle.getResource(anyString())).then(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String path = (String) invocation.getArguments()[0];
				if("./favicon.ico".equalsIgnoreCase(path)) {
					return null;
				}
				return new URL(new File(".").toURI().toURL() + path);
			}
			
		});
		return context;
	}

}
