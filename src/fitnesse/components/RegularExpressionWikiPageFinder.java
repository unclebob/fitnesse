package fitnesse.components;

import static java.util.regex.Pattern.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wiki.WikiPage;

public class RegularExpressionWikiPageFinder extends WikiPageFinder {

  private Pattern regularExpression;

  public RegularExpressionWikiPageFinder(Pattern regularExpression, TraversalListener observer) {
    super(observer);
    this.regularExpression = regularExpression;
  }

  public RegularExpressionWikiPageFinder(String regularExpression, TraversalListener observer) {
    super(observer);
    this.regularExpression = Pattern.compile(regularExpression);
  }

  protected boolean pageMatches(WikiPage page) {
    String pageContent = page.getData().getContent();

    Matcher matcher = regularExpression.matcher(pageContent);
    return matcher.find();
  }

}
