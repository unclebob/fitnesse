// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.LinkedList;

public class WikiPageUtil {

  public static void setPageContents(WikiPage page, String pageContents) throws Exception {
    PageData pageData = page.getData();
    pageData.setContent(pageContents);
    page.commit(pageData);
  }

  public static String getHeaderPageHtml(WikiPage wikiPage) {
    WikiPage header = wikiPage.getHeaderPage();
    return header == null ? "" : header.readOnlyData().getHtml();
  }

  public static String getFooterPageHtml(WikiPage wikiPage) {
    WikiPage footer = wikiPage.getFooterPage();
    return footer == null ? "" : footer.readOnlyData().getHtml();
  }
}
