// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

public class PageCrawlerImpl implements PageCrawler {

  private final WikiPage context;

  protected PageCrawlerImpl(WikiPage context) {
    this.context = context;
  }

  public WikiPage getPage(WikiPagePath path) {
    return getPage(path, null);
  }

  public WikiPage getPage(WikiPagePath path, PageCrawlerDeadEndStrategy deadEndStrategy) {
    return getPage(context, path, deadEndStrategy);
  }

  private  WikiPage getPage(WikiPage page, WikiPagePath path, PageCrawlerDeadEndStrategy deadEndStrategy) {
    if (path == null)
      return null;

    if (isRoot(path))
      return getRoot(page);

    if (path.isEmpty())
      return page;

    if (path.isAbsolute()) {
      WikiPagePath relativeToRoot = new WikiPagePath(path);
      relativeToRoot.setPathMode(WikiPagePath.Mode.RELATIVE);
      return getPage(getRoot(page), relativeToRoot, deadEndStrategy);
    } else if (path.isBackwardSearchPath())
      return getSiblingPage(page, path);

    String firstPathElement = path.getFirst();
    WikiPagePath restOfPath = path.getRest();

    WikiPage childPage = page.getChildPage(firstPathElement);
    if (childPage != null)
      return getPage(childPage, restOfPath, deadEndStrategy);
    else
      return getPageAfterDeadEnd(page, firstPathElement, restOfPath, deadEndStrategy);
  }

  private boolean isRoot(WikiPagePath path) {
    return path.isAbsolute() && path.isEmpty();
  }

  private WikiPage getPageAfterDeadEnd(WikiPage page, String first, WikiPagePath rest, PageCrawlerDeadEndStrategy deadEndStrategy) {
    rest.addNameToFront(first);
    if (deadEndStrategy != null)
      return deadEndStrategy.getPageAfterDeadEnd(page, rest, this);
    else
      return null;
  }

  public boolean pageExists(WikiPagePath path) {
    return getPage(path) != null;
  }

  public WikiPagePath getFullPathOfChild(WikiPagePath childPath) {
    WikiPagePath fullPathOfChild;
    if (childPath.isAbsolute())
      fullPathOfChild = childPath.relativePath();
    else {
      WikiPagePath absolutePathOfParent = new WikiPagePath(context);
      fullPathOfChild = absolutePathOfParent.append(childPath);
    }
    return fullPathOfChild;
  }

  public WikiPagePath getFullPath() {
    return new WikiPagePath(context);
  }

  public String getRelativeName(WikiPage page) {
    StringBuffer qualName = new StringBuffer();
    for (WikiPage p = page; !p.isRoot() && !p.equals(context); p = p.getParent()) {
      if (p != page)
        qualName.insert(0, ".");
      qualName.insert(0, p.getName());
    }
    return qualName.toString();
  }

  public WikiPage getClosestInheritedPage(WikiPage context, String pageName) {
    assert context == this.context;
    List<WikiPage> ancestors = getAncestorsStartingWith();
    for (WikiPage ancestor : ancestors) {
      WikiPage namedPage = ancestor.getChildPage(pageName);
      if (namedPage != null)
        return namedPage;
    }
    return null;
  }

  public WikiPage getRoot() {
    return getRoot(context);
  }

  private WikiPage getRoot(WikiPage page) {
    if (page.isRoot())
      return page;
    else
      return getRoot(page.getParent());
  }

  public void traverse(TraversalListener<? super WikiPage> listener) {
    _traverse(context, listener);
  }

  public void _traverse(WikiPage context, TraversalListener<? super WikiPage> listener) {
    if (context.getClass() == SymbolicPage.class)
      return;
    //TODO MdM Catch any exception thrown by the following and add the page name to the Exception message.
    listener.process(context);
    List<?> children = context.getChildren();
    for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
      WikiPage wikiPage = (WikiPage) iterator.next();
      _traverse(wikiPage, listener);
    }
  }

  /*
     Todo: RcM.  All calls to getPage should actually come here,
     and be relative to the current page, not the parent page.
     It was a gross error to have the whole wiki know that references
     were relative to the parent instead of the page.
     */
  public WikiPage getSiblingPage(WikiPagePath pathRelativeToSibling) {
    return getSiblingPage(context, pathRelativeToSibling);
  }

  private WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling) {
    if (pathRelativeToSibling.isSubPagePath()) {
      WikiPagePath relativePath = new WikiPagePath(pathRelativeToSibling);
      relativePath.setPathMode(WikiPagePath.Mode.RELATIVE);
      return getPage(relativePath);
    } else if (pathRelativeToSibling.isBackwardSearchPath()) {
      String target = pathRelativeToSibling.getFirst();
      WikiPage ancestor = findAncestorWithName(target);
      if (ancestor != null) return getPage(ancestor, pathRelativeToSibling.getRest(), null);

      WikiPagePath absolutePath = new WikiPagePath(pathRelativeToSibling);
      absolutePath.makeAbsolute();
      WikiPage root = getRoot(page);
      return getPage(root, absolutePath, null);
    } else {
      WikiPage parent = page.getParent();
      return getPage(parent, pathRelativeToSibling, null);
    }
  }

  public WikiPage findAncestorWithName(String name) {
    for (WikiPage current = context.getParent(); !current.isRoot(); current = current.getParent()) {
      if (current.getName().equals(name)) return current;
    }
    return null;
  }

  public List<WikiPage> getAllUncles(String uncleName) {
    List<WikiPage> uncles = new ArrayList<WikiPage>();
    List<WikiPage> ancestors = getAncestorsStartingWith();
    for (WikiPage ancestor : ancestors) {
      WikiPage namedPage = ancestor.getChildPage(uncleName);
      if (namedPage != null)
        uncles.add(namedPage);
    }
    return uncles;
  }

  public List<WikiPage> getAncestorsOf() {
    LinkedList<WikiPage> ancestors = new LinkedList<WikiPage>();
    WikiPage parent = context;
    do {
      parent = parent.getParent();
      ancestors.add(parent);
    } while (!parent.isRoot());

    return ancestors;
  }

  public List<WikiPage> getAncestorsStartingWith() {
    LinkedList<WikiPage> ancestors = (LinkedList<WikiPage>)getAncestorsOf();
    ancestors.addFirst(context);
    return ancestors;
  }
}
