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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

public class FitNesseExpediter implements ResponseSender {
  private static final Logger LOG = Logger.getLogger(FitNesseExpediter.class.getName());

  private final Socket socket;
  private final InputStream input;
  private final OutputStream output;
  private Request request;
  private Response response;
  private final FitNesseContext context;
  protected long requestParsingTimeLimit;
  private final ExecutorService executorService = new ForkJoinPool(1);

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
    catch (Throwable e) { // NOSONAR
      // This catch is intentional, since it's the last point where we can catch exceptions that occur in this thread.
      LOG.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  public void setRequestParsingTimeLimit(long t) {
    requestParsingTimeLimit = t;
  }

  public long getRequestParsingTimeLimit() {
    return requestParsingTimeLimit;
  }

  @Override
  public void send(byte[] bytes) {
    try {
      output.write(bytes);
      output.flush();
    }
    catch (IOException e) {
      LOG.log(Level.INFO, format("Output stream closed unexpectedly: %s (Stop button pressed?)", e.getMessage()));
    }
  }

  @Override
  public void close() {
    try {
      log(socket, request, response);
      socket.close();
    }
    catch (IOException e) {
      LOG.log(Level.WARNING, "Error while closing socket", e);
    }
  }

  public Request makeRequest() {
    request = new Request(input);
    request.setPeerDn(SocketFactory.peerDn(socket));
    request.setContextRoot(context.contextRoot);
    return request;
  }

  private void sendResponse() throws IOException {
    response.sendTo(this);
  }

  private Response makeResponse(final Request request) throws SocketException {
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
        reportError(400, "The request could not be parsed.");
      }
    } catch (SocketException se) {
      throw se;
    } catch (TimeoutException e) {
      reportError(408, "The client request has been unproductive for too long. It has timed out and will no longer be processed.");
    } catch (HttpException e) {
      reportError(400, e.getMessage());
    } catch (Exception e) {
      reportError(e);
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

  private void reportError(int status, String message) {
    try {
      response = new ErrorResponder(message).makeResponse(context, request);
      response.setStatus(status);
    }
    catch (Exception e) {
      LOG.log(Level.WARNING, "Can not report error (status = " + status + ", message = " + message + ")", e);
    }
  }

  private void reportError(Exception e) {
    response = new ErrorResponder(e).makeResponse(context, request);
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
