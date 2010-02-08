package fitnesse.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wiki.WikiPage;

public class RegularExpressionWikiPageFinder extends WikiPageFinder {

  private Pattern regularExpression;

  public RegularExpressionWikiPageFinder(String string,
      SearchObserver observer) {
    super(observer);
    regularExpression = Pattern.compile(string);
  }

  @Override
  protected boolean pageMatches(WikiPage page) throws Exception {
    String pageContent = page.getData().getContent();

    Matcher matcher = regularExpression.matcher(pageContent);
    return matcher.matches();
  }

}
