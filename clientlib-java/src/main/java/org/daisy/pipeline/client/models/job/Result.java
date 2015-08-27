package org.daisy.pipeline.client.models.job;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Node;

public class Result implements Comparable<Result> {

	public String href;
	public String file;
	public String mimeType;
	public String name;
	public String from;
	public Long size;

	public List<Result> results = new ArrayList<Result>();

	// convenience variables
	public String filename;
	public String relativeHref;

	private static Pattern filenamePattern = Pattern.compile(".*/");

	public static Result parseResultXml(Node resultNode) throws Pipeline2Exception {
		return parseResultXml(resultNode, null);
	}

	public static Result parseResultXml(Node resultNode, String base) throws Pipeline2Exception {
		Result item = new Result();

		item.href = XPath.selectText("@href", resultNode, Pipeline2Client.ns);
		item.file= XPath.selectText("@file", resultNode, Pipeline2Client.ns);
		item.mimeType = XPath.selectText("@mime-type", resultNode, Pipeline2Client.ns);
		item.name = XPath.selectText("@name", resultNode, Pipeline2Client.ns);
		item.from = XPath.selectText("@from", resultNode, Pipeline2Client.ns);

		item.filename = item.href == null ? null : filenamePattern.matcher(item.href).replaceAll("");

		if (base != null) {
			if (base.length() >= item.href.length()) {
				item.relativeHref = "";
			} else {
				item.relativeHref = item.href == null ? null : item.href.substring(base.length() + 1);
			}
		}

		String sizeText = XPath.selectText("@size", resultNode, Pipeline2Client.ns);
		if (sizeText != null && sizeText.length() > 0) {
			item.size = Long.parseLong(sizeText);
		} else {
			item.size = 0L;
			List<Node> leafNodes = XPath.selectNodes(".//d:result[not(*)]", resultNode, Pipeline2Client.ns);
			for (Node leafNode : leafNodes) {
				String childSize = XPath.selectText("@size", leafNode, Pipeline2Client.ns);
				item.size += childSize == null ? 0 : Long.parseLong(childSize);
			}
		}

		return item;
	}

	public int compareTo(Result other) {
		return href.compareTo(other.href);
	}

}
