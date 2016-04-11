package org.daisy.pipeline.client.http;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import javax.inject.Inject;

import org.daisy.pipeline.client.models.Alive;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Script;

import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class WSTest {
	
	private static WSInterface ws = new WS();
	
	// use this to test the webservice manually in the browser (http://localhost:8181/ws/...)
	//@Test
	public void keepWsAlive() throws InterruptedException {
		Thread.sleep(60000);
	}
	
	@Test
	public void testAlive() {
		Alive alive = ws.alive();
		assertFalse(alive.error);
		assertFalse(alive.authentication);
		assertEquals("1.9", alive.version);
	}
	
	@Test
	public void testScripts() {
		List<Script> scripts = ws.getScripts();
		assertEquals(1, scripts.size());
		Script script = scripts.get(0);
		assertEquals("Example script", script.getNicename());
		// assertEquals(3, script.getInputs().size());
		// Argument option1 = script.getArgument("option-1");
		// assertTrue(option1.getRequired());
		// assertEquals(..., option1.getDataType());
		// Argument option2 = script.getArgument("option-2");
		// assertFalse(option2.getRequired());
		// assertEquals("one", option2.getDefault());
		// assertEquals(..., option2.getDataType());
	}
	
	@Configuration
	public Option[] config() throws MalformedURLException {
		return options(
			systemProperty("org.daisy.pipeline.ws.authentication").value("false"),
			systemProperty("org.daisy.pipeline.version").value("1.9"),
			domTraversalPackage(),
			logbackConfigFile(),
			felixDeclarativeServices(),
			junitBundles(),
			mavenBundlesWithDependencies(
				logbackClassic(),
				// pipeline webservice
				mavenBundle("org.daisy.pipeline:webservice:?"),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"), // org.daisy.common.xproc.XProcEngine
				mavenBundle("org.daisy.pipeline:modules-registry:?"), // javax.xml.transform.URIResolver
				mavenBundle("org.daisy.pipeline:framework-volatile:?"), // org.daisy.pipeline.job.JobStorage
				mavenBundle("org.daisy.pipeline:woodstox-osgi-adapter:?"), // javax.xml.stream.XMLInputFactory
				mavenBundle("org.daisy.pipeline:framework-core:?"), // org.daisy.pipeline.datatypes.DatatypeRegistry
				// these belong in the framework as runtime dependencies
				mavenBundle("commons-io:commons-io:?"),
				mavenBundle("org.daisy.libs:saxon-he:?"),
				mavenBundle("org.daisy.libs:jing:?")
				),
			// example script (incl. datatypes)
			bundle("reference:" + new File(PathUtils.getBaseDir() + "/target/test-classes/example_script/").toURL().toString()),
			// the client must technically not be run in OSGi, however there
			// is no other way to keep the webservice running while the test
			// is executed
			wrappedBundle(
				bundle(new File(PathUtils.getBaseDir() + "/target/clientlib-java-httpclient-2.0.1-SNAPSHOT.jar").toURL().toString()))
				.bundleSymbolicName("org.daisy.pipeline.clientlib-java-httpclient")
				.bundleVersion("2.0.1.SNAPSHOT"),
			wrappedBundle(
				mavenBundle("org.daisy.pipeline:clientlib-java:4.4.2"))
				.bundleSymbolicName("org.daisy.pipeline.clientlib-java")
				.bundleVersion("4.4.2"),
			mavenBundle("org.apache.httpcomponents:httpcore-osgi:?"),
			mavenBundle("org.apache.httpcomponents:httpclient-osgi:?")
		);
	}
}
