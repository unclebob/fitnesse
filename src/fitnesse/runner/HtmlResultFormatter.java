// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.IOException;
import java.io.InputStream;

import org.apache.velocity.VelocityContext;

import fitnesse.components.ContentBuffer;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.responders.run.formatters.SuiteHtmlFormatter;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.HtmlPageFactory;
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

  public HtmlResultFormatter(FitNesseContext context, String host, String rootPath) throws IOException {
    this.context = context;
    this.host = host;
    this.rootPath = rootPath;

    buffer = new ContentBuffer(".html");

    createPage(context.htmlPageFactory, rootPath);
    suiteFormatter = createCustomFormatter();
    suiteFormatter.writeHead(null);
    System.out.println("Built HtmlResultFormatter for " + rootPath);
  }

  private SuiteHtmlFormatter createCustomFormatter() {
    SuiteHtmlFormatter formatter = new SuiteHtmlFormatter(context) {
      @Override
      protected void writeData(String output) {
        try {
          buffer.append(output);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      
      @Override
      protected HtmlPage buildHtml(String pageType) {
        return htmlPage;
      }
    };
    return formatter;
  }
  
  private void createPage(HtmlPageFactory pageFactory, String rootPath) {
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

  public void acceptResult(PageResult result) throws IOException {
    String relativePageName = result.title();
    suiteFormatter.announceStartNewTest(relativePageName, rootPath + "." + relativePageName);
    suiteFormatter.testOutputChunk(result.content());
    suiteFormatter.processTestResults(relativePageName, result.testSummary());
  }

  public void acceptFinalCount(TestSummary testSummary) throws IOException {
    suiteFormatter.testSummary();
    suiteFormatter.finishWritingOutput();
  }

  private void close() throws IOException {
    if (!closed) {
      suiteFormatter.finishWritingOutput();
      closed = true;
    }
  }

  public int getByteCount() throws IOException {
    close();
    return buffer.getSize();
  }

  public InputStream getResultStream() throws IOException {
    close();
    return buffer.getInputStream();
  }

}
