// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.IOException;

import fitnesse.wiki.NoPruningStrategy;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class MockWikiImporter extends WikiImporter {
  public static final String mockContent = "mock importer content";
  public boolean fail;

  @Override
  protected void importRemotePageContent(WikiPage localPage) throws IOException {
    if (fail)
      importerClient.pageImportError(localPage, new Exception("Import of remote page content failed"));
    else
      setMockContent(localPage);
  }

  private void setMockContent(WikiPage localPage) {
    PageData data = localPage.getData();
    data.setContent(mockContent);
    localPage.commit(data);
  }

  @Override
  public void importWiki(WikiPage page) {
    for (WikiPage child : page.getChildren()) {
      child.getPageCrawler().traverse(this, new NoPruningStrategy());
    }
  }

  @Override
  public void process(WikiPage page) {
    setMockContent(page);
  }
}
