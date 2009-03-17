// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.responders.run.TestSummary;

public class PageResult {
  private static final Pattern countsPattern = Pattern.compile("(\\d+)[^,]*, (\\d+)[^,]*, (\\d+)[^,]*, (\\d+)[^,]*");

  private StringBuffer contentBuffer = new StringBuffer();
  private TestSummary testSummary;
  private String title;

  public PageResult(String title) {
    this.title = title;
  }

  public PageResult(String title, TestSummary testSummary, String startingContent) throws Exception {
    this(title);
    this.testSummary = testSummary;
    append(startingContent);
  }

  public String content() {
    return contentBuffer.toString();
  }

  public void append(String data) throws Exception {
    contentBuffer.append(data);
  }

  public String title() {
    return title;
  }

  public TestSummary testSummary() {
    return testSummary;
  }

  public void setTestSummary(TestSummary testSummary) {
    this.testSummary = testSummary;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(title).append("\n");
    buffer.append(testSummary.toString()).append("\n");
    buffer.append(contentBuffer);
    return buffer.toString();
  }

  public static PageResult parse(String resultString) throws Exception {
    int firstEndlIndex = resultString.indexOf('\n');
    int secondEndlIndex = resultString.indexOf('\n', firstEndlIndex + 1);

    String title = resultString.substring(0, firstEndlIndex);
    TestSummary testSummary = parseCounts(resultString.substring(firstEndlIndex + 1, secondEndlIndex));
    String content = resultString.substring(secondEndlIndex + 1);

    return new PageResult(title, testSummary, content);
  }

  private static TestSummary parseCounts(String countString) {
    Matcher matcher = countsPattern.matcher(countString);
    if (matcher.find()) {
      int right = Integer.parseInt(matcher.group(1));
      int wrong = Integer.parseInt(matcher.group(2));
      int ignores = Integer.parseInt(matcher.group(3));
      int exceptions = Integer.parseInt(matcher.group(4));
      return new TestSummary(right, wrong, ignores, exceptions);
    } else
      return null;
  }
}
