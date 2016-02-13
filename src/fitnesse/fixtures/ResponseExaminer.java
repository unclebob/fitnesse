// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.ColumnFixture;
import fitnesse.html.HtmlUtil;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseExaminer extends ColumnFixture {
  public String type;
  public String pattern;
  public String line;
  private String value;
  public int number;
  private Matcher matcher;
  private int currentLine = 0;
  private int currentPosition = 0;
  private int group =0;

  // Content is escaped, since it's used by both FIT and SLiM.
  public String contents() throws Exception {
    String sentData = FitnesseFixtureContext.sender.sentData();
    return HtmlUtil.escapeHTML(sentData);
  }

  public String html() throws Exception {
    String sentData = FitnesseFixtureContext.sender.sentData();
    int headerEnd = sentData.indexOf("\r\n\r\n");
    return sentData.substring(headerEnd+4); 
  }

  public String fullContents() throws Exception {
    return HtmlUtil.escapeHTML(FitnesseFixtureContext.sender.sentData());
  }

    public boolean inOrder() throws Exception {
      if (line == null) {
        return false;
      }
      String pageContent = FitnesseFixtureContext.sender.sentData();
      String[] lines = arrayifyLines(pageContent);
      String l = line.trim();
      for (int i = currentLine; i < lines.length; i++) {
        if (lines[i].trim().contains(l)) {
          currentLine = i;
          return true;
        }
      }
      return false;
    }

    public boolean occursAfter() throws Exception {
        if (line == null) {
            return false;
        }
        String pageContent = FitnesseFixtureContext.sender.sentData();
        int matchPosition = pageContent.indexOf(line, currentPosition);
        if (matchPosition < 0) return false;
        currentPosition = matchPosition;
        return true;
    }

  public int matchCount() throws Exception {
    Pattern p = Pattern.compile(pattern, Pattern.MULTILINE + Pattern.DOTALL);
    extractValueFromResponse();

    matcher = p.matcher(getValue());
    int matches = 0;
    while (matcher.find()) {
      matches++;
    }
    return matches;
  }



  public void extractValueFromResponse() throws Exception {
    setValue(null);
    if (type.equals("contents"))
      setValue(HtmlUtil.unescapeHTML(FitnesseFixtureContext.sender.sentData()));
    else if (type.equals("fullContents"))
      setValue(fullContents());
    else if (type.equals("rawContents"))
      setValue(FitnesseFixtureContext.sender.sentData());
    else if (type.equals("stringContents"))
      setValue(FitnesseFixtureContext.sender.toString());
    else if (type.equals("pageContents"))
      setValue(FitnesseFixtureContext.page.toString());
    else if (type.equals("pageHtml"))
      setValue(FitnesseFixtureContext.page.getHtml());
    else if (type.equals("matchers"))
      setValue(matcher.group(group));
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
    if (type.equals("contents")) {
      return FitnesseFixtureContext.page.getHtml();
    } else if (type.equals("line")) {
      return getLine(number);
    } else {
      throw new Exception("Bad type in ResponseExaminer");
    }
  }

  private String getLine(int number) throws Exception {
    String value = null;
    String pageContent = FitnesseFixtureContext.page.getHtml();
    String lineizedContent = convertBreaksToLineSeparators(pageContent);
    StringTokenizer tokenizedLines = tokenizeLines(lineizedContent);
    for (int i = number; i != 0; i--)
      value = tokenizedLines.nextToken();
    return value != null ? value.trim() : null;
  }

  private StringTokenizer tokenizeLines(String lineizedContent) {
    return new StringTokenizer(lineizedContent, System.getProperty("line.separator"));
  }

  private String[] arrayifyLines(String lineizedContent) {
    return lineizedContent.split(System.getProperty("line.separator"));
  }

  public static String convertBreaksToLineSeparators(String pageContent) {
    return pageContent.replaceAll("<br/>", System.getProperty("line.separator"));
  }

  public String found() throws Exception {
    return found(this.group);
  }
  
  public String found(int group) throws Exception {
    Pattern p = Pattern.compile(pattern, Pattern.MULTILINE + Pattern.DOTALL);
    extractValueFromResponse();

    matcher = p.matcher(getValue());
   return matcher.find() ?  matcher.group(group) : null; 
  }

  public Matcher matcher(){ 
   return matcher; 
  }

  public String source() {
    return getValue();
  }

  public String wrappedHtml() throws Exception {
    String txt = FitnesseFixtureContext.sender.sentData();
    String txt2 = txt.replaceAll("(<br */?>)", "$1" + System.getProperty("line.separator"));
    return "<pre>" + HtmlUtil.escapeHTML(txt2) + "</pre>";
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

  public void setGroup(int number) {
    this.group = number;
  }

  public String getValue() {
    return value;
  }

  public void setLine(String line) {
    this.line = line;
  }
}
