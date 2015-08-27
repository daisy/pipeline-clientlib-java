package org.daisy.pipeline.client.models;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "alive" response from the Pipeline 2 Web Service.
 * 
 * @author jostein
 */
public class Client {
	
	public enum Priority { LOW, MEDIUM, HIGH };
    public enum Role { ADMIN, CLIENTAPP };
    
    public String id;
    public Role role;
    public String contactInfo;
    public Priority priority;
    
	// TODO: implement these somewhere (maybe in some API/interface, or maybe here) to harmonize with ScriptRegistry.java
    public List<Client> getAll();
    public Optional<Client> get(String id);
    public boolean delete(String id);
    public Optional<Client> update(String id, String secret, Role role, String contactInfo, Priority priority);
    public Optional<Client> addClient(String id, String secret, Role role, String contactInfo, Priority priority);
    public Optional<Client> addClient(String id, String secret, Role role, String contactInfo);
	
}
