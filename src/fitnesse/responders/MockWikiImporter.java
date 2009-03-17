// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.util.Iterator;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class MockWikiImporter extends WikiImporter {
  public static String mockContent = "mock importer content";
  public boolean fail;

  protected void importRemotePageContent(WikiPage localPage) throws Exception {
    if (fail)
      importerClient.pageImportError(localPage, new Exception("blah"));
    else
      setMockContent(localPage);
  }

  private void setMockContent(WikiPage localPage) throws Exception {
    PageData data = localPage.getData();
    data.setContent(mockContent);
    localPage.commit(data);
  }

  public void importWiki(WikiPage page) throws Exception {
    PageCrawler pageCrawler = page.getPageCrawler();
    for (Iterator<?> iterator = page.getChildren().iterator(); iterator.hasNext();)
      pageCrawler.traverse((WikiPage) iterator.next(), this);
  }

  public void processPage(WikiPage page) throws Exception {
    setMockContent(page);
  }
}
