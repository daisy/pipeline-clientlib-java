package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.List;
import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.client.models.script.Author;
import org.daisy.pipeline.client.models.script.Homepage;
import org.daisy.pipeline.client.models.script.arguments.ArgBoolean;
import org.daisy.pipeline.client.models.script.arguments.ArgFile;
import org.daisy.pipeline.client.models.script.arguments.ArgFiles;
import org.daisy.pipeline.client.models.script.arguments.ArgString;
import org.daisy.pipeline.client.models.script.arguments.ArgStrings;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * A representation of a Pipeline 2 script.
 * 
 * @author jostein
 */
public class Script {
	
	public String id;
	public String href;
	public String nicename;
	public String desc;
	public Homepage homepage;
	public Author author;
	public List<Argument> arguments;
	
	public Script() {
		this.arguments = new ArrayList<Argument>();
	}
	
	/**
	 * Parse the script described by the provided Pipeline2WSResponse.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/script.xml
	 * 
	 * @param response
	 * @throws Pipeline2WSException
	 */
	public Script(Pipeline2WSResponse response) throws Pipeline2WSException {
		this();
		if (response.status != 200)
			throw new Pipeline2WSException(response.status+" "+response.statusName+": "+response.statusDescription);
		parseScriptXml(response.asXml());
	}
	
	/**
	 * Parse the script described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/script.xml
	 * 
	 * @param scriptXml
	 * @throws Pipeline2WSException
	 */
	public Script(Node scriptXml) throws Pipeline2WSException {
		this();
		parseScriptXml(scriptXml);
	}
	
	private void parseScriptXml(Node scriptXml) throws Pipeline2WSException {
		// select root element if the node is a document node
		if (scriptXml instanceof Document)
			scriptXml = XPath.selectNode("/d:script", scriptXml, Pipeline2WS.ns);
		
		this.id = XPath.selectText("@id", scriptXml, Pipeline2WS.ns);
		this.href = XPath.selectText("@href", scriptXml, Pipeline2WS.ns);
		this.nicename = XPath.selectText("d:nicename", scriptXml, Pipeline2WS.ns);
		this.desc = XPath.selectText("d:description", scriptXml, Pipeline2WS.ns);
		String homepageHref = XPath.selectText("d:homepage", scriptXml, Pipeline2WS.ns);
		if (homepageHref != null)
			this.homepage = new Homepage(homepageHref, homepageHref);
		
		List<Node> inputNodes = XPath.selectNodes("d:input", scriptXml, Pipeline2WS.ns);
		List<Node> optionNodes = XPath.selectNodes("d:option", scriptXml, Pipeline2WS.ns);
		List<Node> outputNodes = XPath.selectNodes("d:output", scriptXml, Pipeline2WS.ns);
		
		for (Node inputNode : inputNodes) {
			if ("true".equals(XPath.selectText("@sequence", inputNode, Pipeline2WS.ns))) {
				this.arguments.add(new ArgFiles(inputNode));
			} else {
				this.arguments.add(new ArgFile(inputNode));
			}
		}

		for (Node optionNode : optionNodes) {
			boolean sequence = "true".equals(XPath.selectText("@sequence", optionNode, Pipeline2WS.ns));
			String xsdType = XPath.selectText("@type", optionNode, Pipeline2WS.ns);
			
			if ("boolean".equals(xsdType))
				this.arguments.add(new ArgBoolean(optionNode));
			
			else if ("anyFileURI".equals(xsdType) && sequence)
				this.arguments.add(new ArgFiles(optionNode));
			
			else if ("anyFileURI".equals(xsdType) || "anyDirURI".equals(xsdType))
				this.arguments.add(new ArgFile(optionNode));
			
			else if ("string".equals(xsdType) && sequence)
				this.arguments.add(new ArgStrings(optionNode));
			
			else
				this.arguments.add(new ArgString(optionNode));
		}

		for (Node outputNode : outputNodes) {
			if ("true".equals(XPath.selectText("@sequence", outputNode, Pipeline2WS.ns))) {
				this.arguments.add(new ArgFiles(outputNode));
			} else {
				this.arguments.add(new ArgFile(outputNode));
			}
		}
	}
	
	/**
	 * Populate the script arguments with the values contained in the given jobRequest document.
	 * 
	 * @param jobRequest
	 * @throws Pipeline2WSException 
	 */
	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
		if (jobRequest instanceof Document)
			jobRequest = ((Document)jobRequest).getDocumentElement();
		
		for (Argument arg : arguments) {
			arg.parseFromJobRequest(jobRequest);
		}
	}
	
	/**
	 * Get an argument by its name and kind.
	 * 
	 * @param name
	 * @param kind
	 * @return
	 */
	public Argument getArgument(String name, String kind) {
		for (Argument arg : arguments) {
			if (kind.equals(arg.kind) && name.equals(arg.name))
				return arg;
		}
		return null;
	}
	
	/**
	 * Parse the list of scripts described by the provided Pipeline2WSResponse.
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static List<Script> getScripts(Pipeline2WSResponse response) throws Pipeline2WSException {
		if (response.status != 200)
			throw new Pipeline2WSException(response.status+" "+response.statusName+": "+response.statusDescription);
		return parseScriptsXml(response.asXml());
	}
	
	/**
	 * Parse the list of scripts described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/scripts.xml
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static List<Script> parseScriptsXml(Node scriptsXml) throws Pipeline2WSException {
		
		List<Script> scripts = new ArrayList<Script>();
		
		// select root element if the node is a document node
		if (scriptsXml instanceof Document)
			scriptsXml = XPath.selectNode("/d:scripts", scriptsXml, Pipeline2WS.ns);
		
		List<Node> scriptNodes = XPath.selectNodes("d:script", scriptsXml, Pipeline2WS.ns);
		for (Node scriptNode : scriptNodes) {
			scripts.add(new Script(scriptNode));
		}
		
		return scripts;
	}
	
}
