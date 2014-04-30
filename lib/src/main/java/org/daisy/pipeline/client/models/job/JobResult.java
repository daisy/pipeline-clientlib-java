package org.daisy.pipeline.client.models.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Node;

/**
 * 
 * 
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
	
	/**
	 * Get the job result matching the given href (either this JobResult or any of its descendants).
	 * Please use Job#getResultByHref instead to search all results in the job.
	 */
	public JobResult getResultByHref(String href, String base) {
		String fullHref = base+"/"+href;
		
		if (fullHref.equals(this.href)) {
			Pipeline2WS.logger().debug("getResultByHref: returning d:result element");
			return this;
		}
		
		if (results == null) {
			Pipeline2WS.logger().debug("getResultByHref: returning null (no descendant d:result element)");
			return null;
		}
		
		for (JobResult result : results) {
			JobResult r = result.getResultByHref(href, base);
			if (r != null) {
				Pipeline2WS.logger().debug("getResultByHref: found d:result !");
				return r;
			} else {
				Pipeline2WS.logger().debug("getResultByHref: wrong d:result: "+result.href);
			}
		}
		
		return null;
	}
	
	private static Pattern filenamePattern = Pattern.compile(".*/");

    public static JobResult parseResultXml(Node resultNode) throws Pipeline2WSException {
    	return parseResultXml(resultNode, null, null);
    }
    
	private static JobResult parseResultXml(Node resultNode, String relativePath, String parentHref) throws Pipeline2WSException {
		JobResult item = new JobResult();
		
		item.href = XPath.selectText("@href", resultNode, Pipeline2WS.ns);
		item.file= XPath.selectText("@file", resultNode, Pipeline2WS.ns);
		item.mimeType = XPath.selectText("@mime-type", resultNode, Pipeline2WS.ns);
		item.name = XPath.selectText("@name", resultNode, Pipeline2WS.ns);
		item.from = XPath.selectText("@from", resultNode, Pipeline2WS.ns);
		
		if (parentHref == null) {
			item.filename = item.href == null ? null : filenamePattern.matcher(item.href).replaceAll("");
		} else {
			item.filename = item.href.substring(parentHref.length()+1);
		}
		
		if (XPath.selectNode("self::d:result[not(string(@size)='') and not(@from)]", resultNode, Pipeline2WS.ns) != null) {
			item.filename = item.filename.replaceFirst("^idx/[^/]+/", "");
		}
		
		if (relativePath == null) {
			relativePath = item.href.substring(0, item.href.length()-item.filename.length());
		}
		item.relativeHref = item.href == null ? null : item.href.substring(relativePath.length());
		
		List<Node> childNodes = XPath.selectNodes("d:result", resultNode, Pipeline2WS.ns);
		for (Node childNode : childNodes) {
			item.results.add(JobResult.parseResultXml(childNode, relativePath, item.href));
		}
		Collections.sort(item.results);
		
		String sizeText = XPath.selectText("@size", resultNode, Pipeline2WS.ns);
		if (sizeText != null && sizeText.length() > 0) {
			item.size = Long.parseLong(sizeText);
		} else {
			item.size = 0L;
			for (JobResult childResult : item.results) {
				Long childSize = childResult.size;
				item.size += childSize == null ? 0 : childSize;
			}
		}
		
		return item;
	}

	public int compareTo(JobResult other) {
		return href.compareTo(other.href);
	}
	
}
