package fitnesse.wiki.search;

import fitnesse.wiki.WikiPage;

public interface PageFinder {

  abstract void search(WikiPage page);

}