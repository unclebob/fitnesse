// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

public abstract class Response {

	public enum Format {
		XML, HTML
	}

	public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";

	protected static final String CRLF = "\r\n";
	private Format format;

	public static SimpleDateFormat makeStandardHttpDateFormat() {
		// SimpleDateFormat is not thread safe, so we need to create each
		// instance independently.
		SimpleDateFormat df = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df;
	}

	private int status = 200;
	private HashMap<String, String> headers = new HashMap<String, String>(17);
	private String contentType = DEFAULT_CONTENT_TYPE;

	public Response(String formatString) {
		if (formatString == null)
			formatString = "html";
		if (formatString.equalsIgnoreCase("html")) {
			format = Format.HTML;
			setContentType(DEFAULT_CONTENT_TYPE);
		} else if (formatString.equalsIgnoreCase("xml")) {
			format = Format.XML;
			setContentType("text/xml");
		}
	}

	public Response(String format, int status) {
		this(format);
		this.status = status;
	}

	public boolean isXmlFormat() {
		return format == Format.XML;
	}

	public boolean isHtmlFormat() {
		return format == Format.HTML;
	}

	public abstract void readyToSend(ResponseSender sender) throws Exception;

	protected abstract void addSpecificHeaders();

	public abstract int getContentSize();

	public int getStatus() {
		return status;
	}

	public void setStatus(int s) {
		status = s;
	}

	public String makeHttpHeaders() {
		StringBuffer text = new StringBuffer();
		text.append("HTTP/1.1 ").append(status).append(" ").append(
				getReasonPhrase()).append(CRLF);
		makeHeaders(text);
		text.append(CRLF);
		return text.toString();
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String type) {
		contentType = type;
	}

	public void redirect(String location) {
		status = 303;
		addHeader("Location", location);
	}

	public void setMaxAge(int age) {
		addHeader("Cache-Control", "max-age=" + age);
	}

	public void setLastModifiedHeader(String date) {
		addHeader("Last-Modified", date);
	}

	public void setExpiresHeader(String date) {
		addHeader("Expires", date);
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public byte[] getEncodedBytes(String value) throws Exception {
		return value.getBytes("UTF-8");
	}

	private void makeHeaders(StringBuffer text) {
		for (Iterator<String> iterator = headers.keySet().iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			String value = headers.get(key);
			text.append(key).append(": ").append(value).append(CRLF);
		}
	}

	protected void addStandardHeaders() {
		addHeader("Content-Type", getContentType());
		addSpecificHeaders();
	}

	protected String getReasonPhrase() {
		return getReasonPhrase(status);
	}

	private static Map<Integer, String> reasonCodes = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(100, "Continue");
			put(101, "Switching Protocols");
			put(200, "OK");
			put(201, "Created");
			put(202, "Accepted");
			put(203, "Non-Authoritative Information");
			put(204, "No Content");
			put(205, "Reset Content");
			put(300, "Multiple Choices");
			put(301, "Moved Permanently");
			put(302, "Found");
			put(303, "See Other");
			put(304, "Not Modified");
			put(305, "Use Proxy");
			put(307, "Temporary Redirect");
			put(400, "Bad Request");
			put(401, "Unauthorized");
			put(402, "Payment Required");
			put(403, "Forbidden");
			put(404, "Not Found");
			put(405, "Method Not Allowed");
			put(406, "Not Acceptable");
			put(407, "Proxy Authentication Required");
			put(408, "Request Time-out");
			put(409, "Conflict");
			put(410, "Gone");
			put(411, "Length Required");
			put(412, "Precondition Failed");
			put(413, "Request Entity Too Large");
			put(414, "Request-URI Too Large");
			put(415, "Unsupported Media Type");
			put(416, "Requested range not satisfiable");
			put(417, "Expectation Failed");
			put(500, "Internal Server Error");
			put(501, "Not Implemented");
			put(502, "Bad Gateway");
			put(503, "Service Unavailable");
			put(504, "Gateway Time-out");
			put(505, "HTTP Version not supported");
		}
	};

	public static String getReasonPhrase(int status) {
		String reasonPhrase = reasonCodes.get(status);
		return reasonPhrase == null ? "Unknown Status" : reasonPhrase;
	}
}
