// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import org.json.JSONObject;

public class PageDriver {
  private PageCreator creator = new PageCreator();
  private ResponseRequester requester = new ResponseRequester();
  private ResponseExaminer examiner = new ResponseExaminer();

  public void createPageWithContent(String pageName, String content) throws Exception {
    creator.pageName = pageName;
    creator.pageContents = content;
    creator.valid();
  }

  public int requestPage(String uri) throws Exception {
    requester.uri = uri;
    requester.execute();
    return requester.status();
  }

  public boolean contentMatches(String pattern) throws Exception {
    examiner.type = "contents";
    examiner.pattern = pattern;
    return examiner.matches();
  }

  public boolean contentContains(String subString) throws Exception {
    examiner.type = "contents";
    examiner.extractValueFromResponse();
    return examiner.getValue().indexOf(subString) != -1;
  }

  public boolean containsJsonPacket(String packet) throws Exception {
    packet = ResponseExaminer.convertBreaksToLineSeparators(packet);
    System.out.println("packet = " + packet);
    JSONObject expected = new JSONObject(packet);
    String contentString = requester.contents();
    int jsonStart = contentString.indexOf("{");
    if (jsonStart == -1)
      return false;
    contentString = contentString.substring(jsonStart);
    System.out.println("contentString = " + contentString);
    JSONObject actual = new JSONObject(contentString);
    return expected.toString(1).equals(actual.toString(1));
  }

  public String content() throws Exception {
    return requester.contents();
  }

  public String lineIs(int lineNumber) throws Exception {
    examiner.type = "line";
    examiner.number = lineNumber;
    return examiner.string();
  }

  public String echo(String it) {
    return it;
  }
}
