// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Test;

public class ChunkingResponderTest {

  private final FitNesseContext context = FitNesseUtil.makeTestContext();

  private Exception exception;
  private ChunkingResponder responder = new ChunkingResponder() {
    protected void doSending() throws Exception {
      throw exception;
    }
  };

  @Test
  public void testException() throws Exception {
    exception = new Exception("test exception");
    ChunkedResponse response = (ChunkedResponse)responder.makeResponse(context, new MockRequest());
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String responseSender = sender.sentData();
    assertSubString("test exception", responseSender);
  }

  @Test
  public void chunkingShouldBeTurnedOffIfnochunkParameterIsPresent() throws Exception {
    MockRequest request = new MockRequest();
    request.addInput("nochunk", "");
    ChunkedResponse response = (ChunkedResponse) responder.makeResponse(context, request);
    assertTrue(response.isChunkingTurnedOff());
  }
}
