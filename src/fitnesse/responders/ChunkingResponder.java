// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.ChunkedDataProvider;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.util.Clock;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ChunkingResponder implements Responder, ChunkedDataProvider {
  private static final Logger LOG = Logger.getLogger(ChunkingResponder.class.getName());

  protected WikiPage root;
  public WikiPage page;
  protected WikiPagePath path;
  protected Request request;
  protected ChunkedResponse response;
  protected FitNesseContext context;
  private boolean dontChunk = false;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;
    this.request = request;
    this.root = context.getRootPage(request.getMap());
    String format = request.getInput("format");
    response = new ChunkedResponse(format, this);

    if (dontChunk || request.hasInput(Request.NOCHUNK))
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
    page = getPageCrawler().getPage(path);
  }

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  private Response pageNotFoundResponse(FitNesseContext context, Request request) throws Exception {
    return new NotFoundResponder().makeResponse(context, request);
  }

  protected boolean shouldRespondWith404() {
    return true;
  }

  @Override
  public void startSending() throws IOException {
    try {
      doSending();
    }
    catch (SocketException e) {
      LOG.log(Level.WARNING, "Socket Exception at: " + Clock.currentTimeInMillis(), e);
      // normal. someone stopped the request.
    }
    catch (Exception e) {
      addExceptionAndCloseResponse(e);
    }
  }

  private void addExceptionAndCloseResponse(Exception e) throws IOException {
    response.add(ErrorResponder.makeExceptionString(e));
    response.close();
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
   * @throws Exception exception thrown
   */
  protected abstract void doSending() throws Exception;
}
