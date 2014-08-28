package fitnesse.wiki.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

public class RegularExpressionWikiPageFinder extends WikiPageFinder {

  private Pattern regularExpression;

  public RegularExpressionWikiPageFinder(Pattern regularExpression, TraversalListener<? super WikiPage> observer) {
    super(observer);
    this.regularExpression = regularExpression;
  }

  public RegularExpressionWikiPageFinder(String regularExpression, TraversalListener<? super WikiPage> observer) {
    super(observer);
    this.regularExpression = Pattern.compile(regularExpression);
  }

  protected boolean pageMatches(WikiPage page) {
    String pageContent = page.getData().getContent();

    Matcher matcher = regularExpression.matcher(pageContent);
    return matcher.find();
  }

}
