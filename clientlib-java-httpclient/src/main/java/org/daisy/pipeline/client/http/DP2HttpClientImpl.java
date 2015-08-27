package org.daisy.pipeline.client.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.w3c.dom.Document;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XML;

/** Implementation of DP2HttpClient that uses Apache HTTP Client as the underlying HTTP client. */
public class DP2HttpClientImpl implements DP2HttpClient {
	
	public Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		return getDelete("GET", endpoint, path, username, secret, parameters);
	}
	
	public Pipeline2WSResponse delete(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		return getDelete("DELETE", endpoint, path, username, secret, parameters);
	}
	
	private Pipeline2WSResponse getDelete(String method, String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		String url = Pipeline2Client.url(endpoint, path, username, secret, parameters);
		if (endpoint == null) {
			return new Pipeline2WSResponse(url, 503, "Endpoint is not set", "Please provide a Pipeline 2 endpoint.", null, null, null);
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpRequestBase http;
		
		if ("DELETE".equals(method)) {
			http = new HttpDelete(url);
		} else { // "GET"
			http = new HttpGet(url);
		}
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(http);
		} catch (ClientProtocolException e) {
			throw new Pipeline2Exception("Error while "+method+"ing.", e);
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while "+method+"ing.", e);
		}
		HttpEntity resEntity = response.getEntity();
		
		InputStream bodyStream = null;
		if (resEntity != null) {
			try {
				bodyStream = resEntity.getContent();
			} catch (IOException e) {
				throw new Pipeline2Exception("Error while reading response body", e);
			}
		}
		
		int status = response.getStatusLine() != null ? response.getStatusLine().getStatusCode() : 204; // Not sure if it's ok to default to 204, but let's try!
		String statusName = response.getStatusLine() != null ? response.getStatusLine().getReasonPhrase() : "";
		String statusDescription = null;
		String contentType = response.getFirstHeader("Content-Type") != null ? response.getFirstHeader("Content-Type").getValue() : "application/octet-stream";
		Long size = (resEntity != null && resEntity.getContentLength() >= 0) ? resEntity.getContentLength() : null;
		
		return new Pipeline2WSResponse(url, status, statusName, statusDescription, contentType, size, bodyStream);
	}
	
	
	public Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2Exception {
		String url = Pipeline2Client.url(endpoint, path, username, secret, null);
		
		if (Pipeline2Client.logger().logsLevel(Pipeline2Logger.LEVEL.DEBUG)) {
			Pipeline2Client.logger().debug("URL: ["+url+"]");
			Pipeline2Client.logger().debug(XML.toString(xml));
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
		StringEntity entity;
		try {
			entity = new StringEntity(XML.toString(xml), "application/xml", HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new Pipeline2Exception("Error while serializing XML for POSTing.", e);
		}
		
		httppost.setEntity(entity);
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		}
		HttpEntity resEntity = response.getEntity();
		
		InputStream bodyStream = null;
		try {
			bodyStream = resEntity.getContent();
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while reading response body", e); 
		}
		
		return new Pipeline2WSResponse(url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), null,
				response.getFirstHeader("Content-Type").getValue(),
				resEntity.getContentLength()>=0?resEntity.getContentLength():null,
				bodyStream);
	}
	
	
	public Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2Exception {
		String url = Pipeline2Client.url(endpoint, path, username, secret, null);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
		MultipartEntity reqEntity = new MultipartEntity();
		for (String partName : parts.keySet()) { 
			reqEntity.addPart(partName, new FileBody(parts.get(partName)));
		}
		httppost.setEntity(reqEntity);
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		}
		HttpEntity resEntity = response.getEntity();
		
		InputStream bodyStream = null;
		try {
			bodyStream = resEntity.getContent();
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while reading response body", e); 
		}
		
		return new Pipeline2WSResponse(url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), null,
				response.getFirstHeader("Content-Type").getValue(),
				resEntity.getContentLength()>=0?resEntity.getContentLength():null,
				bodyStream);
	}
	
}