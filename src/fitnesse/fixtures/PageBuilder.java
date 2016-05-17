// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import fit.Fixture;
import fitnesse.wiki.*;

public class PageBuilder extends Fixture {
  private PrintWriter writer;
  private StringWriter stringWriter;
  private String pageAttributes = null;

  public PageBuilder() {
    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
  }

  public void line(String line) {
    if (line == null)
      line = "";
    if (line.startsWith("\\"))
      line = line.substring(1);
    line = line.replace("&bar;", "|");
    line = line.replaceAll("&bang;", "!");
    line = line.replace("&dollar;", "$");
    writer.println(line);
  }

  public void page(String name) throws Exception {
    String content = stringWriter.toString();
    WikiPagePath path = PathParser.parse(name);
    WikiPage page = WikiPageUtil.addPage(FitnesseFixtureContext.context.getRootPage(), path, content);
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
      if(nameValuePair.contains("!COMMA!")){
        nameValuePair = nameValuePair.replace("!COMMA!", ",");
      }
      int equals = nameValuePair.indexOf("=");
      if (equals < 0)
        throw new Exception("Attribute must have form name=value");
      String name = nameValuePair.substring(0, equals);
      String value = nameValuePair.substring(equals + 1);
      data.setAttribute(name, value);
    }
  }
}
