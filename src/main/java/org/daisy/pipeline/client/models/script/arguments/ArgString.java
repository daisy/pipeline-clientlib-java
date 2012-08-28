package org.daisy.pipeline.client.models.script.arguments;

import org.daisy.pipeline.client.models.script.Argument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ArgString extends Argument {
	
	public String value = "";
	
	public ArgString(Argument arg, String value) {
		super(arg);
		this.value = value;
	}
	
	public Element asDocumentElement(Document document) {
		Element element = document.createElement("option");
		element.setAttribute("name", name);
		element.setTextContent(value);
		return element;
	}
	
}
