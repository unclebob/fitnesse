// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class FrontPageUpdate implements Update {
  private UpdaterImplementation updater;

  public FrontPageUpdate(UpdaterImplementation updater) {
    this.updater = updater;
  }

  public String getName() {
    return "FrontPageUpdate";
  }

  public String getMessage() {
    return "Creating FrontPage";
  }

  public boolean shouldBeApplied() throws Exception {
    return !updater.getRoot().hasChildPage("FrontPage");
  }

  public void doUpdate() throws Exception {
    WikiPage frontPage = updater.getRoot().getPageCrawler().addPage(updater.getRoot(), PathParser.parse("FrontPage"));
    PageData data = new PageData(frontPage);
    data.setContent(content);
    frontPage.commit(data);
  }

  private static String content = "\n\n\n" +
    "!c !3 Welcome to the Wonderful World of !-FitNesse-!!";
}
