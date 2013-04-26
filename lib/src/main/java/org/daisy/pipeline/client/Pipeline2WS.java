package org.daisy.pipeline.client;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;

import org.daisy.pipeline.client.http.*;

/**
 * Methods for communicating directly with the Pipeline 2 Web Service.
 * 
 * @author jostein
 */
public class Pipeline2WS {
	
	/** Used to provide a the namespace when querying a document using XPath. */
	public static final Map<String, String> ns; 
	static {
    	Map<String, String> nsMap = new HashMap<String, String>();
    	nsMap.put("d", "http://www.daisy.org/ns/pipeline/data");
    	ns = Collections.unmodifiableMap(nsMap);
	}
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	public static DateFormat iso8601;
	static {
		iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		iso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	
	// HTTP-related stuff:
	
	/** The HTTP Client implementation */
	private static DP2HttpClient httpClient = null;
	
	/** The HTTP Server implementation for handling callbacks */
	private static DP2HttpCallbackServer callbackServer = null;
	
	/** The Handler that handles the callbacks. Set it with handleCallbacks(...) */
	public static Pipeline2WSCallbackHandler callbackHandler = null;
	
	
	// Logging-related stuff:
	
	/** The logger implementation. Set it with setLogger(...) */
	private static Pipeline2WSLogger logger;
	
	public static void init() {
		if (logger == null)
			logger = new Pipeline2WSLoggerImpl();
		if (httpClient == null)
			httpClient = new DP2HttpClientImpl();
	}
	
	public static Pipeline2WSLogger logger() {
		init();
		return logger;
	}
	
	/**
	 * Sign a URL for communication with a Pipeline 2 Web Service running in authenticated mode.
	 * 
	 * @param endpoint
	 * @param path
	 * @param username
	 * @param secret
	 * @param parameters
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static String url(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2WSException {
		init();
		boolean hasAuth = !(username == null || "".equals(username) || secret == null || "".equals(secret));
		
		String url = endpoint + path;
		if (parameters != null && parameters.size() > 0 || hasAuth)
			url += "?";
		
		if (parameters != null) {
			for (String name : parameters.keySet()) {
				try { url += URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(parameters.get(name), "UTF-8") + "&"; }
				catch (UnsupportedEncodingException e) { throw new Pipeline2WSException("Unsupported encoding: UTF-8", e); }
			}
		}
		
		if (hasAuth) {
			String time = iso8601.format(new Date());

			String nonce = "";
			while (nonce.length() < 30)
				nonce += (Math.random()+"").substring(2);
			nonce = nonce.substring(0, 30);

			url += "authid="+username + "&time="+time + "&nonce="+nonce;

			String hash = "";
			try {
				hash = calculateRFC2104HMAC(url, secret);
				String hashEscaped = "";
				char c;
				for (int i = 0; i < hash.length(); i++) {
					// Base64 encoding uses + which we have to encode in URL parameters.
					// Hoping this for loop is more efficient than the equivalent replace("\\+","%2B") regex.
					c = hash.charAt(i);
					if (c == '+') hashEscaped += "%2B";
					else hashEscaped += c;
				} 
				url += "&sign="+hashEscaped;

			} catch (SignatureException e) {
				throw new Pipeline2WSException("Could not sign request.");
			}
		}
		
		return url;
	}
	
	// adapted slightly from
    // http://docs.amazonwebservices.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/index.html?AuthJavaSampleHMACSignature.html
	// (copied from Pipeline 2 fwk code)
    /**
    * Computes RFC 2104-compliant HMAC signature.
    * * @param data
    * The data to be signed.
    * @param secret
    * The signing secret.
    * @return
    * The Base64-encoded RFC 2104-compliant HMAC signature.
    * @throws
    * java.security.SignatureException when signature generation fails
    */
    private static String calculateRFC2104HMAC(String data, String secret) throws java.security.SignatureException {
        byte[] result;
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingSecret = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingSecret);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            result = Base64.encodeBase64(rawHmac);

        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return new String(result);
    }

	public static void setHttpClientImplementation(DP2HttpClient httpClientImpl) {
		Pipeline2WS.httpClient = httpClientImpl;
	}
	
	/**
	 * Send a GET request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parameters URL query string parameters
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2WSException {
		init();
		logger.debug("getting from "+endpoint+path);
		return httpClient.get(endpoint, path, username, secret, parameters);
	}
	
	/**
	 * POST an XML document.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param xml The XML document to post.
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2WSException {
		init();
		logger.debug("posting XML to "+endpoint+path);
		return httpClient.postXml(endpoint, path, username, secret, xml);
	}
	
	/**
	 * POST a multipart request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parts A map of all the parts.
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2WSException {
		init();
		logger.debug("posting files to "+endpoint+path);
		return httpClient.postMultipart(endpoint, path, username, secret, parts);
	}
	
	/**
	 * Send a DELETE request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parameters URL query string parameters
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse delete(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2WSException {
		init();
		logger.debug("deleting from "+endpoint+path);
		return httpClient.delete(endpoint, path, username, secret, parameters);
	}
	
	/**
	 * Set the HTTP server implementation to use for handling callbacks. Defaults to DP2HttpCallbackServer.
	 * @param callbackServerImpl
	 */
	public static void setHttpCallbackServerImplementation(DP2HttpCallbackServer callbackServerImpl) {
		callbackServer = callbackServerImpl;
		logger.debug("set callback server implementation to "+(callbackServer==null?null:callbackServerImpl.getClass().getCanonicalName()));
	}
	
	/**
	 * To handle callbacks, pass an instance of your Pipeline2WSCallbackHandler to this method.
	 * A web service will be started on the given port to handle the callbacks.
	 * 
	 * @param callbackHandler Your callback handler.
	 * @param port The port to start the callback handler web service on.
	 */
	public static void handleCallbacks(Pipeline2WSCallbackHandler callbackHandler, int port) {
		if (callbackHandler == null)
			return;
		
		Pipeline2WS.callbackHandler = callbackHandler;
		logger.debug("set callback handler implementation to "+(callbackHandler==null?null:callbackHandler.getClass().getCanonicalName()));
		
		// Use default callback server implementation if nothing else is set
		if (callbackServer == null)
			setHttpCallbackServerImplementation(new DP2HttpCallbackServerImpl());
		
		// Initialize callback server on the provided port
		try {
			logger.debug("initializing callback server on port "+port);
        	callbackServer.init(port);
			
		} catch (Exception e) {
			logger.error("Could not start the component");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.error(sw.toString());
		}
	}
	
	/**
	 * Stop handling callbacks. Shuts down the web service that handles callbacks.
	 * @throws Exception 
	 */
	public static void stopCallbacks() throws Exception {
		logger.debug("stopping callbacks");
		Pipeline2WS.callbackServer.close();
	}
	
	/**
	 * Set the logger implementation.
	 * @param logger The logger.
	 */
	public static void setLoggerImplementation(Pipeline2WSLogger loggerImpl) {
		logger = loggerImpl;
		if (logger == null)
			logger = new Pipeline2WSLoggerImpl();
		logger.debug("set logger implementation to "+loggerImpl.getClass().getCanonicalName());
	}
}
