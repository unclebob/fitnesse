// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit.ColumnFixture;
import fitnesse.wikitext.Utils;

public class ResponseExaminer extends ColumnFixture {
  public String type;
  public String pattern;
  public String line;
  private String value;
  public int number;
  private Matcher matcher;
  private int currentLine = 0;

  public String contents() throws Exception {
    String sentData = FitnesseFixtureContext.sender.sentData();
    return Utils.escapeHTML(sentData);
  }

  public String fullContents() throws Exception {
    return Utils.escapeHTML(FitnesseFixtureContext.sender.sentData());
  }

  public boolean inOrder() throws Exception {
    if (line == null) {
      return false;
    }
    String pageContent = FitnesseFixtureContext.sender.sentData();
    String[] lines = arrayifyLines(pageContent);
    for (int i = currentLine; i < lines.length; i++) {
      if (line.equals(lines[i].trim())) {
        currentLine = i;
        return true;
      }
    }
    return false;
  }

  public int matchCount() throws Exception {
    Pattern p = Pattern.compile(pattern, Pattern.MULTILINE + Pattern.DOTALL);
    extractValueFromResponse();

    matcher = p.matcher(value);
    int matches = 0;
    for (matches = 0; matcher.find(); matches++) ;
    return matches;
  }

  public void extractValueFromResponse() throws Exception {
    setValue(null);
    if (type.equals("contents"))
      setValue(Utils.unescapeHTML(FitnesseFixtureContext.sender.sentData()));
    else if (type.equals("fullContents"))
      setValue(fullContents());
    else if (type.equals("status"))
      setValue("" + FitnesseFixtureContext.response.getStatus());
    else if (type.equals("headers")) {
      String text = FitnesseFixtureContext.sender.sentData();
      int headerEnd = text.indexOf("\r\n\r\n");
      setValue(text.substring(0, headerEnd + 2));
    }
  }

  public boolean matches() throws Exception {
    return matchCount() > 0;
  }

  public String string() throws Exception {
    String value = null;
    if (type.equals("contents")) {
      return FitnesseFixtureContext.page.getData().getHtml();
    } else if (type.equals("line")) {
      String pageContent = FitnesseFixtureContext.page.getData().getHtml();
      String lineizedContent = convertBreaksToLineSeparators(pageContent);
      StringTokenizer tokenizedLines = tokenizeLines(lineizedContent);
      for (int i = number; i != 0; i--)
        value = tokenizedLines.nextToken();
      return value.trim();
    } else {
      throw new Exception("Bad type in ResponseExaminer");
    }
  }

  private StringTokenizer tokenizeLines(String lineizedContent) {
    return new StringTokenizer(lineizedContent, System.getProperty("line.separator"));
  }

  private String[] arrayifyLines(String lineizedContent) {
    return lineizedContent.split(System.getProperty("line.separator"));
  }

  public static String convertBreaksToLineSeparators(String pageContent) {
    String lineizedContent = pageContent.replaceAll("<br/>", System.getProperty("line.separator"));
    return lineizedContent;
  }

  public String found() {
    return matcher.group(0);
  }

  public String source() {
    return value;
  }

  public String wrappedHtml() throws Exception {
    String txt = FitnesseFixtureContext.sender.sentData();
    String txt2 = txt.replaceAll("(<br */?>)", "$1" + System.getProperty("line.separator"));
    return "<pre>" + Utils.escapeHTML(txt2) + "</pre>";
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public String getValue() {
    return value;
  }
}
