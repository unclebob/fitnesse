// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class Response
{
	public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";

	protected static final String CRLF = "\r\n";

	public static SimpleDateFormat makeStandardHttpDateFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df;
	}

	private int status = 200;
	private HashMap headers = new HashMap(17);
	private String contentType = DEFAULT_CONTENT_TYPE;

	public Response()
	{
	}

	public Response(int status)
	{
		this.status = status;
	}

	public abstract void readyToSend(ResponseSender sender) throws Exception;

	protected abstract void addSpecificHeaders();

	public abstract int getContentSize();

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int s)
	{
		status = s;
	}

	public String makeHttpHeaders()
	{
		StringBuffer text = new StringBuffer();
		text.append("HTTP/1.1 ").append(status).append(" ").append(getReasonPhrase()).append(CRLF);
		makeHeaders(text);
		text.append(CRLF);
		return text.toString();
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String type)
	{
		contentType = type;
	}

	public void redirect(String location)
	{
		status = 303;
		addHeader("Location", location);
	}

	public void setMaxAge(int age)
	{
		addHeader("Cache-Control", "max-age=" + age);
	}

	public void setLastModifiedHeader(String date)
	{
		addHeader("Last-Modified", date);
	}

	public void setExpiresHeader(String date)
	{
		addHeader("Expires", date);
	}

	public void addHeader(String key, String value)
	{
		headers.put(key, value);
	}

	public String getHeader(String key)
	{
		return (String) headers.get(key);
	}

	public byte[] getEncodedBytes(String value) throws Exception
	{
		return value.getBytes("UTF-8");
	}

	private void makeHeaders(StringBuffer text)
	{
		for(Iterator iterator = headers.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String value = (String) headers.get(key);
			text.append(key).append(": ").append(value).append(CRLF);
		}
	}

	protected void addStandardHeaders()
	{
		addHeader("Content-Type", getContentType());
		addSpecificHeaders();
	}

	protected String getReasonPhrase()
	{
		return getReasonPhrase(status);
	}

	public static String getReasonPhrase(int status)
	{
		switch(status)
		{
		case 100:
			return "Continue";
		case 101:
			return "Switching Protocols";
		case 200:
			return "OK";
		case 201:
			return "Created";
		case 202:
			return "Accepted";
		case 203:
			return "Non-Authoritative Information";
		case 204:
			return "No Content";
		case 205:
			return "Reset Content";
		case 300:
			return "Multiple Choices";
		case 301:
			return "Moved Permanently";
		case 302:
			return "Found";
		case 303:
			return "See Other";
		case 304:
			return "Not Modified";
		case 305:
			return "Use Proxy";
		case 307:
			return "Temporary Redirect";
		case 400:
			return "Bad Request";
		case 401:
			return "Unauthorized";
		case 402:
			return "Payment Required";
		case 403:
			return "Forbidden";
		case 404:
			return "Not Found";
		case 405:
			return "Method Not Allowed";
		case 406:
			return "Not Acceptable";
		case 407:
			return "Proxy Authentication Required";
		case 408:
			return "Request Time-out";
		case 409:
			return "Conflict";
		case 410:
			return "Gone";
		case 411:
			return "Length Required";
		case 412:
			return "Precondition Failed";
		case 413:
			return "Request Entity Too Large";
		case 414:
			return "Request-URI Too Large";
		case 415:
			return "Unsupported Media Type";
		case 416:
			return "Requested range not satisfiable";
		case 417:
			return "Expectation Failed";
		case 500:
			return "Internal Server Error";
		case 501:
			return "Not Implemented";
		case 502:
			return "Bad Gateway";
		case 503:
			return "Service Unavailable";
		case 504:
			return "Gateway Time-out";
		case 505:
			return "HTTP Version not supported";
		default:
			return "Unknown Status";
		}
	}
}

