// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class WikiImportPropertyFormatUpdate extends PageTraversingUpdate {
  public WikiImportPropertyFormatUpdate(UpdaterImplementation updater) {
    super(updater);
  }

  public void processPage(WikiPage currentPage) throws Exception {
    PageData data = currentPage.getData();
    WikiPageProperties props = data.getProperties();

    if (props.has("WikiImportSource")) {
      String source = props.get("WikiImportSource");
      WikiImportProperty importProperty = new WikiImportProperty(source);
      importProperty.addTo(props);
      props.remove("WikiImportSource");
      currentPage.commit(data);
    } else if (props.has("WikiImportRoot")) {
      String source = props.get("WikiImportRoot");
      WikiImportProperty importProperty = new WikiImportProperty(source);
      importProperty.setRoot(true);
      importProperty.addTo(props);
      props.remove("WikiImportRoot");
      currentPage.commit(data);
    }
  }

  public String getName() {
    return "WikiImportFormatUpdate";
  }

  public String getMessage() {
    return "Updating the format of WikiImport properties";
  }
}
