// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import fit.Fixture;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class PageBuilder extends Fixture {
  private PrintWriter writer;
  private StringWriter stringWriter;
  private String pageAttributes = null;

  public PageBuilder() {
    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
  }

  public void line(String line) {
    if (line.startsWith("\\"))
      line = line.substring(1);
    line = line.replaceAll("&bar;", "|");
    line = line.replaceAll("&bang;", "!");
    line = line.replaceAll("&dollar;", "$");
    writer.println(line);
  }

  public void page(String name) throws Exception {
    String content = stringWriter.toString();
    PageCrawler crawler = FitnesseFixtureContext.root.getPageCrawler();
    WikiPagePath path = PathParser.parse(name);
    WikiPage page = crawler.addPage(FitnesseFixtureContext.root, path, content);
    if (pageAttributes != null) {
      PageData data = page.getData();
      setAttributes(data);
      page.commit(data);
      pageAttributes = null;
    }
  }

  public void attributes(String attributes) {
    pageAttributes = attributes;
  }

  private void setAttributes(PageData data) throws Exception {
    StringTokenizer tokenizer = new StringTokenizer(pageAttributes, ",");
    while (tokenizer.hasMoreTokens()) {
      String nameValuePair = tokenizer.nextToken();
      int equals = nameValuePair.indexOf("=");
      if (equals < 0)
        throw new Exception("Attribute must have form name=value");
      String name = nameValuePair.substring(0, equals);
      String value = nameValuePair.substring(equals + 1);
      data.setAttribute(name, value);
    }
  }
}
