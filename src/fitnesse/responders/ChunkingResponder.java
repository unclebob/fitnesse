// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.net.SocketException;

public abstract class ChunkingResponder implements Responder {
  protected WikiPage root;
  public WikiPage page;
  protected WikiPagePath path;
  protected Request request;
  protected ChunkedResponse response;
  protected FitNesseContext context;
  private boolean dontChunk = false;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;
    this.request = request;
    this.root = context.root;
    String format = (String) request.getInput("format");
    response = new ChunkedResponse(format);
    if (dontChunk || context.doNotChunk || request.hasInput("nochunk"))
      response.turnOffChunking();
    getRequestedPage(request);
    if (page == null && shouldRespondWith404())
      return pageNotFoundResponse(context, request);

    Thread respondingThread = new Thread(new RespondingRunnable(), getClass() + ": Responding Thread");
    respondingThread.start();

    return response;
  }

  public void turnOffChunking() {
    dontChunk = true;
  }

  private void getRequestedPage(Request request) throws Exception {
    path = PathParser.parse(request.getResource());
    page = getPageCrawler().getPage(root, path);
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

  private void startSending() {
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

  protected class RespondingRunnable implements Runnable {
    public void run() {
      response.waitForReadyToSend();
      startSending();

    }
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  protected abstract void doSending() throws Exception;
}
