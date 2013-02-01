// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.*;

import java.io.IOException;

/*
This responder is a test rig for SlimTestSystemTest, which makes sure that the SlimTestSystem works nicely with
responders in general.
*/
public abstract class SlimResponder implements Responder, TestSystemListener {
  private boolean slimOpen = false;
  private boolean fastTest = false;
  SlimTestSystem testSystem;
  private WikiPage page;
  private PageData pageData;
  private PageCrawler crawler;
  private FitNesseContext context;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;
    loadPage(request.getResource(), context);

    SimpleResponse response = new SimpleResponse();
    HtmlPage html = context.pageFactory.newPage();
    html.setMainTemplate("render.vm");
    html.put("content", new SlimRenderer());
    response.setContent(html.html());
    return response;
  }

  protected void loadPage(String pageName, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(pageName);
    crawler = context.root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    page = crawler.getPage(context.root, path);
    if (page != null)
      pageData = page.getData();
  }

  public FitNesseContext getContext() {
    return context;
  }

  public WikiPage getPage() {
    return page;
  }

  public class SlimRenderer {

    public String render() {
      String html = null;

      TestSystem.Descriptor descriptor = getDescriptor();
      try {
        testSystem = getTestSystem();
        testSystem.getExecutionLog();
        testSystem.start();
        testSystem.setFastTest(fastTest);
        html = testSystem.runTestsAndGenerateHtml(pageData);
        testSystem.bye();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return html;
    }
  }

  protected TestSystem.Descriptor getDescriptor() {
    return TestSystem.getDescriptor(page, context.pageFactory, false);
  }

  protected abstract SlimTestSystem getTestSystem();

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  boolean slimOpen() {
    return slimOpen;
  }

  public ReadOnlyPageData getTestResults() {
    return testSystem.getTestResults();
  }

  public TestSummary getTestSummary() {
    return testSystem.getTestSummary();
  }

  protected void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  @Override
  public void acceptOutputFirst(String output) {
  }

  @Override
  public void testComplete(TestSummary testSummary)  {
  }

  @Override
  public void exceptionOccurred(Throwable e) {
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }

  public String getCommandLine() {
    return testSystem.buildCommand();
  }
}

