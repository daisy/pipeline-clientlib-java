package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.models.Alive;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Argument.Kind;
import org.daisy.pipeline.client.models.Job.Status;
import org.daisy.pipeline.client.models.Message;
import org.daisy.pipeline.client.models.Result;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.utils.NamespaceContextMap;
import org.daisy.pipeline.client.utils.XML;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ParsingTest {

	private static String resourceBaseUri = new File("src/test/resources/responses/").toURI().toString();

	private File resources = new File("src/test/resources/");
	private String loadResource(String href) {
		File scriptXmlFile = new File(resources, href);
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(scriptXmlFile.getPath()));
			return new String(encoded, Charset.defaultCharset());
		} catch (IOException e) {
			assertTrue("Failed to read "+scriptXmlFile.getPath(), false);
			return null;
		}
	}
	private Document loadResourceXml(String href) {
		String xmlString = loadResource(href);
		return XML.getXml(xmlString);
	}

	@Test
	public void getScripts() {
		try {

			List<Script> scripts = Script.parseScriptsXml(loadResourceXml("responses/scripts.xml"));
			if (scripts.size() == 0)
				fail("no scripts in response");
			if (scripts.get(0).getId() == null || scripts.get(0).getId().length() == 0)
				fail("empty script id");
			if (scripts.get(0).getNicename() == null || scripts.get(0).getNicename().length() == 0)
				fail("empty nicename id");
			if (scripts.get(0).getDescription() == null || scripts.get(0).getDescription().length() == 0)
				fail("empty script description");

			String[] orderedScripts = new String[]{"daisy202-to-epub3", "dtbook-to-epub3", "dtbook-to-zedai", "zedai-to-epub3", "zedai-to-pef"};
			for (int s = 0; s < orderedScripts.length; s++) {
				if (!orderedScripts[s].equals(scripts.get(s).getId()))
					fail("scripts list must be ordered alphabetically by id ("+scripts.get(s).getId()+" at position "+s+")");
			}

			assertNotNull(scripts.get(0).getId());

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseAliveResponse() {
		try {
			Alive alive = new Alive(loadResourceXml("responses/alive.xml"));

			assertNotNull(alive);
			assertEquals(false, alive.authentication);
			assertEquals(true, alive.localfs);
			assertEquals(false, alive.error);
			assertEquals("1.6", alive.version);

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseJobResponse() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job1.xml"));

			assertNotNull(job);
			assertEquals("job1", job.getId());
			assertEquals("http://localhost:8181/ws/jobs/job1", job.getHref());
			assertEquals(Status.DONE, job.getStatus());
			assertNotNull(job.getScript());
			assertEquals("http://localhost:8181/ws/scripts/dtbook-to-zedai", job.getScript().getHref());
			assertEquals("dtbook-to-zedai", job.getScript().getId());
			assertEquals("DTBook to ZedAI", job.getScript().getNicename());
			assertEquals("Transforms DTBook XML into ZedAI XML.", job.getScript().getDescription());
			//			assertNotNull(job.messagesNode);
			//			assertNotNull(job.resultsNode);
			assertEquals("http://localhost:8181/ws/jobs/job1/log", job.getLogHref());

			List<Message> messages = job.getMessages();
			Result result = job.getResult();

			assertEquals(62, messages.size());

			assertNull(result.from);
			assertNull(result.file);
			assertEquals("application/zip", result.mimeType);
			assertEquals(new Long(178080), result.size);
			assertNull(result.name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result", result.href);
			assertEquals("result", result.relativeHref);
			assertEquals("result", result.filename);

			SortedMap<Result, List<Result>> allResults = job.getResults();
			assertEquals(1, allResults.size());

			result = job.getResult("output-dir");
			List<Result> results = job.getResults("output-dir");
			assertEquals("option", result.from);
			assertEquals(null, result.file);
			assertEquals("application/zip", result.mimeType);
			assertEquals(new Long(178080), result.size);
			assertEquals("output-dir", result.name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir", result.href);
			assertEquals("option/output-dir", result.relativeHref);
			assertEquals("output-dir", result.filename);
			assertEquals(4, results.size());

			assertEquals(null, results.get(1).from);
			assertEquals("/responses/jobs/job1/result/option/output-dir/idx/output-dir/valentin.jpg", results.get(1).file);
			assertEquals(null, results.get(1).mimeType);
			assertEquals(new Long(25740), results.get(1).size);
			assertEquals(null, results.get(1).name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output-dir/valentin.jpg", results.get(1).href);
			assertEquals("valentin.jpg", results.get(1).relativeHref);
			assertEquals("valentin.jpg", results.get(1).filename);

			assertEquals(null, results.get(2).from);
			assertEquals("/responses/jobs/job1/result/option/output-dir/idx/output-dir/zedai-mods.xml", results.get(2).file);
			assertEquals(null, results.get(2).mimeType);
			assertEquals(new Long(442), results.get(2).size);
			assertEquals(null, results.get(2).name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output-dir/zedai-mods.xml", results.get(2).href);
			assertEquals("zedai-mods.xml", results.get(2).relativeHref);
			assertEquals("zedai-mods.xml", results.get(2).filename);

			assertEquals(null, results.get(3).from);
			assertEquals("/responses/jobs/job1/result/option/output-dir/idx/output-dir/zedai.xml", results.get(3).file);
			assertEquals(null, results.get(3).mimeType);
			assertEquals(new Long(151891), results.get(3).size);
			assertEquals(null, results.get(3).name);
			assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output-dir/zedai.xml", results.get(3).href);
			assertEquals("zedai.xml", results.get(3).relativeHref);
			assertEquals("zedai.xml", results.get(3).filename);

			job = new Job(loadResourceXml("responses/jobs/job2.xml"));

			messages = job.getMessages();
			result = job.getResult();
			Result outputDirResult = job.getResult("output-dir");
			results = job.getResults().get(outputDirResult);

			assertNotNull(result);
			assertEquals("result", result.filename);
			assertEquals("result", result.relativeHref);
			assertEquals("output-dir", outputDirResult.filename);
			assertEquals("ValentinHaüythefatheroftheeducationfortheblind.epub", results.get(0).relativeHref);
			assertEquals("mimetype", results.get(25).filename);
			assertEquals("epub/mimetype", results.get(25).relativeHref);
			assertEquals("OpenDyslexic-Regular.otf", results.get(19).filename);
			assertEquals("epub/EPUB/Content/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", results.get(19).relativeHref);

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseJobRequest() {
		try {
			Job job = new Job(loadResourceXml("responses/jobRequest.xml"));

			assertNotNull(job);

			for (Argument arg : job.getArguments()) {
				assertNotNull(arg.getName());
				assertNotNull(arg.getNicename());
				assertNotNull(arg.getDesc());
				assertNotNull(arg.getRequired());
				assertNotNull(arg.getSequence());
				assertNotNull(arg.getOrdered());
				assertNotNull(arg.getMediaTypes());
				assertNotNull(arg.getType());
				assertNotNull(arg.getKind());

				if ("source".equals(arg.getName())) {
					assertEquals(Kind.input, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("dtbook-basic.xml", arg.get());
				}
				else if ("mods-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-mods-file.xml", arg.get());
				}
				else if ("css-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-css-file.css", arg.get());
				}
				else if ("zedai-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-zedai-file.xml", arg.get());
				}
				else fail("Unknown argument: "+arg.getName());
			}

			Document jobRequest = job.toJobRequestXml();

			Job reparsedJob = new Job(jobRequest);
			for (Argument arg : reparsedJob.getArguments()) {
				assertNotNull(arg.getName());
				assertNotNull(arg.getNicename());
				assertNotNull(arg.getDesc());
				assertNotNull(arg.getRequired());
				assertNotNull(arg.getSequence());
				assertNotNull(arg.getOrdered());
				assertNotNull(arg.getMediaTypes());
				assertNotNull(arg.getType());
				assertNotNull(arg.getKind());

				if ("source".equals(arg.getName())) {
					assertEquals(Kind.input, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("dtbook-basic.xml", arg.get());
				}
				else if ("mods-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-mods-file.xml", arg.get());
				}
				else if ("css-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-css-file.css", arg.get());
				}
				else if ("zedai-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-zedai-file.xml", arg.get());
				}
				else fail("Unknown argument: "+arg.getName());
			}

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testOutputPorts() {
		try {
			Script script = new Script(loadResourceXml("responses/scripts/dtbook-validator.xml"));

			assertNotNull(script);

			for (Argument arg : script.getArguments()) {
				assertNotNull(arg.getName());
				assertNotNull(arg.getNicename());
				assertNotNull(arg.getDesc());
				assertNotNull(arg.getRequired());
				assertNotNull(arg.getSequence());
				assertNotNull(arg.getOrdered());
				assertNotNull(arg.getMediaTypes());
				assertNotNull(arg.getType());
				assertNotNull(arg.getKind());

				assertEquals(arg.getName()+" has a decription", true, arg.getDesc().length() > 0);
				assertEquals(arg.getName()+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));

				if ("result".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(true, arg.getRequired());
					assertEquals(false, arg.getSequence());
				}
				else if ("schematron-report".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(true, arg.getRequired());
					assertEquals(false, arg.getSequence());
				}
				else if ("relaxng-report".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(true, arg.getRequired());
					assertEquals(true, arg.getSequence());
				}
				else if ("html-report".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(true, arg.getRequired());
					assertEquals(false, arg.getSequence());
				}
			}

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetResults() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job1.xml"));
			assertNotNull(job);

			Result epubResult = job.getResult("output-dir", "F00000 - Don't Worry, Be Happy Lyrics.epub.xml");
			assertNotNull(epubResult);

			// test XML does not contain absolute file URLs, so we prepend the absolute path here
			epubResult.file = resources.toURI().toString()+epubResult.file;

			File file = epubResult.asFile();
			assertNotNull(file);

			String text = epubResult.asText();
			assertNotNull(text);

			Document xml = epubResult.asXml();
			assertNotNull(xml);

			assertTrue(file.exists());
			assertEquals("F00000 - Don't Worry, Be Happy Lyrics.epub.xml", file.getName());
			assertEquals("<TEST/>\n", text);
			assertEquals("TEST", xml.getDocumentElement().getLocalName());

		} catch (Pipeline2Exception e) {
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
