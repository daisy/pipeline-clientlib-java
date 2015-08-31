package org.daisy.pipeline.client.test;

//import java.io.File;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import javax.xml.XMLConstants;
//import javax.xml.namespace.NamespaceContext;
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpression;
//import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;
//
//import junit.framework.Assert;
//
//import org.daisy.pipeline.client.Pipeline2Exception;
//import org.daisy.pipeline.client.Pipeline2LoggerBak;
//import org.daisy.pipeline.client.http.Alive;
//import org.daisy.pipeline.client.http.Jobs;
//import org.daisy.pipeline.client.http.Client;
//import org.daisy.pipeline.client.http.WSResponse;
//import org.daisy.pipeline.client.http.Scripts;
//import org.daisy.pipeline.client.models.Argument;
//import org.daisy.pipeline.client.models.Job;
//import org.daisy.pipeline.client.models.Message;
//import org.daisy.pipeline.client.models.Result;
//import org.daisy.pipeline.client.models.Job.Status;
//import org.daisy.pipeline.client.models.Script;
//import org.daisy.pipeline.client.utils.NamespaceContextMap;
//import org.daisy.pipeline.client.utils.XML;
//import org.junit.Test;
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;

public class Pipeline2WSTest {
	
//	private static String resourceBaseUri = new File("src/test/resources/responses/").toURI().toString();
//
//	@Test
//	public void getScripts() {
//		try {
//			Client.setHttpClientImplementation(new MockHttpClient());
//			WSResponse response = Scripts.get("http://localhost:8181/ws", "clientid", "supersecret");
//			if (response.status != 200)
//				fail(response.status+": "+response.statusName+" ("+response.statusDescription+")");
//			
//			List<Script> scripts = Script.getScripts(response);
//			if (scripts.size() == 0)
//				fail("no scripts in response");
//			if (scripts.get(0).id == null || scripts.get(0).id.length() == 0)
//				fail("empty script id");
//			if (scripts.get(0).nicename == null || scripts.get(0).nicename.length() == 0)
//				fail("empty nicename id");
//			if (scripts.get(0).description == null || scripts.get(0).description.length() == 0)
//				fail("empty script description");
//			
//			String[] orderedScripts = new String[]{"daisy202-to-epub3", "dtbook-to-epub3", "dtbook-to-zedai", "zedai-to-epub3", "zedai-to-pef"};
//			for (int s = 0; s < orderedScripts.length; s++) {
//				if (!orderedScripts[s].equals(scripts.get(s).id))
//					fail("scripts list must be ordered alphabetically by id ("+scripts.get(s).id+" at position "+s+")");
//			}
//			
//			assertNotNull(scripts.get(0).id);
//
//		} catch (Pipeline2Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testParseAliveResponse() {
//		try {
//			Client.logger().setLevel(Pipeline2LoggerBak.LEVEL.ALL);
//			Client.setHttpClientImplementation(new MockHttpClient());
//			WSResponse response = Alive.get("http://localhost:8181/ws");
//			org.daisy.pipeline.client.models.Alive alive = new org.daisy.pipeline.client.models.Alive(response);
//			
//			assertNotNull(alive);
//			assertEquals(false, alive.authentication);
//			assertEquals(true, alive.localfs);
//			assertEquals(false, alive.error);
//			assertEquals("1.6", alive.version);
//			
//			assertEquals(true, Alive.allowsAccessToLocalFilesystem("http://localhost:8181/ws"));;
//			assertEquals(false, Alive.usesAuthentication("http://localhost:8181/ws"));
//
//		} catch (Pipeline2Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testParseJobResponse() {
//		try {
//			Client.logger().setLevel(Pipeline2LoggerBak.LEVEL.ALL);
//			Client.setHttpClientImplementation(new MockHttpClient());
//			WSResponse response = Jobs.get("http://localhost:8181/ws", "clientid", "supersecret", "job1", null);
//			Job job = new Job(response);
//			
//			assertNotNull(job);
//			assertEquals("job1", job.id);
//			assertEquals("http://localhost:8181/ws/jobs/job1", job.href);
//			assertEquals(Status.DONE, job.status);
//			assertNotNull(job.script);
//			assertEquals("http://localhost:8181/ws/scripts/dtbook-to-zedai", job.script.href);
//			assertEquals("dtbook-to-zedai", job.script.id);
//			assertEquals("DTBook to ZedAI", job.script.niceName);
//			assertEquals("Transforms DTBook XML into ZedAI XML.", job.script.description);
//			assertNotNull(job.messagesNode);
//			assertNotNull(job.resultsNode);
//			assertEquals("http://localhost:8181/ws/jobs/job1/log", job.log);
//			
//			List<Message> messages = job.getMessages();
//			Result results = job.getResults();
//			
//			assertEquals(62, messages.size());
//			
//			assertEquals("", results.from);
//			assertEquals("", results.file);
//			assertEquals("application/zip", results.mimeType);
//			assertEquals(new Long(178073), results.size);
//			assertEquals("", results.name);
//			assertEquals("http://localhost:8181/ws/jobs/job1/result", results.href);
//			assertEquals("result", results.relativeHref);
//			assertEquals("result", results.filename);
//			assertEquals(1, results.results.size());
//
//			assertEquals("option", results.results.get(0).from);
//			assertEquals("", results.results.get(0).file);
//			assertEquals("application/zip", results.results.get(0).mimeType);
//			assertEquals(new Long(178073), results.results.get(0).size);
//			assertEquals("output-dir", results.results.get(0).name);
//			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir", results.results.get(0).href);
//			assertEquals("result/option/output-dir", results.results.get(0).relativeHref);
//			assertEquals("output-dir", results.results.get(0).filename);
//			assertEquals(3, results.results.get(0).results.size());
//			
//			assertEquals("", results.results.get(0).results.get(0).from);
//			assertEquals(resourceBaseUri+"jobs/job1/result/option/output-dir/idx/output/valentin.jpg", results.results.get(0).results.get(0).file);
//			assertEquals("", results.results.get(0).results.get(0).mimeType);
//			assertEquals(new Long(25740), results.results.get(0).results.get(0).size);
//			assertEquals("", results.results.get(0).results.get(0).name);
//			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output/valentin.jpg", results.results.get(0).results.get(0).href);
//			assertEquals("result/option/output-dir/idx/output/valentin.jpg", results.results.get(0).results.get(0).relativeHref);
//			assertEquals("valentin.jpg", results.results.get(0).results.get(0).filename);
//			
//			assertEquals("", results.results.get(0).results.get(1).from);
//			assertEquals(resourceBaseUri+"jobs/job1/result/option/output-dir/idx/output/zedai-mods.xml", results.results.get(0).results.get(1).file);
//			assertEquals("", results.results.get(0).results.get(1).mimeType);
//			assertEquals(new Long(442), results.results.get(0).results.get(1).size);
//			assertEquals("", results.results.get(0).results.get(1).name);
//			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output/zedai-mods.xml", results.results.get(0).results.get(1).href);
//			assertEquals("result/option/output-dir/idx/output/zedai-mods.xml", results.results.get(0).results.get(1).relativeHref);
//			assertEquals("zedai-mods.xml", results.results.get(0).results.get(1).filename);
//			
//			assertEquals("", results.results.get(0).results.get(2).from);
//			assertEquals(resourceBaseUri+"jobs/job1/result/option/output-dir/idx/output/zedai.xml", results.results.get(0).results.get(2).file);
//			assertEquals("", results.results.get(0).results.get(2).mimeType);
//			assertEquals(new Long(151891), results.results.get(0).results.get(2).size);
//			assertEquals("", results.results.get(0).results.get(2).name);
//			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output/zedai.xml", results.results.get(0).results.get(2).href);
//			assertEquals("result/option/output-dir/idx/output/zedai.xml", results.results.get(0).results.get(2).relativeHref);
//			assertEquals("zedai.xml", results.results.get(0).results.get(2).filename);
//			
//			response = Jobs.get("http://localhost:8181/ws", "clientid", "supersecret", "job2", null);
//			job = new Job(response);
//			
//			messages = job.getMessages();
//			results = job.getResults();
//			
//			assertNotNull(results);
//			assertEquals("result", results.filename);
//			assertEquals("result", results.relativeHref);
//			assertEquals("output-dir", results.results.get(0).filename);
//			assertEquals("result/option/output-dir", results.results.get(0).relativeHref);
//			assertEquals("mimetype", results.results.get(0).results.get(25).filename);
//			assertEquals("result/option/output-dir/idx/output/output-dir/epub/mimetype", results.results.get(0).results.get(25).relativeHref);
//			assertEquals("OpenDyslexic-Regular.otf", results.results.get(0).results.get(19).filename);
//			assertEquals("result/option/output-dir/idx/output/output-dir/epub/EPUB/Content/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", results.results.get(0).results.get(19).relativeHref);
//
//		} catch (Pipeline2Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testParseJobRequest() {
//		try {
//			Client.logger().setLevel(Pipeline2LoggerBak.LEVEL.ALL);
//			Client.setHttpClientImplementation(new MockHttpClient());
//			WSResponse response = Scripts.get("http://localhost:8181/ws", "clientid", "supersecret", "dtbook-to-zedai");
//			System.out.println("response: "+response.asText());
//			Script script = new Script(response);
//			
//			for (Argument arg : script.arguments) {
//				assertNotNull(arg.name);
//				assertNotNull(arg.name, arg.niceName);
//				assertNotNull(arg.name, arg.desc);
//				assertNotNull(arg.name, arg.required);
//				assertNotNull(arg.name, arg.sequence);
//				assertNotNull(arg.name, arg.ordered);
//				assertNotNull(arg.name, arg.mediaTypes);
//				assertNotNull(arg.name, arg.type);
//				assertNotNull(arg.name, arg.kind);
//				
//				assertEquals(arg.name+" has a decription", true, arg.desc.length() > 0);
//				assertEquals(arg.name+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));
//				
//				if ("source".equals(arg.name)) {
//					assertEquals("input", arg.kind);
//					assertEquals("application/x-dtbook+xml", arg.mediaTypes.get(0));
//					assertEquals(true, arg.ordered);
//					assertEquals(true, arg.required);
//					assertEquals(true, arg.sequence);
//					
//					arg.add("file1.xml");
//					arg.add("file2.xml");
//				}
//				else if ("zedai-filename".equals(arg.name)) {
//					assertEquals("option", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//					assertEquals("string", arg.type);
//					
//					arg.set("zedai.xml");
//				}
//				else if ("assert-valid".equals(arg.name)) {
//					assertEquals("option", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//					assertEquals("boolean", arg.type);
//					
//					arg.set(true);
//				}
//				else if ("output-dir".equals(arg.name)) {
//					assertEquals("option", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(true, arg.required);
//					assertEquals(false, arg.sequence);
//					assertEquals("anyDirURI", arg.type);
//					assertEquals("result", arg.output);
//					
//					arg.set("file:/tmp/text/");
//				}
//				else if ("mods-filename".equals(arg.name)) {
//					assertEquals("option", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//					assertEquals("string", arg.type);
//					
//					arg.set("mods.xml");
//				}
//				else if ("lang".equals(arg.name)) {
//					assertEquals("option", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//					assertEquals("string", arg.type);
//					
//					arg.set("en");
//				}
//				else if ("css-filename".equals(arg.name)) {
//					assertEquals("option", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//					assertEquals("string", arg.type);
//					
//					arg.set("main.css");
//				}
//				else fail("Unknown argument: "+arg.name);
//			}
//			Document jobRequest = Jobs.createJobRequestDocument(script.href, script.arguments, null);
//			
//			script = new Script(response);
//			script.parseFromJobRequest(jobRequest);
//			for (Argument arg : script.arguments) {
//				if ("source".equals(arg.name)) {
//					assertEquals(true, "file1.xml".equals(arg.getContextFile(0)));
//					assertEquals(true, "file2.xml".equals(arg.getContextFile(1)));
//				}
//				else if ("zedai-filename".equals(arg.name)) assertEquals("zedai.xml", arg.get());
//				else if ("assert-valid".equals(arg.name)) assertEquals("true", arg.get());
//				else if ("output-dir".equals(arg.name)) assertEquals("file:/tmp/text/", arg.get());
//				else if ("mods-filename".equals(arg.name)) assertEquals("mods.xml", arg.get());
//				else if ("lang".equals(arg.name)) assertEquals("en", arg.get());
//				else if ("css-filename".equals(arg.name)) assertEquals("main.css", arg.get());
//			}
//
//		} catch (Pipeline2Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testOutputPorts() {
//		try {
//			Client.logger().setLevel(Pipeline2LoggerBak.LEVEL.ALL);
//			Client.setHttpClientImplementation(new MockHttpClient());
//			WSResponse response = Scripts.get("http://localhost:8181/ws", "clientid", "supersecret", "dtbook-validator");
//			System.out.println("response: "+response.asText());
//			Script script = new Script(response);
//			
//			for (Argument arg : script.arguments) {
//				assertNotNull(arg.name);
//				assertNotNull(arg.name, arg.niceName);
//				assertNotNull(arg.name, arg.desc);
//				assertNotNull(arg.name, arg.required);
//				assertNotNull(arg.name, arg.sequence);
//				assertNotNull(arg.name, arg.ordered);
//				assertNotNull(arg.name, arg.mediaTypes);
//				assertNotNull(arg.name, arg.type);
//				assertNotNull(arg.name, arg.kind);
//				
//				assertEquals(arg.name+" has a decription", true, arg.desc.length() > 0);
//				assertEquals(arg.name+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));
//				
//				if ("result".equals(arg.name)) {
//					assertEquals("output", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//				}
//				else if ("schematron-report".equals(arg.name)) {
//					assertEquals("output", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//				}
//				else if ("relaxng-report".equals(arg.name)) {
//					assertEquals("output", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(true, arg.sequence);
//				}
//				else if ("html-report".equals(arg.name)) {
//					assertEquals("output", arg.kind);
//					assertEquals(true, arg.ordered);
//					assertEquals(false, arg.required);
//					assertEquals(false, arg.sequence);
//				}
//			}
//
//		} catch (Pipeline2Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testGetResults() {
//		try {
//			Client.logger().setLevel(Pipeline2LoggerBak.LEVEL.ALL);
//			Client.setHttpClientImplementation(new MockHttpClient());
//			
//			WSResponse response = Jobs.getResult("http://localhost:8181/ws", "clientid", "supersecret", "job1", "F00000 - Don't Worry, Be Happy Lyrics.epub");
//			assertEquals("TEST", response.asText());
//			
//		} catch (Pipeline2Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testGetResultsFromFile() {
//		try {
//			Client.logger().setLevel(Pipeline2LoggerBak.LEVEL.ALL);
//			Client.setHttpClientImplementation(new MockHttpClient());
//			
//			File response = Jobs.getResultFromFile("http://localhost:8181/ws", "clientid", "supersecret", "job1", "option/output-dir/idx/output/zedai.xml");
//			assertNotNull(response);
//			assertTrue(response != null && response.exists());
//			
//		} catch (Pipeline2Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testXPath() {
//		XPathFactory factory = XPathFactory.newInstance();
//		javax.xml.xpath.XPath xpath = factory.newXPath();
//		NamespaceContext context = new NamespaceContextMap(
//				"foo", "http://foo", 
//				"bar", "http://bar");
//
//		xpath.setNamespaceContext(context);
//
//
//		Document xml = XML.getXml("<foo:data xmlns:foo='http://foo' xmlns:bar='http://bar'><bar:foo bar=\"hello\" /></foo:data>");
//		try {
//			XPathExpression compiled = xpath.compile("/foo:data/bar:foo/attribute::bar");
//			NodeList nodeList = (NodeList)compiled.evaluate(xml, XPathConstants.NODESET);
//			assertEquals("bar=\"hello\"", nodeList.item(0).toString());
//		} catch (XPathExpressionException e) {
//			fail(e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//	@Test
//	public void testXPathContext() {
//		Map<String, String> mappings = new HashMap<String, String>();
//		mappings.put("foo", "http://foo");
//		mappings.put("altfoo", "http://foo");
//		mappings.put("bar", "http://bar");
//		mappings.put(XMLConstants.XML_NS_PREFIX,XMLConstants.XML_NS_URI);
//
//		NamespaceContext context = new NamespaceContextMap(mappings);
//		for (Map.Entry<String, String> entry : mappings.entrySet()) {
//			String prefix = entry.getKey();
//			String namespaceURI = entry.getValue();
//
//			Assert.assertEquals("namespaceURI", namespaceURI, context.getNamespaceURI(prefix));
//			boolean found = false;
//			Iterator<?> prefixes = context.getPrefixes(namespaceURI);
//			while (prefixes.hasNext()) {
//				if (prefix.equals(prefixes.next())) {
//					found = true;
//					break;
//				}
//				try {
//					prefixes.remove();
//					Assert.fail("rw");
//				} catch (UnsupportedOperationException e) {
//				}
//			}
//			Assert.assertTrue("prefix: " + prefix, found);
//			Assert.assertNotNull("prefix: " + prefix, context.getPrefix(namespaceURI));
//		}
//
//		Map<String, String> ctxtMap = ((NamespaceContextMap) context).getMap();
//		for (Map.Entry<String, String> entry : mappings.entrySet()) {
//			Assert.assertEquals(entry.getValue(), ctxtMap.get(entry.getKey()));
//		}
//	}
//
//	@Test
//	public void testXPathModify() {
//		NamespaceContextMap context = new NamespaceContextMap();
//
//		try {
//			Map<String, String> ctxtMap = context.getMap();
//			ctxtMap.put("a", "b");
//			Assert.fail("rw");
//		} catch (UnsupportedOperationException e) {
//		}
//
//		try {
//			Iterator<String> it = context.getPrefixes(XMLConstants.XML_NS_URI);
//			it.next();
//			it.remove();
//			Assert.fail("rw");
//		} catch (UnsupportedOperationException e) {
//		}
//	}
//
//	@Test
//	public void testXPathConstants() {
//		NamespaceContext context = new NamespaceContextMap();
//		Assert.assertEquals(XMLConstants.XML_NS_URI, context.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
//		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
//		Assert.assertEquals(XMLConstants.XML_NS_PREFIX, context.getPrefix(XMLConstants.XML_NS_URI));
//		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
//	}
	
}
