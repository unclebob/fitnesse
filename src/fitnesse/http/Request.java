// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import fitnesse.components.Base64;
import fitnesse.util.StreamReader;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.*;

public class Request
{
	private static final Pattern requestLinePattern = Pattern.compile("(\\p{Upper}+?) ([^\\s]+)");
	private static final Pattern requestUriPattern = Pattern.compile("([^?]+)\\??(.*)");
	private static final Pattern queryStringPattern = Pattern.compile("([^=]*)=?([^&]*)&?");
	private static final Pattern headerPattern = Pattern.compile("([^:]*): (.*)");
	private static final Pattern boundaryPattern = Pattern.compile("boundary=(.*)");
	private static final Pattern multipartHeaderPattern = Pattern.compile("([^ =]+)=\\\"([^\"]*)\\\"");

	private static Collection allowedMethods = buildAllowedMethodList();

	protected StreamReader input;
	protected String requestURI;
	protected String resource;
	protected String queryString;
	protected HashMap inputs = new HashMap();
	protected HashMap headers = new HashMap();
	protected String entityBody = "";
	protected String requestLine;
	protected String authorizationUsername;
	protected String authorizationPassword;
	private boolean hasBeenParsed;
	private long bytesParsed = 0;

	public static Set buildAllowedMethodList()
	{
		Set methods = new HashSet(20);
		methods.add("GET");
		methods.add("POST");
		return methods;
	}

	protected Request()
	{
	}

	public Request(InputStream input) throws Exception
	{
		this.input = new StreamReader(new BufferedInputStream(input));
	}

	public void parse() throws Exception
	{
		readAndParseRequestLine();
		headers = parseHeaders(input);
		parseEntityBody();
		hasBeenParsed = true;
	}

	private void readAndParseRequestLine() throws Exception
	{
		requestLine = input.readLine();
		Matcher match = requestLinePattern.matcher(requestLine);
		checkRequestLine(match);
		requestURI = match.group(2);
		parseRequestUri(requestURI);
	}

	private HashMap parseHeaders(StreamReader reader) throws Exception
	{
		HashMap headers = new HashMap();
		String line = reader.readLine();
		while(!"".equals(line))
		{
			Matcher match = headerPattern.matcher(line);
			if(match.find())
			{
				String key = match.group(1);
				String value = match.group(2);
				headers.put(key.toLowerCase(), value);
			}
			line = reader.readLine();
		}
		return headers;
	}

	private void parseEntityBody() throws Exception
	{
		if(hasHeader("Content-Length"))
		{
			String contentType = (String) getHeader("Content-Type");
			if(contentType != null && contentType.startsWith("multipart/form-data"))
			{
				Matcher match = boundaryPattern.matcher(contentType);
				match.find();
				parseMultiPartContent(match.group(1));
			}
			else
			{
				entityBody = input.read(getContentLength());
				parseQueryString(entityBody);
			}
		}
	}

	public int getContentLength()
	{
		return Integer.parseInt((String) getHeader("Content-Length"));
	}

	private void parseMultiPartContent(String boundary) throws Exception
	{
		boundary = "--" + boundary;

		int numberOfBytesToRead = getContentLength();
		accumulateBytesReadAndReset();
		input.readUpTo(boundary);
		while(numberOfBytesToRead - input.numberOfBytesConsumed() > 10)
		{
			input.readLine();
			HashMap headers = parseHeaders(input);
			String contentDisposition = (String) headers.get("content-disposition");
			Matcher matcher = multipartHeaderPattern.matcher(contentDisposition);
			while(matcher.find())
				headers.put(matcher.group(1), matcher.group(2));

			String name = (String) headers.get("name");
			Object value;
			if(headers.containsKey("filename"))
				value = createUploadedFile(headers, input, boundary);
			else
				value = input.readUpTo("\r\n" + boundary);

			inputs.put(name, value);
		}
	}

	private void accumulateBytesReadAndReset()
	{
		bytesParsed += input.numberOfBytesConsumed();
		input.resetNumberOfBytesConsumed();
	}

	private Object createUploadedFile(HashMap headers, StreamReader reader, String boundary) throws Exception
	{
		String filename = (String) headers.get("filename");
		String contentType = (String) headers.get("content-type");
		File tempFile = File.createTempFile("FitNesse", ".uploadedFile");
		OutputStream output = new BufferedOutputStream(new FileOutputStream(tempFile));
		reader.copyBytesUpTo("\r\n" + boundary, output);
		output.close();
		return new UploadedFile(filename, contentType, tempFile);
	}

	private void checkRequestLine(Matcher match) throws HttpException
	{
		if(!match.find())
			throw new HttpException("The request string is malformed and can not be parsed");
		if(!allowedMethods.contains(match.group(1)))
			throw new HttpException("The " + match.group(1) + " method is not currently supported");
	}

	public void parseRequestUri(String requestUri)
	{
		Matcher match = requestUriPattern.matcher(requestUri);
		match.find();
		resource = stripLeadingSlash(match.group(1));
		queryString = match.group(2);
		parseQueryString(queryString);
	}

	protected void parseQueryString(String queryString)
	{
		Matcher match = queryStringPattern.matcher(queryString);
		while(match.find())
		{
			String key = match.group(1);
			String value = decodeContent(match.group(2));
			inputs.put(key, value);
		}
	}

	public String getRequestLine()
	{
		return requestLine;
	}

	public String getRequestUri()
	{
		return requestURI;
	}

	public String getResource()
	{
		return resource;
	}

	public String getQueryString()
	{
		return queryString;
	}

	public boolean hasInput(String key)
	{
		return inputs.containsKey(key);
	}

	public Object getInput(String key)
	{
		return inputs.get(key);
	}

	public boolean hasHeader(String key)
	{
		return headers.containsKey(key.toLowerCase());
	}

	public Object getHeader(String key)
	{
		return headers.get(key.toLowerCase());
	}

	public String getBody()
	{
		return entityBody;
	}

	private String stripLeadingSlash(String url)
	{
		return url.substring(1);
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("--- Request Start ---").append("\n");
		buffer.append("Request URI:  ").append(requestURI).append("\n");
		buffer.append("Resource:     ").append(resource).append("\n");
		buffer.append("Query String: ").append(queryString).append("\n");
		buffer.append("Hearders: (" + headers.size() + ")\n");
		addMap(headers, buffer);
		buffer.append("Form Inputs: (" + inputs.size() + ")\n");
		addMap(inputs, buffer);
		buffer.append("Entity Body: ").append("\n");
		buffer.append(entityBody).append("\n");
		buffer.append("--- End Request ---\n");

		return buffer.toString();
	}

	private void addMap(HashMap map, StringBuffer buffer)
	{
		if(map.size() == 0)
		{
			buffer.append("\tempty");
		}
		for(Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String value = map.get(key) != null ? escape(map.get(key).toString()) : null;
			buffer.append("\t" + escape(key) + " \t-->\t " + value + "\n");
		}
	}

	private String escape(String foo)
	{
		return foo.replaceAll("[\n\r]+", "|");
	}

	public static String decodeContent(String content)
	{
		String escapedContent = null;
		try
		{
			escapedContent = URLDecoder.decode(content, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			escapedContent = "URLDecoder Error";
		}
		return escapedContent;
	}

	public boolean hasBeenParsed()
	{
		return hasBeenParsed;
	}

	public String getUserpass(String headerValue) throws Exception
	{
		String encodedUserpass = headerValue.substring(6);
		return Base64.decode(encodedUserpass);
	}

	public void getCredentials() throws Exception
	{
		if(hasHeader("Authorization"))
		{
			String authHeader = getHeader("Authorization").toString();
			String userpass = getUserpass(authHeader);
			String[] values = userpass.split(":");
			if(values.length == 2)
			{
				authorizationUsername = values[0];
				authorizationPassword = values[1];
			}
		}
	}

	public String getAuthorizationUsername()
	{
		return authorizationUsername;
	}

	public String getAuthorizationPassword()
	{
		return authorizationPassword;
	}

	public long numberOfBytesParsed()
	{
		return bytesParsed + input.numberOfBytesConsumed();
	}
}
