package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

public class TitleWikiPageFinder extends WikiPageFinder {

  private String searchString;

  public TitleWikiPageFinder(String searchString, TraversalListener<? super WikiPage> observer) {
    super(observer);
    this.searchString = searchString.toLowerCase();
  }

  @Override
  protected boolean pageMatches(WikiPage page) {
    String content =  page.getName().toLowerCase();
    return content.contains(searchString);
  }

}
