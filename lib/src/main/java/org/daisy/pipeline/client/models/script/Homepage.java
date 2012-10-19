package org.daisy.pipeline.client.models.script;

/** A homepage. */
public class Homepage {
	public String href;
	public String desc;
	public Homepage(String href, String desc) {
		this.href = href;
		this.desc = desc;
	}
	public String toString() {
		return "<a href=\""+href+"\">"+desc+"</a>";
	}
}