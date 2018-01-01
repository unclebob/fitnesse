package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularExpressionWikiPageFinder extends WikiPageFinder {

  private Pattern regularExpression;

  public RegularExpressionWikiPageFinder(Pattern regularExpression, TraversalListener<? super WikiPage> observer) {
    super(observer);
    this.regularExpression = regularExpression;
  }

  public RegularExpressionWikiPageFinder(String regularExpression, TraversalListener<? super WikiPage> observer) {
    this(Pattern.compile(regularExpression), observer);
  }

  @Override
  protected boolean pageMatches(WikiPage page) {
    String pageContent = page.getData().getContent();

    Matcher matcher = regularExpression.matcher(pageContent);
    return matcher.find();
  }

}
