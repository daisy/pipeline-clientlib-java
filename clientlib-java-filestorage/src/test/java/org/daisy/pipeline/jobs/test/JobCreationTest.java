package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//import javax.xml.XMLConstants;
//import javax.xml.namespace.NamespaceContext;
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpression;
//import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;

import org.junit.rules.TemporaryFolder;
import org.daisy.pipeline.client.persistence.DP2PersistentClientImpl;
import org.daisy.pipeline.client.utils.XML;


public class JobCreationTest {
	
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
	
	public TemporaryFolder testFolder;
	public File jobStorage;
	
	@Before
	public void populateTestFolder() throws IOException {
		testFolder = new TemporaryFolder();
		testFolder.create();
		jobStorage = testFolder.newFolder("jobs");
		File sourceFolder = new File(resources, "jobs");
		JobStorageTest.copyFolder(sourceFolder, jobStorage);
	}
	
	@After
	public void tearDown() {
		testFolder.delete();
	}

	@Test
	public void testCreateJob() {
		DP2PersistentClientImpl job = DP2PersistentClientImpl.createJob("job3", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));
		assertNotNull(job);
	}

	@Test
	public void testBuildJobBoolean() {
		DP2PersistentClientImpl job = DP2PersistentClientImpl.createJob("job3", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));

		// set to a boolean value
		job.set("boolean", true);
		assertEquals(1, job.getCount("boolean"));
		assertEquals("true", job.get("boolean"));
		assertEquals(true, job.getAsBoolean("boolean", false));
		assertEquals(0, job.getAsInteger("boolean", 0));
		assertEquals(0.0, job.getAsDouble("boolean", 0.0), 0.0);
		assertArrayEquals(new String[]{"true"}, job.getAsList("boolean").toArray());
		assertTrue(Arrays.equals(new Boolean[]{true}, job.getAsBooleanList("boolean", false).toArray()));
		assertArrayEquals(new Integer[]{0}, job.getAsIntegerList("boolean", 0).toArray());
		assertArrayEquals(new Double[]{0.0}, job.getAsDoubleList("boolean", 0.0).toArray(new Double[0]));
		assertEquals("true", job.get("boolean", "default"));
		assertEquals("default", job.get("unset-boolean", "default"));
	}

	@Test
	public void testBuildJobString() {
		DP2PersistentClientImpl job = DP2PersistentClientImpl.createJob("job3", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));

		// set to a string value
		job.set("string", "value");
		assertEquals(1, job.getCount("string"));
		assertEquals("value", job.get("string"));
		assertEquals(false, job.getAsBoolean("string", false));
		assertEquals(0, job.getAsInteger("string", 0));
		assertEquals(0.0, job.getAsDouble("string", 0.0), 0.0);
	}

	@Test
	public void testBuildJobUnset() {
		DP2PersistentClientImpl job = DP2PersistentClientImpl.createJob("job3", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));

		// set to a value, and then set to a undefined value (null)
		job.set("undeclare", "value");
		assertEquals(1, job.getCount("undeclare"));
		assertEquals("value", job.get("undeclare"));
		job.unset("undeclare");
		assertEquals(0, job.getCount("undeclare"));
		assertNull(job.get("undeclare"));
		assertEquals(false, job.getAsBoolean("undeclare", false));
		assertEquals(0, job.getAsInteger("undeclare", 0));
		assertEquals(0.0, job.getAsDouble("undeclare", 0.0), 0.0);
		assertArrayEquals(new String[]{}, job.getAsList("undeclare").toArray(new String[0]));
	}

	@Test
	public void testBuildJobLists() {
		DP2PersistentClientImpl job = DP2PersistentClientImpl.createJob("job3", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));

		List<String> stringList = new ArrayList<String>();
		stringList.add("foo");
		stringList.add("bar");
		stringList.add("baz");
		job.set("string-list", stringList);
		assertEquals(3, job.getCount("string-list"));

		List<String> otherStringList = new ArrayList<String>();
		otherStringList.add("1");
		otherStringList.add("2");
		otherStringList.add("3");
		job.set("string-list", stringList);
		assertEquals(3, job.getCount("string-list"));
		for (String value : otherStringList) {
			job.add("string-list", value);
		}
		assertEquals(6, job.getCount("string-list"));
		job.set("string-list", stringList);
		assertEquals(3, job.getCount("string-list"));

		for (int i = 1; i <= 10; i++) {
			job.add("append-list", i);
		}
		assertEquals(10, job.getCount("append-list"));
	}
	
}
