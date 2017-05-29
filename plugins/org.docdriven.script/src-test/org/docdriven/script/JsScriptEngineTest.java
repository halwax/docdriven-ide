package org.docdriven.script;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class JsScriptEngineTest {

	@Test
	public void stringResponse() throws Exception {
		assertThat(new JsScriptEngine().runAndReturnJSON("return new java.lang.String('test')")).isEqualTo("\"test\"");
	}
	
	@Test
	public void undefinedResponse() throws Exception {
		assertThat(new JsScriptEngine().runAndReturnJSON("return (function(){})()")).isEqualTo("");
	}

}
