// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.testsystems.fit.FitTestSystem;
import fitnesse.testsystems.fit.SocketDealer;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testsystems.fit.FitSocketReceiver;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class ExposeThreadingIssueInMockResponseTest {
  private WikiPage root;
  private MockRequest request;
  private TestResponder responder;
  private FitNesseContext context;
  private String results;
  private FitSocketReceiver receiver;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    request = new MockRequest();
    responder = new TestResponder();
    int port = 9123;
    context = FitNesseUtil.makeTestContext(root, port);

    receiver = new FitSocketReceiver(port, FitTestSystem.socketDealer());
    receiver.receiveSocket();
  }

  @After
  public void tearDown() throws Exception {
    receiver.close();
  }

  public static void assertHasRegexp(String regexp, String output) {
    Matcher match = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
    boolean found = match.find();
    if (!found)
      Assert.fail("The regexp <" + regexp + "> was not found in: " + output + ".");
  }

  @Test
  public void testDoSimpleSlimTable() throws Exception {
    doSimpleRun(simpleSlimDecisionTable());
    assertHasRegexp("<td><span class=\"pass\">wow</span></td>", results);
  }

  private String simpleSlimDecisionTable() {
    return "!define TEST_SYSTEM {slim}\n" + "|!-DT:fitnesse.slim.test.TestSlim-!|\n" + "|string|get string arg?|\n"
      + "|wow|wow|\n";
  }

  private void doSimpleRun(String fixtureTable) throws Exception {
    String simpleRunPageName = "TestPage";
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse(simpleRunPageName), classpathWidgets() + fixtureTable);
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    results = sender.sentData();
  }

  private String classpathWidgets() {
    return "!path classes\n";
  }
}
