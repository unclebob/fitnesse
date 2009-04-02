// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.InputStream;

import fitnesse.components.ContentBuffer;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.responders.run.SuiteHtmlFormatter;
import fitnesse.responders.run.TestSummary;
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
  }

  private SuiteHtmlFormatter createCustomFormatter() throws Exception {
    SuiteHtmlFormatter formatter = new SuiteHtmlFormatter(context, null, null) {
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
    htmlPage = pageFactory.newPage();
    htmlPage.head.use(makeBaseTag());
    htmlPage.head.add(makeContentTypeMetaTag());
    htmlPage.title.use(rootPath);
    htmlPage.head.add(htmlPage.title);
    htmlPage.head.add(htmlPage.makeCssLink("/files/css/fitnesse_print.css", "screen"));

    HtmlTag script = new HtmlTag("script", scriptContent);
    script.addAttribute("language", "javascript");
    htmlPage.head.add(script);
    htmlPage.body.addAttribute("onload", "localizeInPageLinks()");

    htmlPage.header.use(HtmlUtil.makeBreadCrumbsWithPageType(rootPath, "Command Line Test Results"));
  }

  private HtmlTag makeContentTypeMetaTag() {
    HtmlTag meta = new HtmlTag("meta");
    meta.addAttribute("http-equiv", "Content-Type");
    meta.addAttribute("content", "text/html; charset=utf-8");
    return meta;
  }

  private HtmlTag makeBaseTag() {
    HtmlTag base = new HtmlTag("base");
    StringBuffer href = new StringBuffer("http://");
    href.append(host);
    href.append("/");
    base.addAttribute("href", href.toString());
    return base;
  }

  public void acceptResult(PageResult result) throws Exception {
    String relativePageName = result.title();
    suiteFormatter.announceStartNewTest(relativePageName, rootPath + "." + relativePageName);
    suiteFormatter.processTestOutput(result.content());
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

  public static final String scriptContent = "\n" +
    "function localizeInPageLinks()\n" +
    "{\n" +
    "\tvar base = document.getElementsByTagName('base')[0].href;\n" +
    "\tvar inPageBase = base + \"#\";\n" +
    "\tvar baseLength = inPageBase.length\n" +
    "\tvar aTags = document.getElementsByTagName('a');\n" +
    "\tfor(var i=0; i < aTags.length; i++)\n" +
    "\t{\n" +
    "\t\tvar tag = aTags[i];\n" +
    "\t\tif(tag.href && tag.href.substring(0, baseLength) == inPageBase)\n" +
    "\t\t\ttag.href = location.href + '#' + tag.href.substring(baseLength);\n" +
    "\t}\n" +
    "}\n";
}
