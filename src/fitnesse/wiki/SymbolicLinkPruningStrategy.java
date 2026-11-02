// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

/**
 * Leaves out symbolic links (and everything below them) found while traversing from a start page.
 * The start page itself is never left out, so traversals that start at a symbolic link still
 * cover the linked pages.
 */
public class SymbolicLinkPruningStrategy implements PagePruningStrategy {

  @Override
  public boolean skipPageAndChildren(WikiPage page) {
    return false;
  }

  @Override
  public boolean skipSymbolicLinks() {
    return true;
  }
}
