package org.daisy.pipeline.client.models.script.arguments;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** An argument of type "string" */
public class ArgString extends Argument {
	
	public String value = "";
	
	public ArgString(Node arg) throws Pipeline2WSException {
		super(arg);
	}
	
	/**
	 * Returns an XML Element representation of the string, compatible with a jobRequest document.
	 * 
	 * Example:
	 * <d:option name="language">en</d:option>
	 * 
	 * @param document The document used to create the element.
	 * @return
	 */
	@Override
	public Element asDocumentElement(Document document) {
		if (value == null)
			return null;
		
		Element element = document.createElement("option");
		element.setAttribute("name", name);
		element.setTextContent(value);
		return element;
	}
	
	/**
	 * Populates the string argument with the value from the provided document element.
	 * This is the inverse of the "Element asDocumentElement(Document)" method.
	 * 
	 * @param option
	 * @return
	 * @throws Pipeline2WSException
	 */
	@Override
	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
		value = XPath.selectText("/*/d:option[@name='"+name+"']", jobRequest, Pipeline2WS.ns);
	}

	@Override
	public void set(Object key, Object value) {
		Integer i = null;
		try { i = Integer.parseInt(key+""); }
		catch (NumberFormatException e) { if (Pipeline2WS.debug) System.err.println("Unable to parse integer: "+key); }
		
		if (i == 0)
			set(value);
	}

	@Override
	public void set(Object value) {
		this.value = value == null ? null : value.toString();
	}

	@Override
	public void add(Object value) {
		set(value);
	}

	@Override
	public void remove(Object value) {
		if (this.value != null && this.value.equals(value))
			this.value = null;
	}

	@Override
	public void clear() {
		value = null;
	}

	@Override
	public String get() {
		return value;
	}

	@Override
	public String get(Object key) {
		Integer i = null;
		try { i = Integer.parseInt(key+""); }
		catch (NumberFormatException e) { if (Pipeline2WS.debug) System.err.println("Unable to parse integer: "+key); }
		
		if (i == 0)
			return value;
		else
			return null;
	}

	@Override
	public int size() {
		return value == null ? 0 : 1;
	}

}
