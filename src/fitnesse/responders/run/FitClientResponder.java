// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import fit.FitProtocol;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.components.ClassPathBuilder;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.testrunner.TestPageWithSuiteSetUpAndTearDown;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import fitnesse.testsystems.fit.FitClient;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.testsystems.fit.FitClientListener;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class FitClientResponder implements Responder, ResponsePuppeteer, FitClientListener {
  private FitNesseContext context;
  private String resource;
  private WikiPage page;
  private boolean shouldIncludePaths;
  private String suiteFilter;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;
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
      PageCrawler crawler = context.root.getPageCrawler();
      if (!crawler.pageExists(pagePath))
        FitProtocol.writeData(notFoundMessage(), socket.getOutputStream());
      else {
        page = crawler.getPage(pagePath);
        PageData data = page.getData();

      	if (data.hasAttribute("Suite"))
      	  handleSuitePage(socket, page, context.root);
      	else if (data.hasAttribute("Test"))
      	  handleTestPage(socket, page);
      	else
      	  FitProtocol.writeData(notATestMessage(), socket.getOutputStream());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    sender.close();
  }

  private void handleTestPage(Socket socket, WikiPage testPage) throws IOException, InterruptedException {
    FitClient client = startClient(socket);

    if (shouldIncludePaths) {
      String classpath = new ClassPathBuilder().getClasspath(page);
      client.send(classpath);
    }

    sendPage(new TestPageWithSuiteSetUpAndTearDown(testPage), client);
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
      sendPage(new WikiTestPage(testPage), client);
    }
    closeClient(client);
  }

  private void sendPage(WikiTestPage testPage, FitClient client) throws IOException, InterruptedException {
    String pageName = page.getPageCrawler().getRelativeName(testPage.getSourcePage());
    String testableHtml = testPage.getDecoratedData().getHtml();
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
  public void testOutputChunk(String output) {
  }

  @Override
  public void testComplete(TestSummary testSummary) {
  }

  @Override
  public void exceptionOccurred(Exception e) {
  }

}
