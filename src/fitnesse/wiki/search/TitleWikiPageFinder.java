package fitnesse.wiki.search;

import java.util.regex.Pattern;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

public class TitleWikiPageFinder extends WikiPageFinder {

  private final Pattern pattern;

  public TitleWikiPageFinder(String searchString, TraversalListener<? super WikiPage> observer) {
    this(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL), observer);
  }

  public TitleWikiPageFinder(Pattern pattern, TraversalListener<? super WikiPage> observer) {
    super(observer);
    this.pattern = pattern;
  }

  @Override
  protected boolean readsPageData() {
    return false;
  }

  @Override
  protected boolean pageMatches(WikiPage page) {
    return pattern.matcher(page.getName()).find();
  }

}
