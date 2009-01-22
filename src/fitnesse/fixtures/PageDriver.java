// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

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

  public String content() throws Exception {
    return requester.contents();
  }
}
