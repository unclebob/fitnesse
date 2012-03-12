// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import static org.junit.Assert.assertNotNull;
import static util.RegexTestCase.assertSubString;

import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.MockCommandRunner;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;

public class CompositeExecutionLogTest {
  private static String ErrorLogName = ExecutionLog.ErrorLogName;
  private WikiPage testPage;
  private MockCommandRunner runner;
  private CompositeExecutionLog log;
  private WikiPage root;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    testPage = root.addChildPage("TestPage");
    context = FitNesseUtil.makeTestContext(root);
    runner = new MockCommandRunner("some command", 123);
    log = new CompositeExecutionLog(testPage);
  }

  @Test
  public void publish() throws Exception {
    log.add("testSystem1", new ExecutionLog(testPage, runner, context.pageFactory));
    log.add("testSystem2", new ExecutionLog(testPage, runner, context.pageFactory));
    log.publish();
    WikiPage errorLogPage = root.getChildPage(ErrorLogName);
    assertNotNull(errorLogPage);
    WikiPage testErrorLog = errorLogPage.getChildPage("TestPage");
    assertNotNull(testErrorLog);
    String content = testErrorLog.getData().getContent();

    assertSubString("!3 !-testSystem1", content);
    assertSubString("!3 !-testSystem2", content);
    assertSubString("'''Command: '''", content);
    assertSubString("!-some command-!", content);
    assertSubString("'''Exit code: '''", content);
    assertSubString("123", content);
    assertSubString("'''Date: '''", content);
    assertSubString("'''Time elapsed: '''", content);
  }

}
