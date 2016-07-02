// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.GregorianCalendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.components.LogData;
import fitnesse.http.HttpException;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import org.apache.commons.lang.StringUtils;

public class FitNesseExpediter implements ResponseSender, Runnable {
  private static final Logger LOG = Logger.getLogger(FitNesseExpediter.class.getName());

  private final Socket socket;
  private final InputStream input;
  private final OutputStream output;
  private final FitNesseContext context;
  private final ExecutorService executorService;
  private final long requestParsingTimeLimit;
  private Request request;
  private Response response;

  public FitNesseExpediter(Socket socket, FitNesseContext context, ExecutorService executorService) throws IOException {
    this(socket, context, executorService, 10000);
  }

  public FitNesseExpediter(Socket socket, FitNesseContext context, ExecutorService executorService, long requestParsingTimeLimit) throws IOException {
    this.context = context;
    this.socket = socket;
    this.executorService = executorService;
    input = socket.getInputStream();
    output = socket.getOutputStream();
    this.requestParsingTimeLimit = requestParsingTimeLimit;
  }

  @Override
  public void run() {
    try {
      // Storing them in instance fields, since we need info for logging when the connection is closed.
      request = makeRequest();
      response = makeResponse(request);
      sendResponse(response);
    } catch (SocketException se) {
      // can be thrown by makeResponse or sendResponse.
    } catch (Throwable e) { // NOSONAR
      // This catch is intentional, since it's the last point where we can catch exceptions that occur in this thread.
      LOG.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  @Override
  public void send(byte[] bytes) throws IOException {
    output.write(bytes);
    output.flush();
  }

  @Override
  public void close() {
    log(socket, request, response);
    if (!socket.isClosed()) {
      try {
        socket.close();
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Error while closing socket", e);
      }
    }
  }

  private Request makeRequest() {
    Request request = new Request(input);
    request.setContextRoot(context.contextRoot);
    return request;
  }

  private void sendResponse(Response response) throws IOException {
    response.sendTo(this);
  }

  private Response makeResponse(final Request request) throws Exception {
    Response response;
    try {
      executorService.submit(new Callable<Request>() {
        @Override
        public Request call() throws Exception {
          request.parse();
          return request;
        }
      }).get(requestParsingTimeLimit, TimeUnit.MILLISECONDS);

      if (request.hasBeenParsed()) {
        if (context.contextRoot.equals(request.getRequestUri() + "/")) {
          response = new SimpleResponse();
          response.redirect(context.contextRoot, "");
        } else {
          response = createGoodResponse(request);
        }
      } else {
        response = reportError(request, 400, "The request could not be parsed.");
      }
    } catch (SocketException se) {
      throw se;
    } catch (TimeoutException e) {
      String message = "The client request has been unproductive for too long. It has timed out and will no longer be processed.";
      LOG.log(Level.FINE, message, e);
      response = reportError(request, 408, message);
    } catch (HttpException e) {
      LOG.log(Level.FINE, "An error occured while fulfilling user request", e);
      response = reportError(request, 400, e.getMessage());
    } catch (Exception e) {
      LOG.log(Level.WARNING, "An error occured while fulfilling user request", e);
      response = reportError(request, e);
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

  private Response reportError(Request request, int status, String message) throws Exception {
    return new ErrorResponder(message, status).makeResponse(context, request);
  }

  private Response reportError(Request request, Exception e) throws Exception {
    return new ErrorResponder(e).makeResponse(context, request);
  }

  public static LogData makeLogData(Socket socket, Request request, Response response) {
    return new LogData(
        ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
        new GregorianCalendar(),
        request.getRequestLine(),
        response.getStatus(),
        response.getContentSize(),
        request.getAuthorizationUsername());
  }

  public void log(Socket s, Request request, Response response) {
    if (context.logger != null)
      context.logger.log(makeLogData(s, request, response));
  }
}
