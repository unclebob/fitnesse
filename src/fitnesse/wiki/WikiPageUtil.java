// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WikiPageUtil {

  public static final String PAGE_HEADER = "PageHeader";
  public static final String PAGE_FOOTER = "PageFooter";
  public static final String FRONT_PAGE = "FrontPage";

  public static void setPageContents(WikiPage page, String pageContents) {
    PageData pageData = page.getData();
    pageData.setContent(pageContents);
    page.commit(pageData);
  }

  public static WikiPage getHeaderPage(WikiPage wikiPage) {
    return wikiPage.getPageCrawler().getClosestInheritedPage(PAGE_HEADER);
  }

  public static WikiPage getFooterPage(WikiPage wikiPage) {
    return wikiPage.getPageCrawler().getClosestInheritedPage(PAGE_FOOTER);
  }


  public static String getHeaderPageHtml(WikiPage wikiPage) {
    WikiPage header = getHeaderPage(wikiPage);
    return header == null ? "" : header.getHtml();
  }

  public static String getFooterPageHtml(WikiPage wikiPage) {
    WikiPage footer = getFooterPage(wikiPage);
    return footer == null ? "" : footer.getHtml();
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
    if (context.getChildPage(first) == null) {
      current = context.addChildPage(first);
    } else
      current = context.getChildPage(first);
    if (rest.isEmpty())
      return current;
    return getOrMakePage(current, rest);
  }

  public static String makePageHtml(WikiPage page) {
    return getHeaderPageHtml(page) + page.getHtml();
  }

  public static File resolveFileUri(String fullPageURI, File rootPath) {
    URI uri = URI.create(fullPageURI);
    try {
      return new File(uri);
    } catch (IllegalArgumentException e) {
      if (!"file".equals(uri.getScheme()) || rootPath == null) {
        throw e;
      }
      // "URI has an authority component" (file://something) or "URI is not hierarchical" (file:something)
      // As a fallback, resolve as a relative URI
      URI rootUri = rootPath.toURI();
      uri = rootUri.resolve(uri.getSchemeSpecificPart().replaceFirst("^/+", ""));
      return new File(uri);
    }
  }

  public static List<String> getXrefPages(WikiPage page) {
    if (page instanceof WikitextPage) {
      List<String> result = new ArrayList<>();
      ((WikitextPage) page).getSyntaxTree().findXrefs(result::add);
      return result;
    }
    return Collections.emptyList();
  }

  public static boolean isTestPage(WikiPage page) {
    return page.getData().hasAttribute("Test");
  }
}
