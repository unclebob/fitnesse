// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.parser.Collapsible;

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
      buffer.append(Collapsible.generateHtml(Collapsible.CLOSED, key, exceptions.get(key)));
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
