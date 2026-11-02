// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SymbolicLinkPruningStrategyTest {
  private WikiPage root;
  private WikiPage linkingPage;
  private WikiPage symLink;

  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("RooT");
    linkingPage = WikiPageUtil.addPage(root, PathParser.parse("LinkingPage"), "");
    WikiPage linkedPage = WikiPageUtil.addPage(root, PathParser.parse("LinkedPage"), "");
    WikiPageUtil.addPage(linkedPage, PathParser.parse("ChildPage"), "");
    symLink = new SymbolicPage("SymLink", linkedPage, linkingPage);
  }

  @Test
  public void skipsSymbolicLink() {
    assertTrue(new SymbolicLinkPruningStrategy(root).skipPageAndChildren(symLink));
  }

  @Test
  public void doesNotSkipRealPages() {
    PagePruningStrategy strategy = new SymbolicLinkPruningStrategy(root);
    assertFalse(strategy.skipPageAndChildren(root));
    assertFalse(strategy.skipPageAndChildren(linkingPage));
  }

  @Test
  public void doesNotSkipSymbolicLinkItStartsAt() {
    assertFalse(new SymbolicLinkPruningStrategy(symLink).skipPageAndChildren(symLink));
  }

  @Test
  public void skipsSymbolicLinkBelowSymbolicLinkItStartsAt() {
    WikiPage innerLink = new SymbolicPage("InnerLink", new SymbolicPage("InnerLink", root, symLink.getRealPage()), symLink);
    assertTrue(new SymbolicLinkPruningStrategy(symLink).skipPageAndChildren(innerLink));
  }

  @Test
  public void doesNotSkipChildrenOfSymbolicLinkItStartsAt() {
    WikiPage child = symLink.getChildPage("ChildPage");
    assertTrue(child.isSymbolicPage());
    assertFalse(new SymbolicLinkPruningStrategy(symLink).skipPageAndChildren(child));
  }
}
