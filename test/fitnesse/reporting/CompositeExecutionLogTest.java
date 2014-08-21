// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static util.RegexTestCase.assertSubString;

import fitnesse.testsystems.ExecutionLogListener;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class CompositeExecutionLogTest {
  private CompositeExecutionLog log;
  private WikiPage root;
  private FitNesseContext context;


  @Test
  public void testNoErrorLogPageToBeginWith() throws Exception {
    assertFalse(root.hasChildPage(WikiPage.ErrorLogName));
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
    log = new CompositeExecutionLog(testPage);
  }

  @Test
  public void publish() throws Exception {
    addTestSystemRun("testSystem1");
    addTestSystemRun("testSystem2");
    log.publish(context.pageFactory);
    WikiPage errorLogPage = root.getChildPage(WikiPage.ErrorLogName);
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

  private void addTestSystemRun(final String testSystemName) {
    log.commandStarted(new ExecutionLogListener.ExecutionContext() {
                         @Override
                         public String getCommand() {
                           return "some command";
                         }

                         @Override
                         public String getTestSystemName() {
                           return testSystemName;
                         }
                       });
    log.exitCode(123);
  }

}
