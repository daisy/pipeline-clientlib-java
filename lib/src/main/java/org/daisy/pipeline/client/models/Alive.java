package org.daisy.pipeline.client.models;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "alive" response from the Pipeline 2 Web Service.
 * 
 * @author jostein
 */
public class Alive {
	
	public Boolean error = null;
	
	public Boolean authentication = null;
	public Boolean local = null;
	public String version = null;
	
	// ---------- Constructors ----------
	
	/**
	 * Parse the "alive"-XML described by the provided Pipeline2WSResponse.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/alive.xml
	 * 
	 * @param response
	 * @throws Pipeline2WSException
	 */
	public Alive(Pipeline2WSResponse response) throws Pipeline2WSException {
		this(response.asXml());
	}
	
	/**
	 * Parse the "alive"-XML described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/alive.xml
	 * 
	 * @param aliveXml
	 * @throws Pipeline2WSException
	 */
	public Alive(Node aliveXml) throws Pipeline2WSException {
		if (XPath.selectNode("/d:error", aliveXml, Pipeline2WS.ns) != null) {
			error = true;
			return;
		}
		error = false;
		
		// select root element if the node is a document node
		if (aliveXml instanceof Document)
			aliveXml = XPath.selectNode("/d:alive", aliveXml, Pipeline2WS.ns);
		
		this.authentication = "true".equals(XPath.selectText("@authentication", aliveXml, Pipeline2WS.ns));
		this.local = "true".equals(XPath.selectText("@local", aliveXml, Pipeline2WS.ns));
		this.version = XPath.selectText("@version", aliveXml, Pipeline2WS.ns);
	}
	
}
