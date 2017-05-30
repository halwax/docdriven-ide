package org.docdriven.script.engines;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RhinoJsScriptEngineTest {
	@Test
	public void stringResponse() throws Exception {
		assertThat(new RhinoJsScriptEngine().runAndReturnJSON("return new java.lang.String('test');")).isEqualTo("\"test\"");
	}
	
	@Test
	public void undefinedResponse() throws Exception {
		assertThat(new RhinoJsScriptEngine().runAndReturnJSON("return (function(){})()")).isEqualTo("undefined");
	}
	
}
