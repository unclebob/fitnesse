package fitnesse.wiki;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.VariableSource;

public class PageVariableSource implements VariableSource {

  private final WikiPage page;

  public PageVariableSource(WikiPage page) {
    this.page = page;
  }

  @Override
  public Maybe<String> findVariable(String key) {
    String value;
    if (key.equals("RUNNING_PAGE_NAME"))
      value = page.getName();
    else if (key.equals("RUNNING_PAGE_PATH"))
      value = page.getPageCrawler().getFullPath().parentPath().toString();
    else
      return Maybe.noString;

    return new Maybe<>(value);
  }
}
