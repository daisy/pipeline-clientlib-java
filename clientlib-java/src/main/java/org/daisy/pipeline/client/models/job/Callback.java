package org.daisy.pipeline.client.models.job;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Node;

/** A callback for job updates. */
public class Callback implements Comparable<Callback> {
	
	public enum Type { messages, status };
	
	public String href;
	public Type type;
	public String frequency;
	
	public Callback(String href, Type type, String frequency) {
		this.href = href;
		this.type = type;
		this.frequency = frequency;
	}
	
	public Callback(Node callbackElement) {
		try {
			this.href = XPath.selectText("@href", callbackElement, Pipeline2Client.ns);
			String type = XPath.selectText("@href", callbackElement, Pipeline2Client.ns);
			for (Type t : Type.values()) {
				if (t.toString().equals(type)) {
					this.type = t;
					break;
				}
			}
			this.frequency = XPath.selectText("@frequency", callbackElement, Pipeline2Client.ns);
			
		} catch (Pipeline2Exception e) {
			Pipeline2Client.logger().error("Unable to parse callback XML", e);
		}
	}
	
	@Override
	public int compareTo(Callback other) {
		assert(href != null);
		assert(type != null);
		return (this.type+this.href).compareTo((other.type+other.href));
	}
	
}
