package pipeline2.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import pipeline2.Pipeline2WS;
import pipeline2.models.script.Argument;
import pipeline2.models.script.Author;
import pipeline2.models.script.Homepage;
import play.libs.XPath;

/** Information about the current script. */
public class Script {
	
	public String id;
	public String href;
	public String nicename;
	public String desc;
	public Homepage homepage;
	public Author author;
	public List<Argument> arguments;
	
	// ---------- Constructors ----------
	
	public Script() {
		this.arguments = new ArrayList<Argument>();
	}
	
	/** Parse a Script XML document retrieved from the Pipeline 2 Web Service and create Helper function for the Script(Document) constructor */
	public Script(Document scriptXml) {
		this();
		
		this.id = XPath.selectText("/d:script/@id", scriptXml, Pipeline2WS.ns);
		this.href = XPath.selectText("/d:script/@href", scriptXml, Pipeline2WS.ns);
		this.nicename = XPath.selectText("/d:script/d:nicename", scriptXml, Pipeline2WS.ns);
		this.desc = XPath.selectText("/d:script/d:description", scriptXml, Pipeline2WS.ns);
		this.homepage = new Homepage(XPath.selectText("/d:script/d:homepage", scriptXml, Pipeline2WS.ns), "");
		
		List<Node> inputNodes = XPath.selectNodes("/d:script/d:input", scriptXml, Pipeline2WS.ns);
		List<Node> optionNodes = XPath.selectNodes("/d:script/d:option", scriptXml, Pipeline2WS.ns);
		List<Node> outputNodes = XPath.selectNodes("/d:script/d:output", scriptXml, Pipeline2WS.ns);
		
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
			arg.xsdType = "anyFileURI";
			arg.output = ""; // irrelevant, but let's give it a value
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
			arg.output = parseTypeString(XPath.selectText("@output", optionNode, Pipeline2WS.ns));
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
			arg.xsdType = "anyFileURI";
			arg.output = parseTypeString(XPath.selectText("@output", outputNode, Pipeline2WS.ns));
			arg.kind = "output";
			
			this.arguments.add(arg);
		}
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
		String[] mediaTypes = mediaTypesString.split(" ");
		for (int i = 0; i < mediaTypes.length; i++)
			mediaTypes[i] = parseTypeString(mediaTypes[i]);
		return Arrays.asList(mediaTypes);
	}
	
}
