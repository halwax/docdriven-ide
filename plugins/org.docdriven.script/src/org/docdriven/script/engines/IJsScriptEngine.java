package org.docdriven.script.engines;

import javax.script.ScriptException;

public interface IJsScriptEngine {

	String runAndReturnJSON(String jsScript) throws ScriptException;
	
	public class ScriptEngineHelper {
		
		public static String wrapInResponseFunction(String script) {
			
			StringBuffer anonymusFunctionBuilder = new StringBuffer();
			
			anonymusFunctionBuilder.append("JSON.stringify((function(){");
			anonymusFunctionBuilder.append(System.lineSeparator());
			anonymusFunctionBuilder.append(script).append(System.lineSeparator());
			anonymusFunctionBuilder.append("})(),");
			anonymusFunctionBuilder.append(" function(key, value) { ");
			anonymusFunctionBuilder.append("  var returnValue = value;");
			anonymusFunctionBuilder.append("  try {");
			anonymusFunctionBuilder.append("   if (value.getClass() !== null) { /* If Java Object */");
			anonymusFunctionBuilder.append("     if (value instanceof java.lang.Number) { returnValue = 1 * value; }");
			anonymusFunctionBuilder.append("     else if (value instanceof java.lang.Boolean) { returnValue = value.booleanValue(); }");
			anonymusFunctionBuilder.append("     else { returnValue = '' + value; }");
			anonymusFunctionBuilder.append("  }} catch (err) {} ");
			anonymusFunctionBuilder.append("  return returnValue; })");
			
			return anonymusFunctionBuilder.toString();
		}
	}

}
