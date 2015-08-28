package org.daisy.pipeline.client.models;

import java.util.List;

import org.w3c.dom.Node;

/**
 * A representation of the "/admin/clients" response from the Pipeline 2 Web Service.
 * 
 * GET /admin/clients
 * <clients href="http://localhost:8181/ws/admin/clients" xmlns="http://www.daisy.org/ns/pipeline/data">
 *     <client id="" href="" secret="" role="" contact=""/>
 * </clients>
 * 
 * GET /admin/clients/{clientId}
 * 
 * 
 */
public class Client {
	
    public enum Role { ADMIN, CLIENTAPP };
    
    public String id;
    public String href;
    public String secret;
    public Role role;
    public String contactInfo;
    // priority?
    
	public Client(Node asXml) {
		// TODO Auto-generated constructor stub
	}
    
	public static List<Client> parseClientsXml(Node asXml) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
