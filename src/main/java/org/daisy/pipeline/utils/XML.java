package org.daisy.pipeline.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Utility class for parsing and serializing XML.
 * 
 * @author jostein
 */
public class XML {
	
	/**
	 * Parse an XML string as DOM.
	 */
	public static Document getXml(String xml, String encoding) {
		try {
            return getXml(new ByteArrayInputStream(xml.getBytes(encoding)), encoding);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
	}
	
	/**
     * Parse an XML string as DOM, with "utf-8" as default encoding.
     * (copied from Play 2.0; play/libs/XML/XML.java)
     */
    public static Document getXml(String xml) {
        return getXml(xml, "utf-8");
    }
    
    /**
     * Parse an InputStream as DOM, with "utf-8" as default encoding.
     */
    public static Document getXml(InputStream in) {
    	return getXml(in, "utf-8");
    }
    
    /**
     * Parse an InputStream as DOM.
     * (based on Play 2.0; play/libs/XML/XML.java)
     */
    public static Document getXml(InputStream in, String encoding) {
       DocumentBuilderFactory factory = null;
       DocumentBuilder builder = null;
       Document ret = null;

       try {
           factory = DocumentBuilderFactory.newInstance();
           factory.setNamespaceAware(true);
           builder = factory.newDocumentBuilder();
//         builder.setEntityResolver(new NoOpEntityResolver());
       } catch (ParserConfigurationException e) {
           throw new RuntimeException(e);
       }

       try {
           InputSource is = new InputSource(in);
           is.setEncoding(encoding);
           ret = builder.parse(is);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }

       return ret;
    }
    
    /**
     * Serialize an XML node as a string.
     * @param xml
     * @return
     */
    public static String toString(Node xml) {
    	try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(xml);
			transformer.transform(source, result);
			return result.getWriter().toString();
			
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
    }
    
}