package pipeline2.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pipeline2.Pipeline2WSException;

public class XPath {
	
	private static Map<String,XPathExpression> expressions = new HashMap<String,XPathExpression>();
	public Map<String,String> namespaces = new HashMap<String,String>();
	private static NamespaceContextMap nsContext = new NamespaceContextMap();
	
	private XPath() {}
	
	private static XPathExpression xpath(String expression, Map<String, String> ns) throws XPathExpressionException {
		updateNs(ns);
		if (expressions.containsKey(expression)) {
			return expressions.get(expression);
		} else {
			javax.xml.xpath.XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(nsContext);
			XPathExpression compiledExpression = xpath.compile(expression);
			expressions.put(expression, compiledExpression);
			return compiledExpression;
		}
	}
	
	private static void updateNs(Map<String, String> ns) {
		boolean equals = ns.size() == nsContext.getMap().size()-2;
		if (equals) {
			for (String prefix : ns.keySet()) {
				if (!nsContext.getMap().containsKey(prefix) || !nsContext.getMap().get(prefix).equals(ns.get(prefix))) {
					equals = false;
					break;
				}
			}
		}
		if (!equals) {
			nsContext = new NamespaceContextMap(ns);
			expressions.clear();
		}
	}
	
	public static List<Node> selectNodes(String expr, Node doc, Map<String, String> ns) throws Pipeline2WSException {
		try {
			NodeList nodeList = (NodeList) xpath(expr,ns).evaluate(doc, XPathConstants.NODESET);
			List<Node> result = new ArrayList<Node>();
		    for (int i = 0; i < nodeList.getLength(); i++) {
		    	result.add(nodeList.item(i));
		    }
		    return result;
			
		} catch (XPathExpressionException e) {
			throw new Pipeline2WSException(e);
		}
	}

	public static String selectText(String expr, Node doc, Map<String, String> ns) throws Pipeline2WSException {
		Node node = selectNode(expr, doc, ns);
		if (node == null) return null;
		return node.getNodeValue();
	}

	public static Node selectNode(String expr, Node doc, Map<String, String> ns) throws Pipeline2WSException {
		try {
			NodeList nodeList = (NodeList) xpath(expr,ns).evaluate(doc, XPathConstants.NODESET);
			if (nodeList.getLength() == 0) return null;
			else return nodeList.item(0);
			
		} catch (XPathExpressionException e) {
			throw new Pipeline2WSException(e);
		}
	}
	
}
