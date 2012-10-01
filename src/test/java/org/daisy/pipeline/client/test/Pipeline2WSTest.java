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

import org.daisy.pipeline.client.Jobs;
import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.Scripts;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.client.models.script.arguments.ArgBoolean;
import org.daisy.pipeline.client.models.script.arguments.ArgFile;
import org.daisy.pipeline.client.models.script.arguments.ArgFiles;
import org.daisy.pipeline.client.models.script.arguments.ArgString;
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
			assertNotNull(scripts.get(0).id);
			
			response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret", "dtbook-to-zedai");
			Script script = new Script(response);
			if ("dtbook-to-zedai".equals(script.id)) {
				if (!"DTBook to ZedAI".equals(script.nicename)) fail("dtbook-to-zedai: Wrong nicename");
				if (!"Transforms DTBook XML into ZedAI XML.".equals(script.desc)) fail("dtbook-to-zedai: Wrong description");
				if (script.homepage == null || !"http://code.google.com/p/daisy-pipeline/wiki/DTBookToZedAI".equals(script.homepage.href)) fail("dtbook-to-zedai: Wrong homepage ("+script.homepage+")");
				if (!containsArgument(script.arguments, "source")) fail("dtbook-to-zedai: Missing input: source");
				if (!containsArgument(script.arguments, "zedai-filename")) fail("dtbook-to-zedai: Missing option: zedai-filename");
//				if (!containsArgument(script.arguments, "output-dir")) fail("dtbook-to-zedai: Missing option: output-dir");
				if (!containsArgument(script.arguments, "mods-filename")) fail("dtbook-to-zedai: Missing option: mods-filename");
				if (!containsArgument(script.arguments, "lang")) fail("dtbook-to-zedai: Missing option: lang");
				if (!containsArgument(script.arguments, "css-filename")) fail("dtbook-to-zedai: Missing option: css-filename");
				for (Argument arg : script.arguments) {
					if ("source".equals(arg.name)) {
						if (!"One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.".equals(arg.desc)) fail("dtbook-to-zedai: Argument source: Wrong description");
						if (!"application/x-dtbook+xml".equals(arg.mediaTypes.get(0))) fail("dtbook-to-zedai: Argument source: Wrong mediaType");
						if (!"true".equals(arg.sequence+"")) fail("dtbook-to-zedai: Argument source: Wrong sequence");							
						
					} else if ("zedai-filename".equals(arg.name)) {
						if (!"Filename for the generated ZedAI file".equals(arg.desc)) fail("dtbook-to-zedai: Argument zedai-filename: Wrong description");
						if (!"true".equals(arg.ordered+"")) fail("dtbook-to-zedai: Argument zedai-filename: Wrong ordered");
						if (!"false".equals(arg.required+"")) fail("dtbook-to-zedai: Argument zedai-filename: Wrong required");
						if (!"false".equals(arg.sequence+"")) fail("dtbook-to-zedai: Argument zedai-filename: Wrong sequence");
						if (!"string".equals(arg.xsdType)) fail("dtbook-to-zedai: Argument zedai-filename: Wrong type");
						
					} else if ("output-dir".equals(arg.name)) {
						if (!"The directory to store the generated files in.".equals(arg.desc)) fail("dtbook-to-zedai: Argument output-dir: Wrong description");
						if (!"true".equals(arg.ordered+"")) fail("dtbook-to-zedai: Argument output-dir: Wrong ordered");
						if (!"true".equals(arg.required+"")) fail("dtbook-to-zedai: Argument output-dir: Wrong required");
						if (!"false".equals(arg.sequence+"")) fail("dtbook-to-zedai: Argument output-dir: Wrong sequence");
						if (!"anyDirURI".equals(arg.xsdType)) fail("dtbook-to-zedai: Argument output-dir: Wrong type");
						
					} else if ("mods-filename".equals(arg.name)) {
						if (!"Filename for the generated MODS file".equals(arg.desc)) fail("dtbook-to-zedai: Argument mods-filename: Wrong description");
						if (!"true".equals(arg.ordered+"")) fail("dtbook-to-zedai: Argument mods-filename: Wrong ordered");
						if (!"false".equals(arg.required+"")) fail("dtbook-to-zedai: Argument mods-filename: Wrong required");
						if (!"false".equals(arg.sequence+"")) fail("dtbook-to-zedai: Argument mods-filename: Wrong sequence");
						if (!"string".equals(arg.xsdType)) fail("dtbook-to-zedai: Argument mods-filename: Wrong type");
						
					} else if ("lang".equals(arg.name)) {
						if (!"Language code of the input document.".equals(arg.desc)) fail("dtbook-to-zedai: Argument lang: Wrong description");
						if (!"true".equals(arg.ordered+"")) fail("dtbook-to-zedai: Argument lang: Wrong ordered");
						if (!"false".equals(arg.required+"")) fail("dtbook-to-zedai: Argument lang: Wrong required");
						if (!"false".equals(arg.sequence+"")) fail("dtbook-to-zedai: Argument lang: Wrong sequence");
						if (!"string".equals(arg.xsdType)) fail("dtbook-to-zedai: Argument lang: Wrong type");
						
					} else if ("css-filename".equals(arg.name)) {
						if (!"Filename for the generated CSS file".equals(arg.desc)) fail("dtbook-to-zedai: Argument css-filename: Wrong description");
						if (!"true".equals(arg.ordered+"")) fail("dtbook-to-zedai: Argument css-filename: Wrong ordered");
						if (!"false".equals(arg.required+"")) fail("dtbook-to-zedai: Argument css-filename: Wrong required");
						if (!"false".equals(arg.sequence+"")) fail("dtbook-to-zedai: Argument css-filename: Wrong sequence");
						if (!"string".equals(arg.xsdType)) fail("dtbook-to-zedai: Argument css-filename: Wrong type");
					
					} else if ("assert-valid".equals(arg.name)) {
						if (!"Whether to stop processing and raise an error on validation issues.".equals(arg.desc)) fail("dtbook-to-zedai: Argument assert-valid: Wrong description");
						if (!"true".equals(arg.ordered+"")) fail("dtbook-to-zedai: Argument css-filename: Wrong ordered");
						if (!"false".equals(arg.required+"")) fail("dtbook-to-zedai: Argument css-filename: Wrong required");
						if (!"false".equals(arg.sequence+"")) fail("dtbook-to-zedai: Argument css-filename: Wrong sequence");
						if (!"boolean".equals(arg.xsdType)) fail("dtbook-to-zedai: Argument css-filename: Wrong type");
						
					} else {
						fail("dtbook-to-zedai: Unknown argument: "+arg.name);
					}
				}
			}

		} catch (Pipeline2WSException e) {
			fail(e.getMessage());
		}
	}
	
	private boolean containsArgument(List<Argument> arguments, String argName) {
		for (Argument arg : arguments) {
			if (argName.equals(arg.name))
				return true;
		}
		return false;
	}
	
	@Test
	public void testParseJobRequest() {
		try {
			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
			Pipeline2WSResponse response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret", "dtbook-to-zedai");
			Script script = new Script(response);
			for (Argument arg : script.arguments) {
				if ("source".equals(arg.name)) {arg.add("file1.xml"); arg.add("file2.xml");}
				else if ("zedai-filename".equals(arg.name)) arg.set("zedai.xml");
				else if ("assert-valid".equals(arg.name)) arg.set(true);
				else if ("output-dir".equals(arg.name)) arg.set("file:/tmp/text/");
				else if ("mods-filename".equals(arg.name)) arg.set("mods.xml");
				else if ("lang".equals(arg.name)) arg.set("en");
				else if ("css-filename".equals(arg.name)) arg.set("main.css");
			}
			Document jobRequest = Jobs.createJobRequestDocument(script.href, script.arguments, null);
			
			script = new Script(response);
			script.parseFromJobRequest(jobRequest);
			for (Argument arg : script.arguments) {
				if ("source".equals(arg.name)) {
					assert("file1.xml".equals(((ArgFiles)arg).hrefs.get(0)));
					assert("file2.xml".equals(((ArgFiles)arg).hrefs.get(1)));
				}
				else if ("zedai-filename".equals(arg.name)) assertEquals("zedai.xml", ((ArgString)arg).value);
				else if ("assert-valid".equals(arg.name)) assertEquals(true, ((ArgBoolean)arg).value);
				else if ("output-dir".equals(arg.name)) assertEquals("file:/tmp/text/", ((ArgFile)arg).href);
				else if ("mods-filename".equals(arg.name)) assertEquals("mods.xml", ((ArgString)arg).value);
				else if ("lang".equals(arg.name)) assertEquals("en", ((ArgString)arg).value);
				else if ("css-filename".equals(arg.name)) assertEquals("main.css", ((ArgString)arg).value);
			}

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
