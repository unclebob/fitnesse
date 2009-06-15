package fitnesse.components;

import fitnesse.wiki.WikiPage;

public class TitleWikiPageFinder extends WikiPageFinder {

  private String searchString;

  public TitleWikiPageFinder(String searchString, SearchObserver observer) {
    super(observer);
    this.searchString = searchString.toLowerCase();
  }

  @Override
  protected boolean pageMatches(WikiPage page) throws Exception {
    String content =  page.getName().toLowerCase();

    boolean matches = content.indexOf(searchString) != -1;
    return matches;
  }

}
