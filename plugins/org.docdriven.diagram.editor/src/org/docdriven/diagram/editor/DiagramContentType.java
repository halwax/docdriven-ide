package org.docdriven.diagram.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;
import org.eclipse.core.runtime.content.XMLContentDescriber;

public class DiagramContentType implements ITextContentDescriber {

	private ITextContentDescriber xmlContentDescriber;
	
	public DiagramContentType() {
		xmlContentDescriber = new XMLContentDescriber();
	}
	
	public int describe(Reader contents, IContentDescription description) throws IOException {
		return xmlContentDescriber.describe(contents, description);
	}

	public int describe(InputStream contents, IContentDescription description) throws IOException {
		return xmlContentDescriber.describe(contents, description);
	}

	public QualifiedName[] getSupportedOptions() {
		return xmlContentDescriber.getSupportedOptions();
	}

}
