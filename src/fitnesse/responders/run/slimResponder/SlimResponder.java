// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import java.io.IOException;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.run.ExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

/*
This responder is a test rig for SlimTestSystemTest, which makes sure that the SlimTestSystem works nicely with
responders in general.
*/
public abstract class SlimResponder implements Responder, TestSystemListener {
  private boolean slimOpen = false;
  ExecutionLog log;
  private boolean fastTest = false;
  SlimTestSystem testSystem;
  private WikiPage page;
  private PageData pageData;
  private PageCrawler crawler;
  
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    loadPage(request.getResource(), context);
    
    SimpleResponse response = new SimpleResponse();
    HtmlPage html = context.htmlPageFactory.newPage();
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
  
  public class SlimRenderer {

    public String render() {
      testSystem = getTestSystem(pageData);
      String html = null;
  
      String classPath = new ClassPathBuilder().getClasspath(page);
      TestSystem.Descriptor descriptor = TestSystem.getDescriptor(page.getData(), false);
      descriptor.testRunner = "fitnesse.slim.SlimService";
      try {
        log = testSystem.getExecutionLog(classPath, descriptor);
        testSystem.start();
        testSystem.setFastTest(fastTest);
        html = testSystem.runTestsAndGenerateHtml(pageData);
        testSystem.bye();
      } catch (IOException e) {
        html = "Could not execute tests: " + e.getMessage();
        e.printStackTrace();
      }
      
      return html;
    }
  }
  
  protected abstract SlimTestSystem getTestSystem(PageData pageData);

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  boolean slimOpen() {
    return slimOpen;
  }

  public PageData getTestResults() {
    return testSystem.getTestResults();
  }

  public TestSummary getTestSummary() {
    return testSystem.getTestSummary();
  }

  protected void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void acceptOutputFirst(String output) {
  }

  public void testComplete(TestSummary testSummary)  {
  }

  public void exceptionOccurred(Throwable e) {
    //todo remove sout
    System.err.println("SlimResponder.exceptionOcurred:" + e.getMessage());
  }

  public String getCommandLine() {
    return testSystem.getCommandLine();
  }
}

