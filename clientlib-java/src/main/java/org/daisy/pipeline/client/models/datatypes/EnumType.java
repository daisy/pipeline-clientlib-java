package org.daisy.pipeline.client.models.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.models.DataType;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EnumType extends DataType {
	
	public List<Value> values = new ArrayList<Value>();
	
	public EnumType(Node dataTypeXml) {
		super(dataTypeXml);
		
		try {
			// select root element if the node is a document node
			if (dataTypeXml instanceof Document)
				dataTypeXml = XPath.selectNode("/*", dataTypeXml, XPath.dp2ns);
			
			Map<String,List<Node>> languages = new HashMap<String,List<Node>>();
			List<Node> translationNodes = XPath.selectNodes("*[local-name()='documentation']", dataTypeXml, XPath.dp2ns);
			for (Node translationNode : translationNodes) {
				String language = XPath.selectText("(@xml:lang | @lang | @*[local-name()='lang'])[1]", translationNode, XPath.dp2ns);
				List<Node> nicenames = XPath.selectNodes("*[local-name()='value']", translationNode, XPath.dp2ns);
				languages.put(language, nicenames);
			}
			
			List<Node> valueNodes = XPath.selectNodes("*[local-name()='value']", dataTypeXml, XPath.dp2ns);
			for (int i = 0; i < valueNodes.size(); i++) {
				Node valueNode = valueNodes.get(i);
				Value value = new Value();
				value.name = XPath.selectText("text()", valueNode, XPath.dp2ns);
				for (String language : languages.keySet()) {
					if (i < languages.get(language).size()) {
						String nicename = XPath.selectText("text()", languages.get(language).get(i), XPath.dp2ns);
						value.nicenames.put(language, nicename);
					}
				}
				values.add(value);
			}
			
		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse datatype (enum) XML", e);
		}
	}
	
	public class Value {
		public String name;
		public Map<String,String> nicenames = new HashMap<String,String>();
		
		/** Use to get the default nicename (if multiple are available) */
		public String getNicename() {
			if (nicenames.containsKey("")) {
				return nicenames.get("");
				
			} else if (nicenames.containsKey("en")) {
				return nicenames.get("en");
				
			} else if (!nicenames.isEmpty()) {
				for (String lang : nicenames.keySet()) {
					return nicenames.get(lang);
				}
			}
			
			return name;
		}
	}
	
}
