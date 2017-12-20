// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

public class PageCrawler {

  private final WikiPage context;

  public PageCrawler(WikiPage context) {
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
    StringBuilder name = new StringBuilder();
    for (WikiPage p = page; !p.isRoot() && !p.equals(context); p = p.getParent()) {
      if (p != page)
        name.insert(0, ".");
      name.insert(0, p.getName());
    }
    return name.toString();
  }

  public WikiPage getClosestInheritedPage(final String pageName) {
    final WikiPage[] foundPage = new WikiPage[1];
    traversePageAndAncestors(new TraversalListener<WikiPage>() {
      @Override
      public void process(WikiPage page) {
        WikiPage namedPage = page.getChildPage(pageName);
        if (namedPage != null && foundPage[0] == null)
          foundPage[0] = namedPage;
      }
    });
    return foundPage[0];
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

  public void traverse(TraversalListener<? super WikiPage> listener, PagePruningStrategy strategy) {
    traverse(this.context, listener, strategy);
  }

  private void traverse(WikiPage page, TraversalListener<? super WikiPage> listener, PagePruningStrategy pruningStrategy) {
    // skip a page and its children if the pruning strategy says so:
    if(pruningStrategy.skipPageAndChildren(page)){ return; }

    listener.process(page);
    for (WikiPage wikiPage : page.getChildren()) {
      traverse(wikiPage, listener, pruningStrategy);
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

  public void traverseUncles(final String uncleName, final TraversalListener<? super WikiPage> callback) {
    traversePageAndAncestors(new TraversalListener<WikiPage>() {
      @Override
      public void process(WikiPage page) {
        WikiPage namedPage = page.getChildPage(uncleName);
        if (namedPage != null)
          callback.process(namedPage);
      }
    });
  }

  public void traversePageAndAncestors(TraversalListener<? super WikiPage> callback) {
    WikiPage page = context;
    while (!page.isRoot()) {
      callback.process(page);
      page = page.getParent();
    }
    // Call once more for root page.
    callback.process(page);
  }
}
