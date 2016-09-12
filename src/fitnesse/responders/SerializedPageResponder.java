// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.fs.PageXmlizer;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.XmlizePageCondition;
import org.w3c.dom.Document;

public class SerializedPageResponder implements SecureResponder {
  private XmlizePageCondition xmlizePageCondition = new XmlizePageCondition() {
    @Override
    public boolean canBeXmlized(WikiPage page) {
      return !(page instanceof SymbolicPage);
    }
  };

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage page = getRequestedPage(request, context);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    if ("pages".equals(request.getInput("type"))) {
      PageXmlizer pageXmlizer = new PageXmlizer();
      pageXmlizer.addPageCondition(xmlizePageCondition);
      Document doc = pageXmlizer.xmlize(page);
      SimpleResponse response = makeResponseWithxml(doc);
      return response;
    } else if ("data".equals(request.getInput("type"))) {
      Document doc = new PageXmlizer().xmlize(page.getData());
      SimpleResponse response = makeResponseWithxml(doc);
      return response;
    } else {
      Object object = getObjectToSerialize(request, page);
      byte[] bytes = serializeToBytes(object);
      return responseWith(bytes);
    }
  }

  private SimpleResponse makeResponseWithxml(Document doc) throws IOException {
    //TODO MdM Shoudl probably use a StreamedResponse
    String output = XmlUtil.xmlAsString(doc);
    SimpleResponse response = new SimpleResponse();
    response.setContentType("text/xml");
    response.setContent(output);
    return response;
  }

  private Object getObjectToSerialize(Request request, WikiPage page) {
    Object object;
    if ("versions".equals(request.getInput("type"))) {
      object = page.getVersions();
    } else if ("meat".equals(request.getInput("type"))) {
      WikiPage originalPage = page;
      if (request.hasInput("version"))
        originalPage = page.getVersion(request.getInput("version"));
      object = originalPage.getData();
    } else
      throw new IllegalArgumentException("Improper use of proxy retrieval. 'type' should be one of 'versions', 'meat'.");
    return object;
  }

  private WikiPage getRequestedPage(Request request, FitNesseContext context) {
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.getRootPage().getPageCrawler().getPage(path);
    return page;
  }

  private SimpleResponse responseWith(byte[] bytes) {
    SimpleResponse response = new SimpleResponse();
    response.setContentType("application/octet-stream");
    response.setContent(bytes);
    return response;
  }

  private byte[] serializeToBytes(Object object) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(byteStream);
    os.writeObject(object);
    os.close();
    byte[] bytes = byteStream.toByteArray();
    return bytes;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

}
