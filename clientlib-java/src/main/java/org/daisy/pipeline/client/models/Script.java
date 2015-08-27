package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * A representation of a Pipeline 2 script.
 * 
 * @author jostein
 */
public class Script implements Comparable<Script> {
	
	private String id;
	private String href; // xs:anyURI
	private List<String> inputFilesets = new ArrayList<String>();
	private List<String> outputFilesets = new ArrayList<String>();
	private String niceName;
	private String description;
	private String version;
	private String homepage; // xs:anyURI
	private Map<String, Argument> inputs = new HashMap<String, Argument>();
	private Map<String, Argument> outputs = new HashMap<String, Argument>();
	
	// lazy load document; don't parse it until necessary
	private Node scriptNode = null;
	private boolean lazyLoaded = false;
	
	// TODO: implement these somewhere (maybe in some API/interface, or maybe here) to harmonize with ScriptRegistry.java
	public Script getScript(URI href);
    public Script getScript(String name);
    public List<Script> getScripts();
	
	/**
	 * Parse the script described by the provided Pipeline2WSResponse.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/script.xml
	 * 
	 * @param response
	 * @throws Pipeline2Exception
	 */
	public Script(Pipeline2WSResponse response) throws Pipeline2Exception {
		if (response.status != 200)
			throw new Pipeline2Exception(response.status+" "+response.statusName+": "+response.statusDescription);
		scriptNode = response.asXml();
	}
	
	/**
	 * Parse the script described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/script.xml
	 * 
	 * @param scriptXml
	 * @throws Pipeline2Exception
	 */
	public Script(Node scriptXml) throws Pipeline2Exception {
		scriptNode = scriptXml;
	}
	
	private void lazyLoad() {
		if (lazyLoaded || scriptNode == null) {
			return;
		}
		
		try {
			// select root element if the node is a document node
			if (scriptNode instanceof Document)
				scriptNode = XPath.selectNode("/d:script", scriptNode, Pipeline2Client.ns);
			
			this.id = XPath.selectText("@id", scriptNode, Pipeline2Client.ns);
			this.href = XPath.selectText("@href", scriptNode, Pipeline2Client.ns);
			this.niceName = XPath.selectText("d:nicename", scriptNode, Pipeline2Client.ns);
			this.description = XPath.selectText("d:description", scriptNode, Pipeline2Client.ns);
			this.homepage = XPath.selectText("d:homepage", scriptNode, Pipeline2Client.ns);
			
			List<Node> inputNodes = XPath.selectNodes("d:input", scriptNode, Pipeline2Client.ns);
			List<Node> optionNodes = XPath.selectNodes("d:option", scriptNode, Pipeline2Client.ns);
			List<Node> outputNodes = XPath.selectNodes("d:output", scriptNode, Pipeline2Client.ns);
			
			for (Node inputNode : inputNodes) {
				this.inputs.add(new Argument(inputNode));
			}
	
			for (Node optionNode : optionNodes) {
				this.inputs.add(new Argument(optionNode));
			}
	
			for (Node outputNode : outputNodes) {
				this.outputs.add(new Argument(outputNode));
			}
			
		} catch (Pipeline2Exception e) {
			Pipeline2Client.logger().error("Unable to parse script XML", e);
		}
		
		lazyLoaded = true;
	}
	
//	/**
//	 * Populate the script arguments with the values contained in the given jobRequest document.
//	 * 
//	 * @param jobRequest
//	 * @throws Pipeline2WSException 
//	 */
//	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
//		if (jobRequest instanceof Document)
//			jobRequest = ((Document)jobRequest).getDocumentElement();
//		
//		for (Argument arg : inputs) {
//			arg.parseFromJobRequest(jobRequest);
//		}
//	}
	
	/**
	 * Get an argument (inputs, options or outputs) by its name.
	 * 
	 * @param name
	 * @param kind
	 * @return
	 */
	public Argument getArgument(String name) {
		for (Argument arg : inputs) {
			if (name.equals(arg.name))
				return arg;
		}
		for (Argument arg : outputs) {
			if (name.equals(arg.name))
				return arg;
		}
		return null;
	}
	
	/**
	 * Parse the list of scripts described by the provided Pipeline2WSResponse.
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2Exception
	 */
	public static List<Script> getScripts(Pipeline2WSResponse response) throws Pipeline2Exception {
		if (response.status != 200)
			throw new Pipeline2Exception(response.status+" "+response.statusName+": "+response.statusDescription);
		List<Script> scripts = parseScriptsXml(response.asXml());
		Collections.sort(scripts);
		return scripts;
	}
	
	/**
	 * Parse the list of scripts described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/scripts.xml
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2Exception
	 */
	public static List<Script> parseScriptsXml(Node scriptsXml) throws Pipeline2Exception {
		
		List<Script> scripts = new ArrayList<Script>();
		
		// select root element if the node is a document node
		if (scriptsXml instanceof Document)
			scriptsXml = XPath.selectNode("/d:scripts", scriptsXml, Pipeline2Client.ns);
		
		List<Node> scriptNodes = XPath.selectNodes("d:script", scriptsXml, Pipeline2Client.ns);
		for (Node scriptNode : scriptNodes) {
			scripts.add(new Script(scriptNode));
		}
		
		return scripts;
	}

	public int compareTo(Script other) {
		if (id == null) return 1;
		if (other.id == null) return -1;
		return id.compareTo(other.id);
	}
	
	// getters and setters (to ensure lazy loading)
    public String getId() { lazyLoad(); return id; }
    public String getHref() { lazyLoad(); return href; }
    public List<String> getInputFilesets() { lazyLoad(); return inputFilesets; }
    public List<String> getOutputFilesets() { lazyLoad(); return outputFilesets; }
    public String getNicename() { lazyLoad(); return niceName; }
    public String getDescription() { lazyLoad(); return description; }
    public String getVersion() { lazyLoad(); return version; }
    public String getHomepage() { lazyLoad(); return homepage; }
    public List<Argument> getInputs() { lazyLoad(); return inputs; }
    public List<Argument> getOutputs() { lazyLoad(); return outputs; }
//  public void setId(String id) { lazyLoad(); this.id = id; }
//  public void setHref(String href) { lazyLoad(); this.href = href; }
//  public void setInputFilesets(List<String> inputFilesets) { lazyLoad(); this.inputFilesets = inputFilesets; }
//  public void setOutputFilesets(List<String> outputFilesets) { lazyLoad(); this.outputFilesets = outputFilesets; }
//  public void setNicename(String nicename) { lazyLoad(); this.nicename = nicename; }
//  public void setDescription(String description) { lazyLoad(); this.description = description; }
//  public void setVersion(String version) { lazyLoad(); this.version = version; }
//  public void setHomepage(String homepage) { lazyLoad(); this.homepage = homepage; }
//  public void setInputs(List<Argument> inputs) { lazyLoad(); this.inputs = inputs; }
//  public void setOutputs(List<Argument> outputs) { lazyLoad(); this.outputs = outputs; }
	
}
