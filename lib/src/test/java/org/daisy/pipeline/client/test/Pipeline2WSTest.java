package org.daisy.pipeline.client.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.daisy.pipeline.client.Alive;
import org.daisy.pipeline.client.Jobs;
import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSLogger;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.Scripts;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Job.Status;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.utils.NamespaceContextMap;
import org.daisy.pipeline.utils.XML;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Pipeline2WSTest {

	@Test
	public void getScripts() {
		try {
			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
			Pipeline2WSResponse response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret");
			if (response.status != 200)
				fail(response.status+": "+response.statusName+" ("+response.statusDescription+")");
			
			List<Script> scripts = Script.getScripts(response);
			if (scripts.size() == 0)
				fail("no scripts in response");
			if (scripts.get(0).id == null || scripts.get(0).id.length() == 0)
				fail("empty script id");
			if (scripts.get(0).nicename == null || scripts.get(0).nicename.length() == 0)
				fail("empty nicename id");
			if (scripts.get(0).desc == null || scripts.get(0).desc.length() == 0)
				fail("empty script description");
			
			String[] orderedScripts = new String[]{"daisy202-to-epub3", "dtbook-to-epub3", "dtbook-to-zedai", "zedai-to-epub3", "zedai-to-pef"};
			for (int s = 0; s < orderedScripts.length; s++) {
				if (!orderedScripts[s].equals(scripts.get(s).id))
					fail("scripts list must be ordered alphabetically by id ("+scripts.get(s).id+" at position "+s+")");
			}
			
			assertNotNull(scripts.get(0).id);

		} catch (Pipeline2WSException e) {
			fail(e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private boolean containsArgument(List<Argument> arguments, String argName) {
		for (Argument arg : arguments) {
			if (argName.equals(arg.name))
				return true;
		}
		return false;
	}
	
	@Test
	public void testParseAliveResponse() {
		try {
			Pipeline2WS.logger().setLevel(Pipeline2WSLogger.LEVEL.ALL);
			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
			Pipeline2WSResponse response = Alive.get("http://localhost:8182/ws");
			org.daisy.pipeline.client.models.Alive alive = new org.daisy.pipeline.client.models.Alive(response);
			
			assertNotNull(alive);
			assertEquals(false, alive.authentication);
			assertEquals(true, alive.localfs);
			assertEquals(false, alive.error);
			assertEquals("1.6", alive.version);
			
			assertEquals(true, Alive.allowsAccessToLocalFilesystem("http://localhost:8182/ws"));;
			assertEquals(false, Alive.usesAuthentication("http://localhost:8182/ws"));

		} catch (Pipeline2WSException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseJobResponse() {
		try {
			Pipeline2WS.logger().setLevel(Pipeline2WSLogger.LEVEL.ALL);
			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
			Pipeline2WSResponse response = Jobs.get("http://localhost:8182/ws", "clientid", "supersecret", "job1", null);
			Job job = new Job(response);
			
			assertNotNull(job);
			assertEquals("job1", job.id);
			assertEquals("http://localhost:8181/ws/jobs/job1", job.href);
			assertEquals(Status.DONE, job.status);
			assertNotNull(job.script);
			assertEquals("http://localhost:8181/ws/scripts/dtbook-to-zedai", job.script.href);
			assertEquals("dtbook-to-zedai", job.script.id);
			assertEquals("DTBook to ZedAI", job.script.nicename);
			assertEquals("Transforms DTBook XML into ZedAI XML.", job.script.desc);
			assertNotNull(job.messages);
			assertEquals(62, job.messages.size());
			assertEquals("http://localhost:8181/ws/jobs/job1/log", job.logHref);
			
			assertEquals("", job.results.from);
			assertEquals("", job.results.file);
			assertEquals("application/zip", job.results.mimeType);
			assertEquals(new Long(178073), job.results.size);
			assertEquals("", job.results.name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result", job.results.href);
			assertEquals("result", job.results.relativeHref);
			assertEquals("result", job.results.filename);
			assertEquals(1, job.results.results.size());

			assertEquals("option", job.results.results.get(0).from);
			assertEquals("", job.results.results.get(0).file);
			assertEquals("application/zip", job.results.results.get(0).mimeType);
			assertEquals(new Long(178073), job.results.results.get(0).size);
			assertEquals("output-dir", job.results.results.get(0).name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir", job.results.results.get(0).href);
			assertEquals("result/option/output-dir", job.results.results.get(0).relativeHref);
			assertEquals("option/output-dir", job.results.results.get(0).filename);
			assertEquals(3, job.results.results.get(0).results.size());
			
			assertEquals("", job.results.results.get(0).results.get(2).from);
			assertEquals("file:/home/jostein/pipeline-assembly/target/dev-launcher/data/data/job1/output/output-dir/valentin.jpg", job.results.results.get(0).results.get(0).file);
			assertEquals("", job.results.results.get(0).results.get(0).mimeType);
			assertEquals(new Long(25740), job.results.results.get(0).results.get(0).size);
			assertEquals("", job.results.results.get(0).results.get(0).name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/valentin.jpg", job.results.results.get(0).results.get(0).href);
			assertEquals("result/option/output-dir/valentin.jpg", job.results.results.get(0).results.get(0).relativeHref);
			assertEquals("valentin.jpg", job.results.results.get(0).results.get(0).filename);
			
			assertEquals("", job.results.results.get(0).results.get(1).from);
			assertEquals("file:/home/jostein/pipeline-assembly/target/dev-launcher/data/data/job1/output/output-dir/zedai-mods.xml", job.results.results.get(0).results.get(1).file);
			assertEquals("", job.results.results.get(0).results.get(1).mimeType);
			assertEquals(new Long(442), job.results.results.get(0).results.get(1).size);
			assertEquals("", job.results.results.get(0).results.get(1).name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/zedai-mods.xml", job.results.results.get(0).results.get(1).href);
			assertEquals("result/option/output-dir/zedai-mods.xml", job.results.results.get(0).results.get(1).relativeHref);
			assertEquals("zedai-mods.xml", job.results.results.get(0).results.get(1).filename);
			
			assertEquals("", job.results.results.get(0).results.get(2).from);
			assertEquals("file:/home/jostein/pipeline-assembly/target/dev-launcher/data/data/job1/output/output-dir/zedai.xml", job.results.results.get(0).results.get(2).file);
			assertEquals("", job.results.results.get(0).results.get(2).mimeType);
			assertEquals(new Long(151891), job.results.results.get(0).results.get(2).size);
			assertEquals("", job.results.results.get(0).results.get(2).name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/zedai.xml", job.results.results.get(0).results.get(2).href);
			assertEquals("result/option/output-dir/zedai.xml", job.results.results.get(0).results.get(2).relativeHref);
			assertEquals("zedai.xml", job.results.results.get(0).results.get(2).filename);
			
			response = Jobs.get("http://localhost:8182/ws", "clientid", "supersecret", "job2", null);
			job = new Job(response);
			
			assertNotNull(job.results);
			assertEquals("result", job.results.filename);
			assertEquals("result", job.results.relativeHref);
			assertEquals("option/output-dir", job.results.results.get(0).filename);
			assertEquals("result/option/output-dir", job.results.results.get(0).relativeHref);
			assertEquals("idx/output-dir/epub/mimetype", job.results.results.get(0).results.get(25).filename);
			assertEquals("result/option/output-dir/idx/output-dir/epub/mimetype", job.results.results.get(0).results.get(25).relativeHref);
			assertEquals("idx/output-dir/epub/EPUB/Content/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", job.results.results.get(0).results.get(19).filename);
			assertEquals("result/option/output-dir/idx/output-dir/epub/EPUB/Content/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", job.results.results.get(0).results.get(19).relativeHref);

		} catch (Pipeline2WSException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseJobRequest() {
		try {
			Pipeline2WS.logger().setLevel(Pipeline2WSLogger.LEVEL.ALL);
			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
			Pipeline2WSResponse response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret", "dtbook-to-zedai");
			System.out.println("response: "+response.asText());
			Script script = new Script(response);
			
			for (Argument arg : script.arguments) {
				assertNotNull(arg.name);
				assertNotNull(arg.name, arg.nicename);
				assertNotNull(arg.name, arg.desc);
				assertNotNull(arg.name, arg.required);
				assertNotNull(arg.name, arg.sequence);
				assertNotNull(arg.name, arg.ordered);
				assertNotNull(arg.name, arg.mediaTypes);
				assertNotNull(arg.name, arg.xsdType);
				assertNotNull(arg.name, arg.kind);
				
				assertEquals(arg.name+" has a decription", true, arg.desc.length() > 0);
				assertEquals(arg.name+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));
				
				if ("source".equals(arg.name)) {
					assertEquals("input", arg.kind);
					assertEquals("application/x-dtbook+xml", arg.mediaTypes.get(0));
					assertEquals(true, arg.ordered);
					assertEquals(true, arg.required);
					assertEquals(true, arg.sequence);
					
					arg.add("file1.xml");
					arg.add("file2.xml");
				}
				else if ("zedai-filename".equals(arg.name)) {
					assertEquals("option", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
					assertEquals("string", arg.xsdType);
					
					arg.set("zedai.xml");
				}
				else if ("assert-valid".equals(arg.name)) {
					assertEquals("option", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
					assertEquals("boolean", arg.xsdType);
					
					arg.set(true);
				}
				else if ("output-dir".equals(arg.name)) {
					assertEquals("option", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(true, arg.required);
					assertEquals(false, arg.sequence);
					assertEquals("anyDirURI", arg.xsdType);
					assertEquals("result", arg.output);
					
					arg.set("file:/tmp/text/");
				}
				else if ("mods-filename".equals(arg.name)) {
					assertEquals("option", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
					assertEquals("string", arg.xsdType);
					
					arg.set("mods.xml");
				}
				else if ("lang".equals(arg.name)) {
					assertEquals("option", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
					assertEquals("string", arg.xsdType);
					
					arg.set("en");
				}
				else if ("css-filename".equals(arg.name)) {
					assertEquals("option", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
					assertEquals("string", arg.xsdType);
					
					arg.set("main.css");
				}
				else fail("Unknown argument: "+arg.name);
			}
			Document jobRequest = Jobs.createJobRequestDocument(script.href, script.arguments, null);
			
			script = new Script(response);
			script.parseFromJobRequest(jobRequest);
			for (Argument arg : script.arguments) {
				if ("source".equals(arg.name)) {
					assertEquals(true, "file1.xml".equals(arg.get(0)));
					assertEquals(true, "file2.xml".equals(arg.get(1)));
				}
				else if ("zedai-filename".equals(arg.name)) assertEquals("zedai.xml", arg.get());
				else if ("assert-valid".equals(arg.name)) assertEquals("true", arg.get());
				else if ("output-dir".equals(arg.name)) assertEquals("file:/tmp/text/", arg.get());
				else if ("mods-filename".equals(arg.name)) assertEquals("mods.xml", arg.get());
				else if ("lang".equals(arg.name)) assertEquals("en", arg.get());
				else if ("css-filename".equals(arg.name)) assertEquals("main.css", arg.get());
			}

		} catch (Pipeline2WSException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testOutputPorts() {
		try {
			Pipeline2WS.logger().setLevel(Pipeline2WSLogger.LEVEL.ALL);
			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
			Pipeline2WSResponse response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret", "dtbook-validator");
			System.out.println("response: "+response.asText());
			Script script = new Script(response);
			
			for (Argument arg : script.arguments) {
				assertNotNull(arg.name);
				assertNotNull(arg.name, arg.nicename);
				assertNotNull(arg.name, arg.desc);
				assertNotNull(arg.name, arg.required);
				assertNotNull(arg.name, arg.sequence);
				assertNotNull(arg.name, arg.ordered);
				assertNotNull(arg.name, arg.mediaTypes);
				assertNotNull(arg.name, arg.xsdType);
				assertNotNull(arg.name, arg.kind);
				
				assertEquals(arg.name+" has a decription", true, arg.desc.length() > 0);
				assertEquals(arg.name+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));
				
				if ("result".equals(arg.name)) {
					assertEquals("output", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
				}
				else if ("schematron-report".equals(arg.name)) {
					assertEquals("output", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
				}
				else if ("relaxng-report".equals(arg.name)) {
					assertEquals("output", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(true, arg.sequence);
				}
				else if ("html-report".equals(arg.name)) {
					assertEquals("output", arg.kind);
					assertEquals(true, arg.ordered);
					assertEquals(false, arg.required);
					assertEquals(false, arg.sequence);
				}
			}

		} catch (Pipeline2WSException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetResults() {
		try {
			Pipeline2WS.logger().setLevel(Pipeline2WSLogger.LEVEL.ALL);
			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
			
			Pipeline2WSResponse response = Jobs.getResult("http://localhost:8182/ws", "clientid", "supersecret", "job1", "F00000 - Don't Worry, Be Happy Lyrics.epub");
			assertEquals("TEST", response.asText());
			
		} catch (Pipeline2WSException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testXPath() {
		XPathFactory factory = XPathFactory.newInstance();
		javax.xml.xpath.XPath xpath = factory.newXPath();
		NamespaceContext context = new NamespaceContextMap(
				"foo", "http://foo", 
				"bar", "http://bar");

		xpath.setNamespaceContext(context);


		Document xml = XML.getXml("<foo:data xmlns:foo='http://foo' xmlns:bar='http://bar'><bar:foo bar=\"hello\" /></foo:data>");
		try {
			XPathExpression compiled = xpath.compile("/foo:data/bar:foo/attribute::bar");
			NodeList nodeList = (NodeList)compiled.evaluate(xml, XPathConstants.NODESET);
			assertEquals("bar=\"hello\"", nodeList.item(0).toString());
		} catch (XPathExpressionException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testXPathContext() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("foo", "http://foo");
		mappings.put("altfoo", "http://foo");
		mappings.put("bar", "http://bar");
		mappings.put(XMLConstants.XML_NS_PREFIX,XMLConstants.XML_NS_URI);

		NamespaceContext context = new NamespaceContextMap(mappings);
		for (Map.Entry<String, String> entry : mappings.entrySet()) {
			String prefix = entry.getKey();
			String namespaceURI = entry.getValue();

			Assert.assertEquals("namespaceURI", namespaceURI, context.getNamespaceURI(prefix));
			boolean found = false;
			Iterator<?> prefixes = context.getPrefixes(namespaceURI);
			while (prefixes.hasNext()) {
				if (prefix.equals(prefixes.next())) {
					found = true;
					break;
				}
				try {
					prefixes.remove();
					Assert.fail("rw");
				} catch (UnsupportedOperationException e) {
				}
			}
			Assert.assertTrue("prefix: " + prefix, found);
			Assert.assertNotNull("prefix: " + prefix, context.getPrefix(namespaceURI));
		}

		Map<String, String> ctxtMap = ((NamespaceContextMap) context).getMap();
		for (Map.Entry<String, String> entry : mappings.entrySet()) {
			Assert.assertEquals(entry.getValue(), ctxtMap.get(entry.getKey()));
		}
	}

	@Test
	public void testXPathModify() {
		NamespaceContextMap context = new NamespaceContextMap();

		try {
			Map<String, String> ctxtMap = context.getMap();
			ctxtMap.put("a", "b");
			Assert.fail("rw");
		} catch (UnsupportedOperationException e) {
		}

		try {
			Iterator<String> it = context.getPrefixes(XMLConstants.XML_NS_URI);
			it.next();
			it.remove();
			Assert.fail("rw");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testXPathConstants() {
		NamespaceContext context = new NamespaceContextMap();
		Assert.assertEquals(XMLConstants.XML_NS_URI, context.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
		Assert.assertEquals(XMLConstants.XML_NS_PREFIX, context.getPrefix(XMLConstants.XML_NS_URI));
		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
	}
	
}
