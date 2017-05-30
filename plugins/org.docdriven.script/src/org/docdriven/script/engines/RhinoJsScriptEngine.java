package org.docdriven.script.engines;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

public class RhinoJsScriptEngine implements IJsScriptEngine {


	static {

		Bundle bundle = FrameworkUtil.getBundle(RhinoJsScriptEngine.class);
		final ClassLoader classLoader = bundle!=null ? bundle.adapt(BundleWiring.class).getClassLoader() : RhinoJsScriptEngine.class.getClassLoader();
		
		// set a custom class loader to find everything in the eclipse universe
		AccessController.doPrivileged(new PrivilegedAction<Object> () {

			@Override
			public Object run() {
				ContextFactory.getGlobal().initApplicationClassLoader(classLoader);
				return null;
			}
			
		});
	}
	
	@Override
	public String runAndReturnJSON(String jsScript) throws ScriptException {
		
		String wrapInResponseFunctionScript = ScriptEngineHelper.wrapInResponseFunction(jsScript);

	    // Creates and enters a Context. The Context stores information
        // about the execution environment of a script.
        Context cx = Context.enter();
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.
            Scriptable scope = cx.initStandardObjects();

            // Now evaluate the string we've collected.
            Object result = cx.evaluateString(scope, wrapInResponseFunctionScript, "<cmd>", 1, null);

            // Convert the result to a string and print it.
            return Context.toString(result);

        } catch(Exception e) {
        	throw new ScriptException(e);
        } finally {
            // Exit from the context.
            Context.exit();
        }
	}

	
}
