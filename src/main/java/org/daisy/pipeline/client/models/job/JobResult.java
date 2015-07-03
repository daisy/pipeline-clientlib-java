package org.daisy.pipeline.client.models.job;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Node;

/**
 * @author jostein
 */
public class JobResult implements Comparable<JobResult> {

	public String href;
	public String file;
	public String mimeType;
	public String name;
	public String from;
	public Long size;

	public List<JobResult> results = new ArrayList<JobResult>();

	// convenience variables
	public String filename;
	public String relativeHref;

	private static Pattern filenamePattern = Pattern.compile(".*/");

	public static JobResult parseResultXml(Node resultNode) throws Pipeline2WSException {
		return parseResultXml(resultNode, null);
	}

	public static JobResult parseResultXml(Node resultNode, String base) throws Pipeline2WSException {
		JobResult item = new JobResult();

		item.href = XPath.selectText("@href", resultNode, Pipeline2WS.ns);
		item.file= XPath.selectText("@file", resultNode, Pipeline2WS.ns);
		item.mimeType = XPath.selectText("@mime-type", resultNode, Pipeline2WS.ns);
		item.name = XPath.selectText("@name", resultNode, Pipeline2WS.ns);
		item.from = XPath.selectText("@from", resultNode, Pipeline2WS.ns);

		item.filename = item.href == null ? null : filenamePattern.matcher(item.href).replaceAll("");

		if (base != null) {
			if (base.length() >= item.href.length()) {
				item.relativeHref = "";
			} else {
				item.relativeHref = item.href == null ? null : item.href.substring(base.length() + 1);
			}
		}

		String sizeText = XPath.selectText("@size", resultNode, Pipeline2WS.ns);
		if (sizeText != null && sizeText.length() > 0) {
			item.size = Long.parseLong(sizeText);
		} else {
			item.size = 0L;
			List<Node> leafNodes = XPath.selectNodes(".//d:result[not(*)]", resultNode, Pipeline2WS.ns);
			for (Node leafNode : leafNodes) {
				String childSize = XPath.selectText("@size", leafNode, Pipeline2WS.ns);
				item.size += childSize == null ? 0 : Long.parseLong(childSize);
			}
		}

		return item;
	}

	public int compareTo(JobResult other) {
		return href.compareTo(other.href);
	}

}
