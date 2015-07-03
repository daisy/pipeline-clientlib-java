package org.daisy.pipeline.client.models.script.arguments;

import java.util.HashMap;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.models.script.Argument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** An argument of type "parameters" */
public class ArgParameters extends Argument {
	
	public Map<String,String> parameters = new HashMap<String,String>();
	
	public ArgParameters(Node arg) throws Pipeline2WSException {
		super(arg);
	}
	
	/** For chaining */
	public Argument put(String name, String value) {
		this.parameters.put(name, value);
		return this;
	}
	
	/**
	 * Returns an XML Element representation of the parameter set, compatible with a jobRequest document.
	 * 
	 * Example:
	 * TODO: parameter arguments are not implemented yet.
	 * 
	 * @param document The document used to create the element.
	 * @return
	 */
	@Override
	public Node asDocumentElement(Document document) {
		if (parameters.size() == 0)
			return null;
		
		return null; // TODO: parameter ports are not implemented yet
	}
	
	/**
	 * Populates the parameter set with values from the provided document element.
	 * This is the inverse of the "Element asDocumentElement(Document)" method.
	 * 
	 * @param option
	 * @return
	 * @throws Pipeline2WSException
	 */
	@Override
	public void parseFromJobRequest(Node option) throws Pipeline2WSException {
		// TODO: parameter ports are not implemented yet
	}

	@Override
	public void set(Object key, Object value) {
		if (key != null && value != null)
			parameters.put(key.toString(), value.toString());
	}

	@Override
	public void set(Object value) {
		// do nothing; a key is required for each parameter
	}

	@Override
	public void add(Object value) {
		// do nothing; a key is required for each parameter
	}

	@Override
	public void remove(Object value) {
		parameters.remove(value);
	}

	@Override
	public void clear() {
		parameters.clear();
	}

	@Override
	public String get() {
		// do nothing; a key is required for each parameter
		return null;
	}

	@Override
	public String get(Object key) {
		return parameters.get(key);
	}

	@Override
	public int size() {
		return parameters.size();
	}

	
}
