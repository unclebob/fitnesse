// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.FitNesseTraversalListener;

import java.util.Iterator;
import java.util.List;

public class PageCrawlerImpl implements PageCrawler {
  private PageCrawlerDeadEndStrategy deadEndStrategy;

  protected PageCrawlerImpl() {
  }

  public WikiPage getPage(WikiPage context, WikiPagePath path) throws Exception {
    if (path == null)
      return null;

    if (isRoot(path))
      return getRoot(context);

    if (path.isEmpty())
      return context;

    if (path.isAbsolute()) {
      WikiPagePath relativeToRoot = new WikiPagePath(path);
      relativeToRoot.setPathMode(WikiPagePath.Mode.RELATIVE);
      return getPage(getRoot(context), relativeToRoot);
    } else if (path.isBackwardSearchPath())
      return getSiblingPage(context, path);

    String firstPathElement = path.getFirst();
    WikiPagePath restOfPath = path.getRest();

    WikiPage childPage = context.getChildPage(firstPathElement);
    if (childPage != null)
      return getPage(childPage, restOfPath);
    else
      return getPageAfterDeadEnd(context, firstPathElement, restOfPath);
  }

  private boolean isRoot(WikiPagePath path) {
    return path.isAbsolute() && path.isEmpty();
  }

  protected WikiPage getPageAfterDeadEnd(WikiPage context, String first, WikiPagePath rest) throws Exception {
    rest.addNameToFront(first);
    if (deadEndStrategy != null)
      return deadEndStrategy.getPageAfterDeadEnd(context, rest, this);
    else
      return null;
  }

  public void setDeadEndStrategy(PageCrawlerDeadEndStrategy strategy) {
    deadEndStrategy = strategy;
  }

  public boolean pageExists(WikiPage context, WikiPagePath path) throws Exception {
    return getPage(context, path) != null;
  }

  public WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath) throws Exception {
    WikiPagePath fullPathOfChild;
    if (childPath.isAbsolute())
      fullPathOfChild = childPath.relativePath();
    else {
      WikiPagePath absolutePathOfParent = new WikiPagePath(parent);
      fullPathOfChild = absolutePathOfParent.append(childPath);
    }
    return fullPathOfChild;
  }

  public WikiPagePath getFullPath(WikiPage page) throws Exception {
    return new WikiPagePath(page);
  }

  public WikiPage addPage(WikiPage context, WikiPagePath path, String content) throws Exception {
    WikiPage page = addPage(context, path);
    if (page != null) {
      PageData data = new PageData(page);
      data.setContent(content);
      page.commit(data);
    }
    return page;
  }

  public WikiPage addPage(WikiPage context, WikiPagePath path) throws Exception {
    return getOrMakePage(context, path.getNames());
  }

  private WikiPage getOrMakePage(WikiPage context, List<?> namePieces) throws Exception {
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

  public String getRelativeName(WikiPage base, WikiPage page) throws Exception {
    StringBuffer qualName = new StringBuffer();
    for (WikiPage p = page; !isRoot(p) && p != base; p = p.getParent()) {
      if (p != page)
        qualName.insert(0, ".");
      qualName.insert(0, p.getName());
    }
    return qualName.toString();
  }

  //TODO this doesn't belong here
  public static WikiPage getInheritedPage(String pageName, WikiPage context) throws Exception {
    List<WikiPage> ancestors = WikiPageUtil.getAncestorsStartingWith(context);
    for (WikiPage ancestor : ancestors) {
      WikiPage namedPage = ancestor.getChildPage(pageName);
      if (namedPage != null)
        return namedPage;
    }
    return null;
  }

  public boolean isRoot(WikiPage page) throws Exception {
    WikiPage parent = page.getParent();
    return parent == null || parent == page;
  }

  public WikiPage getRoot(WikiPage page) throws Exception {
    if (isRoot(page))
      return page;
    else
      return getRoot(page.getParent());
  }

  public void traverse(WikiPage context, FitNesseTraversalListener listener) throws Exception {
    if (context.getClass() == SymbolicPage.class)
      return;
    //TODO MdM Catch any exception thrown by the following and add the page name to the Exception message.
    listener.processPage(context);
    List<?> children = context.getChildren();
    for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
      WikiPage wikiPage = (WikiPage) iterator.next();
      traverse(wikiPage, listener);
    }
  }

  /*
     Todo: RcM.  All calls to getPage should actually come here,
     and be relative to the current page, not the parent page.
     It was a gross error to have the whole wiki know that references
     were relative to the parent instead of the page.
     */
  public WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling) throws Exception {
    PageCrawler crawler = page.getPageCrawler();
    if (pathRelativeToSibling.isSubPagePath()) {
      WikiPagePath relativePath = new WikiPagePath(pathRelativeToSibling);
      relativePath.setPathMode(WikiPagePath.Mode.RELATIVE);
      return getPage(page, relativePath);
    } else if (pathRelativeToSibling.isBackwardSearchPath()) {
      String target = pathRelativeToSibling.getFirst();
      for (WikiPage current = page.getParent(); !crawler.isRoot(current); current = current.getParent()) {
        if (current.getName().equals(target))
          return getPage(current, pathRelativeToSibling.getRest());
      }
      WikiPagePath absolutePath = new WikiPagePath(pathRelativeToSibling);
      absolutePath.makeAbsolute();
      return getPage(crawler.getRoot(page), absolutePath);
    } else {
      WikiPage parent = page.getParent();
      return getPage(parent, pathRelativeToSibling);
    }
  }
}
