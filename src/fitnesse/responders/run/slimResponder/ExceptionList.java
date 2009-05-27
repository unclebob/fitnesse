// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fitnesse.html.HtmlUtil;

public class ExceptionList {
  private boolean stopTestCalled = false;
  private Map<String, String> exceptions;
  private boolean firstHtmlRequest = true;
  private int testNumber = 0;

  private StringBuffer buffer;

  public ExceptionList() {
    exceptions = new HashMap<String, String>();
  }

  public void addException(String key, String exceptionStack)  {
    exceptions.put(key, exceptionStack);
  }
  
  public String toHtml() {
    buffer = new StringBuffer();
    if (exceptions.size() == 0) {
      return "";
    }
    else if (firstHtmlRequest) {
      firstHtmlRequest = false;
      return writeExceptionDiv();
    }
    else {
      return writeUpdateExceptionDivHtml();
    }
  }

  public boolean stopTestCalled() {
    return stopTestCalled;
  }
  
  public void setStopTestCalled() {
    stopTestCalled = true;
  }
  
  public void resetForNewTest() {
    stopTestCalled = false;
    firstHtmlRequest = true;
    testNumber++;
  }
  
  private String writeExceptionDiv() {
    header();
    writeExceptions();
    footer();
    return buffer.toString();
  }
  
  private String writeUpdateExceptionDivHtml() {
    writeExceptions();
    return HtmlUtil.makeAppendElementScript(getDivName(), buffer.toString()).html();
  }

  private void footer() {
    buffer.append("</div><hr/>");
  }

  private void writeExceptions() {
    for (String key : exceptions.keySet()) {
      buffer.append(String.format("<a name=\"%s\"/><b></b>", key));
      String collapsibleSectionFormat = "<div class=\"collapse_rim\">"
          + "<div style=\"float: right;\" class=\"meta\"><a href=\"javascript:expandAll();\">Expand All</a> | <a href=\"javascript:collapseAll();\">Collapse All</a></div>"
          + "<a href=\"javascript:toggleCollapsable('%d');\">"
          + "<img src=\"/files/images/collapsableClosed.gif\" class=\"left\" id=\"img%d\"/>"
          + "</a>" + "&nbsp;<span class=\"meta\">%s </span>\n" + "\n"
          + "\t<div class=\"hidden\" id=\"%d\"><pre>%s</pre></div>\n"
          + "</div>";
      long id = new Random().nextLong();
      buffer.append(String.format(collapsibleSectionFormat, id, id, key, id,
          exceptions.get(key)));
    }
    exceptions.clear();
  }

  private void header() {
    buffer.append("<div id=\"" + getDivName() + "\"><H3> <span class=\"fail\">Exceptions</span></H3><br/>");
  }
  
  private String getDivName() {
    return "test_exceptions" + testNumber;
  }
}
