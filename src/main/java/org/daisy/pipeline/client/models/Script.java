package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.client.models.script.Author;
import org.daisy.pipeline.client.models.script.Homepage;
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
	
	/** List of mime types that are supported by more than one file argument.
	 * This is not really a part of the Web API, but are more of a convenience.
	 * A user interface cannot automatically assign files of these media types to a file argument. */
	public List<String> mediaTypeBlacklist;
	
	// ---------- Constructors ----------
	
	public Script() {
		this.arguments = new ArrayList<Argument>();
		this.mediaTypeBlacklist = new ArrayList<String>();
	}
	
	/**
	 * Parse the script described by the provided Pipeline2WSResponse.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/script.xml
	 * 
	 * @param response
	 * @throws Pipeline2WSException
	 */
	public Script(Pipeline2WSResponse response) throws Pipeline2WSException {
		this(response.asXml());
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
		
		// select root element if the node is a document node
		if (scriptXml instanceof Document)
			scriptXml = XPath.selectNode("/d:job", scriptXml, Pipeline2WS.ns);
		
		this.id = XPath.selectText("@id", scriptXml, Pipeline2WS.ns);
		this.href = XPath.selectText("@href", scriptXml, Pipeline2WS.ns);
		this.nicename = XPath.selectText("d:nicename", scriptXml, Pipeline2WS.ns);
		this.desc = XPath.selectText("d:description", scriptXml, Pipeline2WS.ns);
		this.homepage = new Homepage(XPath.selectText("d:homepage", scriptXml, Pipeline2WS.ns), "");
		
		List<Node> inputNodes = XPath.selectNodes("d:input", scriptXml, Pipeline2WS.ns);
		List<Node> optionNodes = XPath.selectNodes("d:option", scriptXml, Pipeline2WS.ns);
		List<Node> outputNodes = XPath.selectNodes("d:output", scriptXml, Pipeline2WS.ns);
		
		for (Node inputNode : inputNodes) {
			Argument arg = new Argument();
			
			arg.name = parseTypeString(XPath.selectText("@name", inputNode, Pipeline2WS.ns));
			arg.nicename = parseTypeString(XPath.selectText("@nicename", inputNode, Pipeline2WS.ns));
			if (arg.nicename == null || "".equals(arg.nicename))
				arg.nicename = arg.name;
			arg.desc = parseTypeString(XPath.selectText("@desc", inputNode, Pipeline2WS.ns));
			arg.required = parseTypeBoolean(XPath.selectText("@required", inputNode, Pipeline2WS.ns), true);
			arg.sequence = parseTypeBoolean(XPath.selectText("@sequence", inputNode, Pipeline2WS.ns), false);
			arg.ordered = parseTypeBoolean(XPath.selectText("@ordered", inputNode, Pipeline2WS.ns), true);
			arg.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", inputNode, Pipeline2WS.ns));
			if (arg.mediaTypes.size() == 0)
				arg.mediaTypes.add("application/xml");
			arg.xsdType = "anyFileURI";
			arg.output = null; // irrelevant, but let's give it a value
			arg.kind = "input"; // TODO "parameters": is there a @kind attribute in the Web API?
			
			this.arguments.add(arg);
		}

		for (Node optionNode : optionNodes) {
			Argument arg = new Argument();
			
			arg.name = parseTypeString(XPath.selectText("@name", optionNode, Pipeline2WS.ns));
			arg.nicename = parseTypeString(XPath.selectText("@nicename", optionNode, Pipeline2WS.ns));
			if (arg.nicename == null || "".equals(arg.nicename))
				arg.nicename = arg.name;
			arg.desc = parseTypeString(XPath.selectText("@desc", optionNode, Pipeline2WS.ns));
			arg.required = parseTypeBoolean(XPath.selectText("@required", optionNode, Pipeline2WS.ns), true);
			arg.sequence = parseTypeBoolean(XPath.selectText("@sequence", optionNode, Pipeline2WS.ns), false);
			arg.ordered = parseTypeBoolean(XPath.selectText("@ordered", optionNode, Pipeline2WS.ns), true);
			arg.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", optionNode, Pipeline2WS.ns));
			arg.xsdType = parseTypeString(XPath.selectText("@type", optionNode, Pipeline2WS.ns));
			arg.output = parseTypeString(XPath.selectText("@outputType", optionNode, Pipeline2WS.ns));
			arg.kind = "option";
			
			this.arguments.add(arg);
		}

		for (Node outputNode : outputNodes) {
			Argument arg = new Argument();
			
			arg.name = parseTypeString(XPath.selectText("@name", outputNode, Pipeline2WS.ns));
			arg.nicename = parseTypeString(XPath.selectText("@nicename", outputNode, Pipeline2WS.ns));
			if (arg.nicename == null || "".equals(arg.nicename))
				arg.nicename = arg.name;
			arg.desc = parseTypeString(XPath.selectText("@desc", outputNode, Pipeline2WS.ns));
			arg.required = parseTypeBoolean(XPath.selectText("@required", outputNode, Pipeline2WS.ns), true);
			arg.sequence = parseTypeBoolean(XPath.selectText("@sequence", outputNode, Pipeline2WS.ns), false);
			arg.ordered = parseTypeBoolean(XPath.selectText("@ordered", outputNode, Pipeline2WS.ns), true);
			arg.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", outputNode, Pipeline2WS.ns));
			if (arg.mediaTypes.size() == 0)
				arg.mediaTypes.add("application/xml");
			arg.xsdType = "anyFileURI";
			arg.output = parseTypeString(XPath.selectText("@outputType", outputNode, Pipeline2WS.ns));
			arg.kind = "output";
			
			this.arguments.add(arg);
		}
		
		Map<String,Integer> mediaTypeOccurences = new HashMap<String,Integer>();
		for (Argument arg : this.arguments) {
			for (String mediaType : arg.mediaTypes) {
				if (mediaTypeOccurences.containsKey(mediaType)) {
					mediaTypeOccurences.put(mediaType, mediaTypeOccurences.get(mediaType)+1);
				} else {
					mediaTypeOccurences.put(mediaType, 1);
				}
			}
		}
		for (String mediaType : mediaTypeOccurences.keySet()) {
			if (mediaTypeOccurences.get(mediaType) > 1)
				this.mediaTypeBlacklist.add(mediaType);
		}
	}
	
	/**
	 * Parse the list of scripts described by the provided Pipeline2WSResponse.
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static List<Script> getScripts(Pipeline2WSResponse response) throws Pipeline2WSException {
		return getScripts(response.asXml());
	}
	
	/**
	 * Parse the list of scripts described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/scripts.xml
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static List<Script> getScripts(Node scriptsXml) throws Pipeline2WSException {
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
	
	/** Helper function for the Script(Document) constructor */
	private static String parseTypeString(String string) {
		if (!(string instanceof String))
			return null;
		return string.replaceAll("\"", "'").replaceAll("\\n", " ");
	}
	
	/** Helper function for the Script(Document) constructor */
	private static boolean parseTypeBoolean(String bool, boolean def) {
		if (!(bool instanceof String))
			return def;
		if ("false".equals(bool))
			return false;
		if ("true".equals(bool))
			return true;
		return def;
	}
	
	/** Helper function for the Script(Document) constructor */
	private static List<String> parseTypeMediaTypes(String mediaTypesString) {
		if (!(mediaTypesString instanceof String))
			return new ArrayList<String>();
		mediaTypesString = parseTypeString(mediaTypesString);
		String[] mediaTypes = (mediaTypesString==null?"":mediaTypesString).split(" ");
		List<String> mediaTypesList = new ArrayList<String>();
		for (String mediaType : mediaTypes) {
			if ("".equals(mediaType))
				continue;
			
			if ("text/xml".equals(mediaType))
				mediaTypesList.add("application/xml");
			else
				mediaTypesList.add(mediaType);
		}
		return mediaTypesList;
	}
	
}
