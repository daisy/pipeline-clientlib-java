package org.daisy.pipeline.client.models;

import java.util.List;

import org.w3c.dom.Document;

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
    
	public static List<Property> parsePropertiesXml(Document xml) {
		// TODO Auto-generated method stub
		return null;
	}
    
    // TODO: any other methods?
	
}
