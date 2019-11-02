package fitnesse.wiki;

public interface PagePruningStrategy {
    boolean skipPageAndChildren(WikiPage page);
}
