// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fitnesse.http.Request;
import fitnesse.wikitext.parser.See;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;

public class WikiPageUtil {

  public static void setPageContents(WikiPage page, String pageContents) {
    PageData pageData = page.getData();
    pageData.setContent(pageContents);
    page.commit(pageData);
  }

  public static WikiPage getHeaderPage(WikiPage wikiPage) {
    return wikiPage.getPageCrawler().getClosestInheritedPage("PageHeader");
  }

  public static WikiPage getFooterPage(WikiPage wikiPage) {
    return wikiPage.getPageCrawler().getClosestInheritedPage("PageFooter");
  }


  public static String getHeaderPageHtml(WikiPage wikiPage) {
    return getHeaderPageHtml(wikiPage, null);
  }

  public static String getHeaderPageHtml(WikiPage wikiPage, Request request) {
    WikiPage header = getHeaderPage(wikiPage);
    if(wikiPage != null && request != null) { ((BaseWikiPage)wikiPage).setUrlParams(request.getMap()); }
    if(header != null && request != null) { ((BaseWikiPage)header).setUrlParams(request.getMap()); }
    return header == null ? "" : header.getHtml();
  }

  public static String getFooterPageHtml(WikiPage wikiPage) {
    return getFooterPageHtml(wikiPage, null);
  }

  public static String getFooterPageHtml(WikiPage wikiPage, Request request) {
    WikiPage footer = getFooterPage(wikiPage);
    if(footer != null && request != null) { ((BaseWikiPage)footer).setUrlParams(request.getMap()); }
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
    if (rest.size() == 0)
      return current;
    return getOrMakePage(current, rest);
  }

  public static String makePageHtml(WikiPage page) {
      return makePageHtml(page, null);
  }

  public static String makePageHtml(WikiPage page, Request request) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(getHeaderPageHtml(page,request));
    buffer.append(page.getHtml());
    return buffer.toString();
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
      final ArrayList<String> xrefPages = new ArrayList<String>();
      ((WikitextPage) page).getSyntaxTree().walkPreOrder(new SymbolTreeWalker() {
        @Override
        public boolean visit(Symbol node) {
          if (node.isType(See.symbolType)) xrefPages.add(node.childAt(0).getContent());
          return true;
        }

        @Override
        public boolean visitChildren(Symbol node) {
          return true;
        }
      });
      return xrefPages;
    }
    return Collections.emptyList();
  }
}
