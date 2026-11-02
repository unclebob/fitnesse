package fitnesse.wiki;

public interface PagePruningStrategy {
    boolean skipPageAndChildren(WikiPage page);

    /**
     * @return true to leave out symbolic links (and the pages below them) without resolving them.
     */
    default boolean skipSymbolicLinks() {
        return false;
    }
}
