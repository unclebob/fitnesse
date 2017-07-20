// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.UnauthorizedResponder;
import fitnesse.http.MockRequest;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseParser;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.MockSocket;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

public class FitNesseExpediterTest {
  public static final int REQUEST_PARSING_TIME_LIMIT = 200;
  private FitNesseExpediter expediter;
  private MockSocket socket;
  private FitNesseContext context;
  private PipedInputStream clientInput;
  private PipedOutputStream clientOutput;
  private ResponseParser response;
  private java.util.concurrent.ExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    executorService = Executors.newFixedThreadPool(2);
    WikiPage root = context.getRootPage();
    root.addChildPage("FrontPage");
    socket = new MockSocket();
    expediter = new FitNesseExpediter(socket, context, executorService);
  }

  @Test
  public void testAuthenticationGetsCalled() throws Exception {
    context = FitNesseUtil.makeTestContext(new StoneWallAuthenticator());
    WikiPage root = context.getRootPage();
    root.addChildPage("FrontPage");
    expediter = new FitNesseExpediter(socket, context, executorService);
    MockRequest request = new MockRequest();
    Response response = expediter.createGoodResponse(request);
    assertEquals(401, response.getStatus());
  }

  @Test(expected = IOException.class)
  public void testClosedSocketMidResponse() throws Exception {
    MockRequest request = new MockRequest();
    Response response = expediter.createGoodResponse(request);
    socket.close();
    response.sendTo(expediter);
  }

  @Test
  public void testIncompleteRequestsTimeOut() throws Exception {
    final FitNesseExpediter sender = preparePipedFitNesseExpediter();

    Thread senderThread = makeSendingThread(sender);
    senderThread.start();
    Thread parseResponseThread = makeParsingThread();
    parseResponseThread.start();
    Thread.sleep(REQUEST_PARSING_TIME_LIMIT + 100);

    parseResponseThread.join();

    assertEquals(408, response.getStatus());
  }

  private FitNesseExpediter preparePipedFitNesseExpediter() throws Exception {
    PipedInputStream socketInput = new PipedInputStream();
    clientOutput = new PipedOutputStream(socketInput);
    clientInput = new PipedInputStream();
    PipedOutputStream socketOutput = new PipedOutputStream(clientInput);
    MockSocket socket = new MockSocket(socketInput, socketOutput);
    return new FitNesseExpediter(socket, context, executorService, REQUEST_PARSING_TIME_LIMIT);
  }

  @Test
  public void testCompleteRequest() throws Exception {
    final FitNesseExpediter sender = preparePipedFitNesseExpediter();

    Thread senderThread = makeSendingThread(sender);
    senderThread.start();
    Thread parseResponseThread = makeParsingThread();
    parseResponseThread.start();

    clientOutput.write("GET /root HTTP/1.1\r\n\r\n".getBytes());
    clientOutput.flush();

    parseResponseThread.join();

    assertEquals(200, response.getStatus());
  }

  @Test
  public void slowButCompleteRequestCanTimeOut() throws Exception {
    final FitNesseExpediter sender = preparePipedFitNesseExpediter();

    Thread senderThread = makeSendingThread(sender);
    senderThread.start();
    Thread parseResponseThread = makeParsingThread();
    parseResponseThread.start();

    boolean pipeHasBeenClosed = false;

    // 22 bytes * 20ms sleep => 440 ms for the whole request. Time limit is set to 200ms.
    byte[] bytes = "GET /root HTTP/1.1\r\n\r\n".getBytes();
    try {
      for (byte aByte : bytes) {
        clientOutput.write(aByte);
        clientOutput.flush();
        Thread.sleep(20);
      }
    }
    catch (IOException pipedClosed) {
      pipeHasBeenClosed = true;
    }

    parseResponseThread.join();

    assertTrue(pipeHasBeenClosed);
    assertEquals(408, response.getStatus());
    assertThat(response.getBody(), containsString("The client request has been unproductive for too long. It has timed out and will no longer be processed."));
  }

  private Thread makeSendingThread(final FitNesseExpediter sender) {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          sender.run();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private Thread makeParsingThread() {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          response = new ResponseParser(clientInput);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  class StoneWallAuthenticator extends Authenticator {
    @Override
    public Responder authenticate(FitNesseContext context, Request request, Responder privilegedResponder) {
      return new UnauthorizedResponder();
    }

    @Override
    public boolean isAuthenticated(String username, String password) {
      return false;
    }
  }

}
