package org.docdriven.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

public class JsScriptEngine {
	
	public String runAndReturnJSON(String jsScript) throws ScriptException {
		ScriptEngine scriptEngine = setupEngine();
		String wrapInResponseFunctionScript = wrapInResponseFunction(jsScript);
		try {			
			Object result = scriptEngine.eval(wrapInResponseFunctionScript);
			return toString(result);
		} catch(Exception e) {
			throw new ScriptException(e);
		}
	}

	private String toString(Object eval) {
		return eval==null ? "" : eval.toString();
	}

	private String wrapInResponseFunction(String script) {
		StringBuffer anonymusFunctionBuilder = new StringBuffer();
		anonymusFunctionBuilder.append("JSON.stringify((function(){");
		anonymusFunctionBuilder.append(System.lineSeparator());
		anonymusFunctionBuilder.append(script);
		anonymusFunctionBuilder.append(System.lineSeparator());
		anonymusFunctionBuilder.append("})())");
		return anonymusFunctionBuilder.toString();
	}

	private ScriptEngine setupEngine() throws ScriptException {
		
		ScriptEngineManager engineManager = new ScriptEngineManager();
		
		ScriptEngine engine = null;
		Activator activator = Activator.getDefault();
		if(activator!=null) {
			
			ClassLoader tccl = Thread.currentThread().getContextClassLoader();
			try {
				
				Bundle bundle = Activator.getDefault().getBundle();
			    ClassLoader classLoader = bundle.adapt(BundleWiring.class).getClassLoader();    
				Thread.currentThread().setContextClassLoader(classLoader);
				engine = engineManager.getEngineByExtension("js");
			    
			} finally {
			    Thread.currentThread().setContextClassLoader(tccl);
			}			
		} else {
			engine = engineManager.getEngineByExtension("js");
		}
		
		if (engine == null) {
			throw new ScriptException("Unable to load JS Script Engine");
		}
		
		return engine;
	}
}
