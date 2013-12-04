// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.LinkedList;
import java.util.List;

public class WikiPageUtil {

  public static void setPageContents(WikiPage page, String pageContents) {
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

  public static WikiPage addPage(WikiPage context, WikiPagePath path, String content) {
    WikiPage page = addPage(context, path);
    if (page != null) {
      PageData data = new PageData(page.getData(), content);
      page.commit(data);
    }
    return page;
  }

  public static WikiPage addPage(WikiPage context, WikiPagePath path) {
    return getOrMakePage(context, path.getNames());
  }

  private static WikiPage getOrMakePage(WikiPage context, List<?> namePieces) {
    String first = (String) namePieces.get(0);
    List<?> rest = namePieces.subList(1, namePieces.size());
    WikiPage current;
    if (context.getChildPage(first) == null)
      current = context.addChildPage(first);
    else
      current = context.getChildPage(first);
    if (rest.size() == 0)
      return current;
    return getOrMakePage(current, rest);
  }

  public static String makePageHtml(ReadOnlyPageData pageData) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(getHeaderPageHtml(pageData.getWikiPage()));
    buffer.append(pageData.getHtml());
    return buffer.toString();
  }
}
