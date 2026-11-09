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
    return page != startPage && page.isSymbolicLink();
  }
}
