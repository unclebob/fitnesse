// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import static fitnesse.wiki.PageData.ErrorLogName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static util.RegexTestCase.assertSubString;

import fitnesse.testsystems.CommandRunnerExecutionLog;
import fitnesse.testsystems.MockCommandRunner;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class CompositeExecutionLogTest {
  private MockCommandRunner runner;
  private CompositeExecutionLog log;
  private WikiPage root;
  private FitNesseContext context;


  @Test
  public void testNoErrorLogPageToBeginWith() throws Exception {
    assertFalse(root.hasChildPage(ErrorLogName));
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    WikiPage testPage = root.addChildPage("TestPage");
    PageData data = testPage.getData();
    WikiPageProperties properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "Test Page tags");
    testPage.commit(data);
    context = FitNesseUtil.makeTestContext(root);
    runner = new MockCommandRunner("some command", 123);
    log = new CompositeExecutionLog(testPage);
  }

  @Test
  public void publish() throws Exception {
    log.add("testSystem1", new CommandRunnerExecutionLog(runner));
    log.add("testSystem2", new CommandRunnerExecutionLog(runner));
    log.publish(context.pageFactory);
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
    assertSubString("Test Page tags", testErrorLog.getData().getAttribute(PageData.PropertySUITES));
  }

}
