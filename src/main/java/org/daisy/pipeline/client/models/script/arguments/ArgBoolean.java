package org.daisy.pipeline.client.models.script.arguments;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** An argument of type "boolean" */
public class ArgBoolean extends Argument {
	
	public Boolean value = null;
	
	public ArgBoolean(Node arg) throws Pipeline2WSException {
		super(arg);
	}
	
	/**abstract
	 * Returns an XML Element representation of the boolean argument, compatible with a jobRequest document.
	 * 
	 * Example:
	 * <d:option name="include-illustrations">true</d:option>
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
		element.setTextContent(value ? "true" : "false");
		
		return element;
	}
	
	/**
	 * Populates the value with the boolean value in the provided document element.
	 * This is the inverse of the "Element asDocumentElement(Document)" method.
	 * 
	 * @param option
	 * @return
	 * @throws Pipeline2WSException
	 */
	@Override
	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
		String booleanText = XPath.selectText("/*/d:option[@name='"+name+"']", jobRequest, Pipeline2WS.ns);
		set(booleanText);
	}

	@Override
	public void set(Object key, Object value) {
		Integer i = null;
		try { i = Integer.parseInt(key+""); }
		catch (NumberFormatException e) {
			Pipeline2WS.logger().warn("Unable to parse integer: "+key);
		}
		
		if (i == 0)
			set(value);
	}
	
	@Override
	public void set(Object value) {
		if ("true".equals(value))
			this.value = true;
			
		else if ("false".equals(value))
			this.value = false;
			
		else Pipeline2WS.logger().warn("Unable to parse boolean value of option '"+name+"': "+value);
	}

	@Override
	public void add(Object value) {
		set(value);
	}

	@Override
	public void remove(Object value) {
		return;
	}

	@Override
	public void clear() {
		return;
	}
	
	@Override
	public String get() {
		return value == null ? null : ""+value;
	}
	
	@Override
	public String get(Object key) {
		Integer i = null;
		try { i = Integer.parseInt(key+""); }
		catch (NumberFormatException e) {
			Pipeline2WS.logger().warn("Unable to parse integer: "+key);
		}
		
		if (i == 0)
			return get();
		else
			return null;
	}

	@Override
	public int size() {
		return 1;
	}
	
}
