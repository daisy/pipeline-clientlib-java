package org.daisy.pipeline.client.models.script.arguments;

import org.daisy.pipeline.client.models.script.Argument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ArgBoolean extends Argument {
	
	public boolean value;
	
	public ArgBoolean(Argument argument, boolean value) {
		super(argument);
		this.value = value;
	}
	
	public Element asDocumentElement(Document document) {
		Element element = document.createElement("option");
		element.setAttribute("name", name);
		element.setTextContent(value ? "true" : "false");
		return element;
	}
	
}
