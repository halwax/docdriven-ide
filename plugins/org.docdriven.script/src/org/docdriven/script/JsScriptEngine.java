package org.docdriven.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JsScriptEngine {
	
	public String runAndReturnJSON(String jsScript) throws ScriptException {
		ScriptEngine scriptEngine = setupEngine();
		return toString(scriptEngine.eval(wrapInResponseFunction(jsScript)));
	}

	private String toString(Object eval) {
		return eval==null ? "" : eval.toString();
	}

	private String wrapInResponseFunction(String script) {
		StringBuffer anonymusFunctionBuilder = new StringBuffer();
		anonymusFunctionBuilder.append("JSON.stringify((function(){");
		anonymusFunctionBuilder.append(script);
		anonymusFunctionBuilder.append("})())");
		return anonymusFunctionBuilder.toString();
	}

	private ScriptEngine setupEngine() throws ScriptException {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("js");

		if (engine == null) {
			throw new ScriptException("Unable to load JS Script Engine");
		}
		
		return engine;
	}
}
