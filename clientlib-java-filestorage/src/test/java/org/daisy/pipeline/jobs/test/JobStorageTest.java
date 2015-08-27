package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.persistence.DP2PersistentClientImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JobStorageTest {
	
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
		copyFolder(sourceFolder, jobStorage);
	}
	
	@After
	public void tearDown() {
//		testFolder.delete();
	}
	
	public static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists()) {
            	destinationFolder.mkdir();
            }
            String files[] = sourceFolder.list();
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);
                copyFolder(srcFile, destFile);
            }
            
        } else {
    		Files.copy(sourceFolder.toPath(), destinationFolder.toPath());
        }
    }
	
	@Test
	public void testListAndLoadJobs() {
		List<DP2PersistentClientImpl> jobs = DP2PersistentClientImpl.listJobs(jobStorage);
		assertEquals(2, jobs.size());
		
		
		DP2PersistentClientImpl job = jobs.get(0);
		
		assertEquals("job1", job.getId());
		assertEquals("Nicename", job.getNicename());
		assertEquals("http://localhost:8181/ws/scripts/dtbook-to-epub3", job.getScriptHref());
		assertEquals("dtbook-to-epub3", job.getScriptId());
		
		assertEquals(4, job.getNames().size());
		assertTrue(job.getNames().contains("source"));
		assertTrue(job.getNames().contains("language"));
		assertTrue(job.getNames().contains("assert-valid"));
		assertTrue(job.getNames().contains("audio"));
		
		assertEquals(2, job.getCount("source"));
		assertEquals(1, job.getCount("language"));
		assertEquals(1, job.getCount("assert-valid"));
		assertEquals(1, job.getCount("audio"));
		
		assertEquals("hauy_valid.xml", job.getAsList("source").get(0));
		assertEquals("dtbook.2005.basic.css", job.getAsList("source").get(1));
		assertEquals("en", job.get("language"));
		assertEquals(true, job.getAsBoolean("assert-valid", false));
		assertEquals(true, job.getAsBoolean("audio", false));
		
		
		job = jobs.get(1);
		
		assertEquals("job2", job.getId());
		assertEquals("Other nicename", job.getNicename());
		assertEquals("http://localhost:8181/ws/scripts/dtbook-to-epub3", job.getScriptHref());
		assertEquals("dtbook-to-epub3", job.getScriptId());
		
		assertEquals(3, job.getNames().size());
		assertTrue(job.getNames().contains("source"));
		assertFalse(job.getNames().contains("language"));
		assertTrue(job.getNames().contains("assert-valid"));
		assertTrue(job.getNames().contains("audio"));
		
		assertEquals(1, job.getCount("source"));
		assertEquals(0, job.getCount("language"));
		assertEquals(1, job.getCount("assert-valid"));
		assertEquals(1, job.getCount("audio"));
		
		assertEquals("hauy_valid.xml", job.get("source"));
		assertEquals(null, job.get("language"));
		assertEquals(false, job.getAsBoolean("assert-valid", false));
		assertEquals(false, job.getAsBoolean("audio", false));
	}
	
	@Test
	public void testDeleteJob() {
		List<String> jobsBefore = new ArrayList<String>();
		for (DP2PersistentClientImpl job : DP2PersistentClientImpl.listJobs(jobStorage)) {
			jobsBefore.add(job.getId());
		}
		
		DP2PersistentClientImpl job2 = DP2PersistentClientImpl.loadJob("job2", jobStorage);
		job2.delete();
		
		List<String> jobsAfter = new ArrayList<String>();
		for (DP2PersistentClientImpl job : DP2PersistentClientImpl.listJobs(jobStorage)) {
			jobsAfter.add(job.getId());
		}
		
		assertEquals(2, jobsBefore.size());
		assertEquals(1, jobsAfter.size());
		assertTrue(jobsAfter.contains("job1"));
		assertFalse(jobsAfter.contains("job2"));
	}
	
	@Test
	public void testStoreJob() {
		List<String> jobsBefore = new ArrayList<String>();
		for (DP2PersistentClientImpl job : DP2PersistentClientImpl.listJobs(jobStorage)) {
			jobsBefore.add(job.getId());
		}
		
		DP2PersistentClientImpl newJob = DP2PersistentClientImpl.createJob("job3", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));
		newJob.set("source", "filename.xml");
		newJob.set("assert-valid", "false");
		newJob.save();
		
		List<String> jobsAfter = new ArrayList<String>();
		for (DP2PersistentClientImpl job : DP2PersistentClientImpl.listJobs(jobStorage)) {
			jobsAfter.add(job.getId());
		}
		
		assertEquals(2, jobsBefore.size());
		assertEquals(3, jobsAfter.size());
		assertTrue(jobsAfter.contains("job1"));
		assertTrue(jobsAfter.contains("job2"));
		assertTrue(jobsAfter.contains("job3"));
		assertTrue(new File(jobStorage, "job3/jobRequest.xml").isFile());
		assertTrue(new File(jobStorage, "job3/script.xml").isFile());
		assertTrue(new File(jobStorage, "job3/context").isDirectory());
	}
	
	@Test
	public void testFiles() {
		DP2PersistentClientImpl job = DP2PersistentClientImpl.createJob("filesJob", jobStorage, loadResource("scripts/dtbook-to-epub3.xml"));
		File xmlFile = new File(jobStorage, "job1/context/hauy_valid.xml");
		File cssFile = new File(jobStorage, "job1/context/dtbook.2005.basic.css");
		
		job.set("source", xmlFile);
		assertEquals(1, job.getCount("source"));
		assertEquals("hauy_valid.xml", job.get("source"));
		
		job.set("source", cssFile);
		assertEquals(1, job.getCount("source"));
		assertEquals("dtbook.2005.basic.css", job.get("source"));
		
		job.add("source", xmlFile);
		assertEquals(2, job.getCount("source"));
		assertEquals("hauy_valid.xml", job.getAsList("source").get(1));
		
		job.add("output-dir", testFolder.getRoot());
		assertEquals(1, job.getCount("output-dir"));
		try {
			assertEquals("file:"+testFolder.getRoot().getCanonicalPath()+"/", job.get("output-dir"));
		} catch (IOException e) {
			assertTrue(false);
		}
	}

}
