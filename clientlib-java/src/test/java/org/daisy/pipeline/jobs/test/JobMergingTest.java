package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.rules.TemporaryFolder;

public class JobMergingTest {
	
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
	
//	@Test
//	public void testTemplates() throws URISyntaxException { // TODO
//		JobStorage job = JobStorage.createJob("job3", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));
//		JobStorage template1 = JobStorage.loadJob("job1", jobStorage);
//		JobStorage template2 = JobStorage.loadJob("job2", jobStorage);
//		
//		// files: add files to context and keep track of which context files are from template and which are not
//		// non-files: overwrite job from template
//		
//		File outputDir = testFolder.newFolder("output-dir");
//		outputDir.mkdirs();
//		
//		job.set("assert-valid", true);
//		job.set("language", "en");
//		job.set("output-dir", outputDir, null);
//		
////		outputType="result|temp" => valid to point to external dir
//		
//		assertEquals(null, job.validate("source"));
//		assertEquals(null, job.validate("assert-valid"));
//		assertEquals(null, job.validate("language"));
//		assertEquals(null, job.validate("output-dir"));
//		assertEquals(null, job.validate());
//		
////		input source required sequence mediaType=application/x-dtbook+xml
////		option language ordered type=string
////		option assert-valid ordered type=boolean
////		option tts-config ordered type=anyFileURI
////		option audio ordered type=boolean
//		
//		/*
//		 * - no 
//		 */
//		
//		// ...assertions...
//		job.setJobTemplate(template1);
//		// ...assertions...
//		job.setJobTemplate(template2);
//		// ...assertions...
//		job.clearJobTemplate();
//		// ...assertions...
//	}
	
}
