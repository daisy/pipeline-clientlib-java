package org.daisy.pipeline.client.models;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "/admin/properties" response from the Pipeline 2 Web Service.
 * 
 * Example XML:
 * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * <properties href="http://localhost:8181/ws/admin/properties" xmlns="http://www.daisy.org/ns/pipeline/data">
 *     <property bundleId="36" bundleName="org.daisy.pipeline.persistence-derby" name="javax.persistence.jdbc.driver" value="org.apache.derby.jdbc.EmbeddedDriver"/>
 *     <property bundleId="14" bundleName="org.daisy.pipeline.webservice" name="org.daisy.pipeline.ws.host" value="localhost"/>
 *     <property bundleId="14" bundleName="org.daisy.pipeline.webservice" name="org.daisy.pipeline.ws.path" value="/ws"/>
 *     <property bundleId="14" bundleName="org.daisy.pipeline.webservice" name="org.daisy.pipeline.ws.port" value="8181"/>
 * </properties>
 */
public class Property {
	
	public String name;
    public String value;
    public long bundleId;
    public String bundleName;
    
    // TODO: methods?
	
}
