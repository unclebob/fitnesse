// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.components.LogData;
import fitnesse.http.HttpException;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.socketservice.SocketFactory;
import org.apache.commons.lang.StringUtils;
import fitnesse.util.Clock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FitNesseExpediter implements ResponseSender {
  private static final Logger LOG = Logger.getLogger(FitNesseExpediter.class.getName());

  private final Socket socket;
  private final InputStream input;
  private final OutputStream output;
  private Request request;
  private Response response;
  private final FitNesseContext context;
  protected long requestParsingTimeLimit;
  private long requestProgress;
  private long requestParsingDeadline;
  private volatile boolean hasError;

  public FitNesseExpediter(Socket s, FitNesseContext context) throws IOException {
    this.context = context;
    socket = s;
    input = s.getInputStream();
    output = s.getOutputStream();
    requestParsingTimeLimit = 10000;
  }

  public void start() {
    try {
      Request request = makeRequest();
      makeResponse(request);
      sendResponse();
    }
    catch (SocketException se) {
      // can be thrown by makeResponse or sendResponse.
    }
    catch (Throwable e) {
      LOG.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  public void setRequestParsingTimeLimit(long t) {
    requestParsingTimeLimit = t;
  }

  public long getRequestParsingTimeLimit() {
    return requestParsingTimeLimit;
  }

  public void send(byte[] bytes) {
    try {
      output.write(bytes);
      output.flush();
    }
    catch (IOException e) {
      LOG.log(Level.INFO, "Output stream closed unexpectedly (Stop button pressed?)", e);
    }
  }

  public void close() {
    try {
      log(socket, request, response);
      socket.close();
    }
    catch (IOException e) {
      LOG.log(Level.WARNING, "Error while closing socket", e);
    }
  }

  public Socket getSocket() {
    return socket;
  }

  public Request makeRequest() {
    request = new Request(input);
    request.setPeerDn(SocketFactory.peerDn(socket));
    request.setContextRoot(context.contextRoot);
    return request;
  }

  public void sendResponse() throws IOException {
    response.sendTo(this);
  }

  private Response makeResponse(Request request) throws SocketException {
    try {
      Thread parseThread = createParsingThread(request);
      parseThread.start();

      waitForRequest(request);
      if (!hasError) {
        if (context.contextRoot.equals(request.getRequestUri() + "/")) {
          response = new SimpleResponse();
          response.redirect(context.contextRoot, "");
        } else {
          response = createGoodResponse(request);
        }
      }
    }
    catch (SocketException se) {
      throw se;
    }
    catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to handle request", e);
      response = new ErrorResponder(e).makeResponse(context, request);
    }
    // Add those as default headers?
    response.addHeader("Server", "FitNesse-" + context.version);
    response.addHeader("Connection", "close");
    return response;
  }

  public Response createGoodResponse(Request request) throws Exception {
    if (StringUtils.isBlank(request.getResource()) && StringUtils.isBlank(request.getQueryString()))
      request.setResource("FrontPage");
    Responder responder = context.responderFactory.makeResponder(request);
    responder = context.authenticator.authenticate(context, request, responder);
    return responder.makeResponse(context, request);
  }

  private void waitForRequest(Request request) throws InterruptedException {
    long now = Clock.currentTimeInMillis();
    requestParsingDeadline = now + requestParsingTimeLimit;
    requestProgress = 0;
    while (!hasError && !request.hasBeenParsed()) {
      Thread.sleep(10);
      if (timeIsUp() && parsingIsUnproductive(request))
        reportError(408, "The client request has been unproductive for too long. It has timed out and will now longer be processed.");
    }
  }

  private boolean parsingIsUnproductive(Request request) {
    long updatedRequestProgress = request.numberOfBytesParsed();
    if (updatedRequestProgress > requestProgress) {
      requestProgress = updatedRequestProgress;
      return false;
    } else
      return true;
  }

  private boolean timeIsUp() {
    long now = Clock.currentTimeInMillis();
    if (now > requestParsingDeadline) {
      requestParsingDeadline = now + requestParsingTimeLimit;
      return true;
    } else
      return false;
  }

  private Thread createParsingThread(final Request request) {
    Thread parseThread = new Thread() {
      public synchronized void run() {
        try {
          request.parse();
        }
        catch (HttpException e) {
          reportError(400, e.getMessage());
        }
        catch (Exception e) {
          reportError(e);
        }
      }
    };
    return parseThread;
  }

  private void reportError(int status, String message) {
    try {
      response = new ErrorResponder(message).makeResponse(context, request);
      response.setStatus(status);
      hasError = true;
    }
    catch (Exception e) {
      LOG.log(Level.WARNING, "Can not report error (status = " + status + ", message = " + message + ")", e);
    }
  }

  private void reportError(Exception e) {
    response = new ErrorResponder(e).makeResponse(context, request);
    hasError = true;
  }

  public static LogData makeLogData(Socket socket, Request request, Response response) {
    LogData data = new LogData(
        ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
        new GregorianCalendar(),
        request.getRequestLine(),
        response.getStatus(),
        response.getContentSize(),
        request.getAuthorizationUsername());

    return data;
  }

  public void log(Socket s, Request request, Response response) {
    if (context.logger != null)
      context.logger.log(makeLogData(s, request, response));
  }
}
