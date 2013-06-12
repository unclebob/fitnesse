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

  public WikiPage getPage(WikiPage context, WikiPagePath path) {
    assert context == this.context;
    return getPage(context, path, null);
  }

  public WikiPage getPage(WikiPage context, WikiPagePath path, PageCrawlerDeadEndStrategy deadEndStrategy) {
    if (path == null)
      return null;

    if (isRoot(path))
      return _getRoot(context);

    if (path.isEmpty())
      return context;

    if (path.isAbsolute()) {
      WikiPagePath relativeToRoot = new WikiPagePath(path);
      relativeToRoot.setPathMode(WikiPagePath.Mode.RELATIVE);
      return getPage(_getRoot(context), relativeToRoot, deadEndStrategy);
    } else if (path.isBackwardSearchPath())
      return getSiblingPage(context, path);

    String firstPathElement = path.getFirst();
    WikiPagePath restOfPath = path.getRest();

    WikiPage childPage = context.getChildPage(firstPathElement);
    if (childPage != null)
      return getPage(childPage, restOfPath, deadEndStrategy);
    else
      return getPageAfterDeadEnd(context, firstPathElement, restOfPath, deadEndStrategy);
  }

  private boolean isRoot(WikiPagePath path) {
    return path.isAbsolute() && path.isEmpty();
  }

  protected WikiPage getPageAfterDeadEnd(WikiPage context, String first, WikiPagePath rest, PageCrawlerDeadEndStrategy deadEndStrategy) {
    rest.addNameToFront(first);
    if (deadEndStrategy != null)
      return deadEndStrategy.getPageAfterDeadEnd(context, rest, this);
    else
      return null;
  }

  public boolean pageExists(WikiPage context, WikiPagePath path) {
    assert context == this.context;
    return getPage(context, path) != null;
  }

  public WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath) {
    assert parent == this.context;
    WikiPagePath fullPathOfChild;
    if (childPath.isAbsolute())
      fullPathOfChild = childPath.relativePath();
    else {
      WikiPagePath absolutePathOfParent = new WikiPagePath(parent);
      fullPathOfChild = absolutePathOfParent.append(childPath);
    }
    return fullPathOfChild;
  }

  public WikiPagePath getFullPath(WikiPage page) {
    assert page == this.context;
    return new WikiPagePath(page);
  }

  public String getRelativeName(WikiPage base, WikiPage page) {
    assert base == this.context;
    StringBuffer qualName = new StringBuffer();
    for (WikiPage p = page; !_isRoot(p) && !p.equals(base); p = p.getParent()) {
      if (p != page)
        qualName.insert(0, ".");
      qualName.insert(0, p.getName());
    }
    return qualName.toString();
  }

  public WikiPage getClosestInheritedPage(WikiPage context, String pageName) {
    assert context == this.context;
    List<WikiPage> ancestors = getAncestorsStartingWith(context);
    for (WikiPage ancestor : ancestors) {
      WikiPage namedPage = ancestor.getChildPage(pageName);
      if (namedPage != null)
        return namedPage;
    }
    return null;
  }

  public boolean isRoot(WikiPage page) {
    assert page == this.context;
    return _isRoot(page);
  }

  public boolean _isRoot(WikiPage page) {
    WikiPage parent = page.getParent();
    return parent == null || parent == page;
  }

  public WikiPage getRoot(WikiPage page) {
    assert page == this.context;
    return _getRoot(page);
  }

  private WikiPage _getRoot(WikiPage page) {
    if (_isRoot(page))
      return page;
    else
      return _getRoot(page.getParent());
  }

  public void traverse(WikiPage context, TraversalListener<? super WikiPage> listener) {
    assert context == this.context;
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
  public WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling) {
    assert page == this.context;
    PageCrawler crawler = page.getPageCrawler();
    if (pathRelativeToSibling.isSubPagePath()) {
      WikiPagePath relativePath = new WikiPagePath(pathRelativeToSibling);
      relativePath.setPathMode(WikiPagePath.Mode.RELATIVE);
      return getPage(page, relativePath);
    } else if (pathRelativeToSibling.isBackwardSearchPath()) {
      String target = pathRelativeToSibling.getFirst();
      WikiPage ancestor = findAncestorWithName(page, target);
      if (ancestor != null) return ancestor.getPageCrawler().getPage(ancestor, pathRelativeToSibling.getRest());

      WikiPagePath absolutePath = new WikiPagePath(pathRelativeToSibling);
      absolutePath.makeAbsolute();
      WikiPage root = crawler.getRoot(page);
      return root.getPageCrawler().getPage(root, absolutePath);
    } else {
      WikiPage parent = page.getParent();
      return parent.getPageCrawler().getPage(parent, pathRelativeToSibling);
    }
  }

  public WikiPage findAncestorWithName(WikiPage page, String name) {
    assert page == this.context;
    for (WikiPage current = page.getParent(); !_isRoot(current); current = current.getParent()) {
      if (current.getName().equals(name)) return current;
    }
    return null;
  }

  public List<WikiPage> getAllUncles(WikiPage context, String uncleName) {
    assert context == this.context;
    List<WikiPage> uncles = new ArrayList<WikiPage>();
    List<WikiPage> ancestors = getAncestorsStartingWith(context);
    for (WikiPage ancestor : ancestors) {
      WikiPage namedPage = ancestor.getChildPage(uncleName);
      if (namedPage != null)
        uncles.add(namedPage);
    }
    return uncles;
  }

  public List<WikiPage> getAncestorsOf(WikiPage page) {
    assert page == this.context;
    LinkedList<WikiPage> ancestors = new LinkedList<WikiPage>();
    WikiPage parent = page;
    do {
      parent = parent.getParent();
      ancestors.add(parent);
    } while (!_isRoot(parent));

    return ancestors;
  }

  public List<WikiPage> getAncestorsStartingWith(WikiPage page) {
    assert page == this.context;
    LinkedList<WikiPage> ancestors = (LinkedList<WikiPage>)getAncestorsOf(page);
    ancestors.addFirst(page);
    return ancestors;
  }
}
