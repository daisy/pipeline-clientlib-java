package org.daisy.pipeline.client.models.script.arguments;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** An argument of type "string" and sequence="true" */
public class ArgStrings extends Argument {
	
	public List<String> values = new ArrayList<String>();
	
	public ArgStrings(Node arg) throws Pipeline2WSException {
		super(arg);
	}
	
	/**
	 * Returns an XML Element representation of the string sequence, compatible with a jobRequest document.
	 * 
	 * Example:
	 * <d:option name="colors">
	 *     <d:item value="red"/>
	 *     <d:item value="green"/>
	 *     <d:item value="blue"/>
	 * </d:option>
	 * 
	 * @param document The document used to create the element.
	 * @return
	 */
	@Override
	public Element asDocumentElement(Document document) {
		if (values.size() == 0)
			return null;
		
		Element element = document.createElement("option");
		element.setAttribute("name", name);
		
		for (String value : values) {
			Element item = document.createElement("item");
			item.setAttribute("value", value);
			element.appendChild(item);
		}
		
		return element;
	}
	
	/**
	 * Populates the string sequence with values from the provided document element.
	 * This is the inverse of the "Element asDocumentElement(Document)" method.
	 * 
	 * @param option
	 * @return
	 * @throws Pipeline2WSException
	 */
	@Override
	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
		values.clear();
		List<Node> items = XPath.selectNodes("/*/d:option[@name='"+name+"']/d:item", jobRequest, Pipeline2WS.ns);
		for (Node item : items) {
			add(XPath.selectText("@value", item, Pipeline2WS.ns));
		}
	}

	@Override
	public void set(Object key, Object value) {
		Integer i = null;
		try { i = Integer.parseInt(key+""); }
		catch (NumberFormatException e) { if (Pipeline2WS.debug) System.err.println("Unable to parse integer: "+key); }
		
		if (value != null)
			values.set(i, value.toString());
	}

	@Override
	public void set(Object value) {
		clear();
		add(value);
	}

	@Override
	public void add(Object value) {
		if (value != null)
			values.add(value.toString());
	}

	@Override
	public void remove(Object value) {
		values.remove(value);
	}

	@Override
	public void clear() {
		values.clear();
	}

	@Override
	public String get() {
		// there are multiple strings to return; don't return anything
		return null;
	}

	@Override
	public String get(Object key) {
		Integer i = null;
		try { i = Integer.parseInt(key+""); }
		catch (NumberFormatException e) { if (Pipeline2WS.debug) System.err.println("Unable to parse integer: "+key); }
		
		return values.get(i);
	}

	@Override
	public int size() {
		return values.size();
	}

}
