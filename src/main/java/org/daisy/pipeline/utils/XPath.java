package org.daisy.pipeline.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.daisy.pipeline.client.Pipeline2WSException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for querying XML documents using XPath.
 * 
 * @author jostein
 */
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
	
	/**
	 * Select all nodes matching `expr` and return them as a List.
	 * 
	 * @param expr XPath expression
	 * @param doc XML node
	 * @param ns Namespace map (Map<String prefix, String namespace>)
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static List<Node> selectNodes(String expr, Node doc, Map<String, String> ns) throws Pipeline2WSException {
		if (doc == null || expr == null)
			return new ArrayList<Node>();
		
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
	
	/**
	 * Select the node matching `expr` and return its text content.
	 * 
	 * @param expr XPath expression
	 * @param doc XML node
	 * @param ns Namespace map (Map<String prefix, String namespace>)
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static String selectText(String expr, Node doc, Map<String, String> ns) throws Pipeline2WSException {
		if (doc == null || expr == null)
			return null;
		
		try {
			String nodeText = (String) xpath(expr,ns).evaluate(doc, XPathConstants.STRING);
			return nodeText;
//			if (nodeList.getLength() == 0) return null;
//			else return nodeList.item(0);
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new Pipeline2WSException(e);
		}
		
//		Node node = selectNode(expr, doc, ns);
//		if (node == null) return null;
//		return node.getTextContent();
	}
	
	/**
	 * Select the nodes matching `expr` and return it.
	 * 
	 * @param expr XPath expression
	 * @param doc XML node
	 * @param ns Namespace map (Map<String prefix, String namespace>)
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static Node selectNode(String expr, Node doc, Map<String, String> ns) throws Pipeline2WSException {
		if (doc == null || expr == null)
			return null;
		
		try {
			NodeList nodeList = (NodeList) xpath(expr,ns).evaluate(doc, XPathConstants.NODESET);
			if (nodeList.getLength() == 0) return null;
			else return nodeList.item(0);
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new Pipeline2WSException(e);
		}
	}
	
}
