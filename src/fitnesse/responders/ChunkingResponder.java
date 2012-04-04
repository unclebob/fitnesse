// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.ChunkedDataProvider;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.net.SocketException;

public abstract class ChunkingResponder implements Responder, ChunkedDataProvider {
  protected WikiPage root;
  public WikiPage page;
  protected WikiPagePath path;
  protected Request request;
  protected ChunkedResponse response;
  protected FitNesseContext context;
  private boolean dontChunk = false;

  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;
    this.request = request;
    this.root = context.root;
    String format = (String) request.getInput("format");
    response = new ChunkedResponse(format, this);
    
    if (dontChunk || context.doNotChunk || request.hasInput("nochunk"))
      response.turnOffChunking();
    getRequestedPage(request);
    if (page == null && shouldRespondWith404())
      return pageNotFoundResponse(context, request);

    return response;
  }

  public void turnOffChunking() {
    dontChunk = true;
  }

  private void getRequestedPage(Request request) {
    path = PathParser.parse(request.getResource());
    page = getPageCrawler().getPage(root, path);
  }

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  private Response pageNotFoundResponse(FitNesseContext context, Request request) {
    return new NotFoundResponder().makeResponse(context, request);
  }

  protected boolean shouldRespondWith404() {
    return true;
  }

  public void startSending() {
    try {
      doSending();
    }
    catch (SocketException e) {
      System.out.println("Socket Exception at: " + System.currentTimeMillis());
      e.printStackTrace();
      // normal. someone stopped the request.
    }
    catch (Exception e) {
      addExceptionAndCloseResponse(e);
    }
  }

  private void addExceptionAndCloseResponse(Exception e) {
    try {
      response.add(ErrorResponder.makeExceptionString(e));
      response.closeAll();
    }
    catch (Exception e1) {
    }
  }

  protected String getRenderedPath() {
    if (path != null)
      return PathParser.render(path);
    else
      return request.getResource();
  }


  public void setRequest(Request request) {
    this.request = request;
  }

  /**
   * Performs the actual chunk sending in a separate thread.
   * 
   * @throws Exception
   */
  protected abstract void doSending() throws Exception;
}
