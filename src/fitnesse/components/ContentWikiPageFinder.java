package fitnesse.components;

import fitnesse.wiki.WikiPage;

public class ContentWikiPageFinder extends WikiPageFinder {

  private String searchString;

  public ContentWikiPageFinder(String searchString, SearchObserver observer) {
    super(observer);
    this.searchString = searchString.toLowerCase();
  }

  @Override
  protected boolean pageMatches(WikiPage page) throws Exception {
    String content = page.getData().getContent().toLowerCase();

    boolean matches = content.indexOf(searchString) != -1;
    return matches;
  }

}
