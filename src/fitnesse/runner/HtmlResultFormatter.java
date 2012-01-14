// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.InputStream;

import org.apache.velocity.VelocityContext;

import fitnesse.components.ContentBuffer;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.responders.run.formatters.SuiteHtmlFormatter;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import fitnesse.FitNesseContext;

public class HtmlResultFormatter implements ResultFormatter {
  private ContentBuffer buffer;
  private boolean closed = false;
  private SuiteHtmlFormatter suiteFormatter;
  private FitNesseContext context;
  private String host;
  private String rootPath;
  private HtmlPage htmlPage;

  public HtmlResultFormatter(FitNesseContext context, String host, String rootPath) throws Exception {
    this.context = context;
    this.host = host;
    this.rootPath = rootPath;

    buffer = new ContentBuffer(".html");

    createPage(context.htmlPageFactory, rootPath);
    suiteFormatter = createCustomFormatter();
    suiteFormatter.writeHead(null);
    System.out.println("Built HtmlResultFormatter for " + rootPath);
  }

  private SuiteHtmlFormatter createCustomFormatter() throws Exception {
    SuiteHtmlFormatter formatter = new SuiteHtmlFormatter(context) {
      @Override
      protected void writeData(String output) throws Exception {
        buffer.append(output);
      }
      
      @Override
      protected HtmlPage buildHtml(String pageType) throws Exception {
        return htmlPage;
      }
    };
    return formatter;
  }
  
  private void createPage(HtmlPageFactory pageFactory, String rootPath) throws Exception {
    htmlPage = context.htmlPageFactory.newPage();

    htmlPage.setTitle(rootPath);
    htmlPage.put("baseUri", baseUri(host));

    htmlPage.setPageTitle(new PageTitle("Command Line Test Results", PathParser.parse(rootPath)));
  }

  public String baseUri(String host) {
    StringBuffer href = new StringBuffer("http://");
    href.append(host);
    href.append("/");
    return href.toString();
  }

  public void acceptResult(PageResult result) throws Exception {
    String relativePageName = result.title();
    suiteFormatter.announceStartNewTest(relativePageName, rootPath + "." + relativePageName);
    suiteFormatter.testOutputChunk(result.content());
    suiteFormatter.processTestResults(relativePageName, result.testSummary());
  }

  public void acceptFinalCount(TestSummary testSummary) throws Exception {
    suiteFormatter.testSummary();
    suiteFormatter.finishWritingOutput();
  }

  private void close() throws Exception {
    if (!closed) {
      suiteFormatter.finishWritingOutput();
      closed = true;
    }
  }

  public int getByteCount() throws Exception {
    close();
    return buffer.getSize();
  }

  public InputStream getResultStream() throws Exception {
    close();
    return buffer.getInputStream();
  }

}