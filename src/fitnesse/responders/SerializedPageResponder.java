// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.components.XmlWriter;
import fitnesse.authentication.*;
import fitnesse.wiki.*;
import fitnesse.http.*;

import java.io.*;
import org.w3c.dom.Document;

public class SerializedPageResponder implements SecureResponder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
Thread.sleep(1000);
    WikiPage page = getRequestedPage(request, context);
    if(page == null)
			return new NotFoundResponder().makeResponse(context, request);

		if("pages".equals(request.getInput("type")))
		{
			Document doc = new PageXmlizer().xmlize(page);
			SimpleResponse response = makeResponseWithxml(doc);
			return response;
		}
		else if("data".equals(request.getInput("type")))
		{
			Document doc = new PageXmlizer().xmlize(page.getData());
			SimpleResponse response = makeResponseWithxml(doc);
			return response;
		}
		else
		{
			Object object = getObjectToSerialize(request, page);
			byte[] bytes = serializeToBytes(object);
      return responseWith(bytes);
		}
	}

	private SimpleResponse makeResponseWithxml(Document doc) throws Exception
	{
		//TODO MdM Shoudl probably use a StreamedResponse
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		XmlWriter xmlWriter = new XmlWriter(output);
		xmlWriter.write(doc);
		xmlWriter.flush();
		xmlWriter.close();
		SimpleResponse response = new SimpleResponse();
		response.setContentType("text/xml");
		response.setContent(output.toByteArray());
		return response;
	}

	private Object getObjectToSerialize(Request request, WikiPage page) throws Exception
  {
    Object object;
    if("bones".equals(request.getInput("type")))
      object = new ProxyPage(page);
    else if("meat".equals(request.getInput("type")))
    {
      PageData originalData = page.getData();
      if(request.hasInput("version"))
        originalData = page.getDataVersion((String) request.getInput("version"));
      PageData data = new PageData(originalData);

      object = data;
    }
    else
      throw new Exception("Improper use of proxy retrieval");
    return object;
  }

  private WikiPage getRequestedPage(Request request, FitNesseContext context) throws Exception
  {
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.root.getPageCrawler().getPage(context.root, path);
    return page;
  }

  private SimpleResponse responseWith(byte[] bytes)
  {
    SimpleResponse response = new SimpleResponse();
    response.setContentType("application/octet-stream");
    response.setContent(bytes);
    return response;
  }

  private byte[] serializeToBytes(Object object) throws IOException
  {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(byteStream);
    os.writeObject(object);
    os.close();
    byte[] bytes = byteStream.toByteArray();
    return bytes;
  }

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}

}
