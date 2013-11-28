// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import java.io.IOException;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.wiki.ClassPathBuilder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.testrunner.WikiPageDescriptor;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.Utils;

/*
This responder is a test rig for SlimTestSystemTest, which makes sure that the SlimTestSystem works nicely with
responders in general.
*/
public abstract class SlimResponder implements Responder, TestSystemListener {
  protected boolean fastTest = false;
  SlimTestSystem testSystem;
  private WikiPage page;
  private PageData pageData;
  private FitNesseContext context;
  private Throwable slimException;
  private StringBuilder output;
  private TestSummary testSummary;

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
    PageCrawler crawler = context.root.getPageCrawler();
    page = crawler.getPage(path);
    if (page != null)
      pageData = page.getData();
  }

  public FitNesseContext getContext() {
    return context;
  }

  public WikiPage getPage() {
    return page;
  }

  protected Descriptor getDescriptor() {
    return new WikiPageDescriptor(page.readOnlyData(), true, false, new ClassPathBuilder().getClasspath(page));
  }

  public class SlimRenderer {

    public String render() {

      try {
        output = new StringBuilder(512);
        testSystem = getTestSystem();
        testSystem.start();
        testSystem.runTests(new WikiTestPage(pageData));
      } catch (IOException e) {
        slimException = e;
      } finally {
        try {
          if (testSystem != null) testSystem.bye();
        } catch (IOException e) {
          if (slimException == null) {
            slimException = e;
          }
        }
      }
      String exceptionString = "";
      if (slimException != null) {
        exceptionString = String.format("<div class='error'>%s</div>", Utils.escapeHTML(slimException.getMessage()));
      }
      return exceptionString + output.toString();
    }
  }

  protected abstract SlimTestSystem getTestSystem() throws IOException;

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  boolean slimOpen() {
    boolean slimOpen = false;
    return slimOpen;
  }

  public TestSummary getTestSummary() {
    return testSummary;
  }

  protected void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testOutputChunk(String output) {
    this.output.append(output);
  }

  @Override
  public void testStarted(TestPage testPage) {
    //
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary)  {
    this.testSummary = testSummary;
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable throwable) {
    slimException = throwable;
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }
}

