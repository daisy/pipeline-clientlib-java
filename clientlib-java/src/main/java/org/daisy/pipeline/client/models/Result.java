package org.daisy.pipeline.client.models;

import java.util.List;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Result implements Comparable<Result> {

	public String href;
	public String file;
	public String mimeType;
	public String name;
	public String from;
	public Long size;

	// convenience variables
	public String filename;
	public String relativeHref;

	private static Pattern filenamePattern = Pattern.compile(".*/");

	public static Result parseResultXml(Node resultNode) throws Pipeline2Exception {
		return parseResultXml(resultNode, null);
	}

	public static Result parseResultXml(Node resultNode, String base) throws Pipeline2Exception {
		Result item = new Result();

		item.href = XPath.selectText("@href", resultNode, XPath.dp2ns);
		item.file= XPath.selectText("@file", resultNode, XPath.dp2ns);
		item.mimeType = XPath.selectText("@mime-type", resultNode, XPath.dp2ns);
		item.name = XPath.selectText("@name", resultNode, XPath.dp2ns);
		item.from = XPath.selectText("@from", resultNode, XPath.dp2ns);

		item.filename = item.href == null ? null : filenamePattern.matcher(item.href).replaceAll("");

		if (base != null) {
			if (base.length() >= item.href.length()) {
				item.relativeHref = "";
			} else {
				item.relativeHref = item.href == null ? null : item.href.substring(base.length() + 1);
			}
		}

		String sizeText = XPath.selectText("@size", resultNode, XPath.dp2ns);
		if (sizeText != null && sizeText.length() > 0) {
			item.size = Long.parseLong(sizeText);
		} else {
			item.size = 0L;
			List<Node> leafNodes = XPath.selectNodes(".//d:result[not(*)]", resultNode, XPath.dp2ns);
			for (Node leafNode : leafNodes) {
				String childSize = XPath.selectText("@size", leafNode, XPath.dp2ns);
				item.size += childSize == null ? 0 : Long.parseLong(childSize);
			}
		}

		return item;
	}

	public int compareTo(Result other) {
		return href.compareTo(other.href);
	}

	public void toXml(Element resultElement) {
		if (href != null) {
		    resultElement.setAttribute("href", href);
		}
		if (file != null) {
		    resultElement.setAttribute("file", file);
		}
		if (mimeType != null) {
		    resultElement.setAttribute("mime-type", mimeType);
		}
		if (name != null) {
		    resultElement.setAttribute("name", name);
		}
		if (from != null) {
		    resultElement.setAttribute("from", from);
		}
		if (size != null) {
		    resultElement.setAttribute("size", size+"");
		}
	}

}
