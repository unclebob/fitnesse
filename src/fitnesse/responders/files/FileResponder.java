// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.http.*;
import fitnesse.*;
import fitnesse.responders.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.ParseException;

public class FileResponder implements Responder
{
	private static FileNameMap fileNameMap = URLConnection.getFileNameMap();
	public String resource;
	public File requestedFile;
	public Date lastModifiedDate;
	public String lastModifiedDateString;

	public static Responder makeResponder(Request request, String rootPath) throws Exception
	{
		String resource = request.getResource();

		if(fileNameHasSpaces(resource))
			resource = restoreRealSpacesInFileName(resource);

		File requestedFile = new File(rootPath + "/" + resource);
		if(!requestedFile.exists())
			return new NotFoundResponder();

		if(requestedFile.isDirectory())
			return new DirectoryResponder(resource, requestedFile);
		else
			return new FileResponder(resource, requestedFile);
	}

	public FileResponder(String resource, File requestedFile)
	{
		this.resource = resource;
		this.requestedFile = requestedFile;
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		InputStreamResponse response = new InputStreamResponse();
		determineLastModifiedInfo();

		if(isNotModified(request))
			return createNotModifiedResponse();
		else
		{
			response.setBody(requestedFile);
			setContentType(requestedFile, response);
			response.setLastModifiedHeader(lastModifiedDateString);
		}
		return response;
	}

	public static boolean fileNameHasSpaces(String resource)
	{
		return resource.indexOf("%20") != 0;
	}

	public static String restoreRealSpacesInFileName(String resource) throws Exception
	{
		return URLDecoder.decode(resource, "UTF-8");
	}

	String getResource()
	{
		return resource;
	}

	private boolean isNotModified(Request request)
	{
		if(request.hasHeader("If-Modified-Since"))
		{
			String queryDateString = (String) request.getHeader("If-Modified-Since");
			try
			{
				Date queryDate = SimpleResponse.makeStandardHttpDateFormat().parse(queryDateString);
				if(!queryDate.before(lastModifiedDate))
					return true;
			}
			catch(ParseException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	private Response createNotModifiedResponse()
	{
		Response response = new SimpleResponse();
		response.setStatus(304);
		response.addHeader("Date", SimpleResponse.makeStandardHttpDateFormat().format(new Date()));
		response.addHeader("Cache-Control", "private");
		response.setLastModifiedHeader(lastModifiedDateString);
		return response;
	}

	private void determineLastModifiedInfo()
	{
		lastModifiedDate = new Date(requestedFile.lastModified());
		lastModifiedDateString = SimpleResponse.makeStandardHttpDateFormat().format(lastModifiedDate);

		try  // remove milliseconds
		{
			lastModifiedDate = SimpleResponse.makeStandardHttpDateFormat().parse(lastModifiedDateString);
		}
		catch(java.text.ParseException jtpe)
		{
			jtpe.printStackTrace();
		}
	}

	private void setContentType(File file, Response response)
	{
		String contentType = fileNameMap.getContentTypeFor(file.getName());
		if(contentType == null)
		{
			if(file.getName().endsWith(".css"))
				contentType = "text/css";
			else
				contentType = "text/plain";
		}
		response.setContentType(contentType);
	}
}
