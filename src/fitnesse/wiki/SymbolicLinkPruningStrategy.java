// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

/**
 * Skips symbolic links (and everything below them) found while traversing from a start page.
 * The start page itself is never skipped, so traversals that start inside a linked tree still
 * cover that tree.
 */
public class SymbolicLinkPruningStrategy implements PagePruningStrategy {
  private final WikiPage startPage;

  public SymbolicLinkPruningStrategy(WikiPage startPage) {
    this.startPage = startPage;
  }

  @Override
  public boolean skipPageAndChildren(WikiPage page) {
    return page != startPage && SymbolicPage.isSymbolicLink(page);
  }
}
