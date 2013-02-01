// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fit.FitProtocol;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.components.ClassPathBuilder;
import fitnesse.components.FitClient;
import fitnesse.html.SetupTeardownAndLibraryIncluder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.*;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class FitClientResponder implements Responder, ResponsePuppeteer, TestSystemListener {
  private FitNesseContext context;
  private PageCrawler crawler;
  private String resource;
  private WikiPage page;
  private boolean shouldIncludePaths;
  private String suiteFilter;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;
    crawler = context.root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    resource = request.getResource();
    shouldIncludePaths = request.hasInput("includePaths");
    suiteFilter = (String) request.getInput("suiteFilter");
    return new PuppetResponse(this);
  }

  @Override
  public void readyToSend(ResponseSender sender) {
    Socket socket = sender.getSocket();
    WikiPagePath pagePath = PathParser.parse(resource);
    try {
      if (!crawler.pageExists(context.root, pagePath))
	      FitProtocol.writeData(notFoundMessage(), socket.getOutputStream());
      else {
	      page = crawler.getPage(context.root, pagePath);
	      PageData data = page.getData();

      	if (data.hasAttribute("Suite"))
      	  handleSuitePage(socket, page, context.root);
      	else if (data.hasAttribute("Test"))
      	  handleTestPage(socket, data);
      	else
      	  FitProtocol.writeData(notATestMessage(), socket.getOutputStream());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    sender.close();
  }

  private void handleTestPage(Socket socket, PageData data) throws IOException, InterruptedException {
    FitClient client = startClient(socket);

    if (shouldIncludePaths) {
      String classpath = new ClassPathBuilder().getClasspath(page);
      client.send(classpath);
    }

    sendPage(data, client, true);
    closeClient(client);
  }

  private void handleSuitePage(Socket socket, WikiPage page, WikiPage root) throws IOException, InterruptedException {
    FitClient client = startClient(socket);
    SuiteFilter filter = new SuiteFilter(suiteFilter, null, null, null);
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    List<WikiPage> testPages = suiteTestFinder.makePageList();

    if (shouldIncludePaths) {
      String classpath = new ClassPathBuilder().buildClassPath(testPages);
      client.send(classpath);
    }

    for (WikiPage testPage : testPages) {
      PageData testPageData = testPage.getData();
      sendPage(testPageData, client, false);
    }
    closeClient(client);
  }

  private void sendPage(PageData data, FitClient client, boolean includeSuiteSetup) throws IOException, InterruptedException {
    String pageName = crawler.getRelativeName(page, data.getWikiPage());
    SetupTeardownAndLibraryIncluder.includeInto(data, includeSuiteSetup);
    String testableHtml = data.getHtml();
    String sendableHtml = pageName + "\n" + testableHtml;
    client.send(sendableHtml);
  }

  private void closeClient(FitClient client) throws IOException, InterruptedException {
    client.done();
    client.join();
  }

  private FitClient startClient(Socket socket) throws IOException, InterruptedException {
    FitClient client = new FitClient(this);
    client.acceptSocket(socket);
    return client;
  }

  private String notATestMessage() {
    return resource + " is neither a Test page nor a Suite page.";
  }

  private String notFoundMessage() {
    return "The page " + resource + " was not found.";
  }

  @Override
  public void acceptOutputFirst(String output) {
  }

  @Override
  public void testComplete(TestSummary testSummary) {
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
}
