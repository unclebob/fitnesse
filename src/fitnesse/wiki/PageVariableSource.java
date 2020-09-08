package fitnesse.wiki;

import fitnesse.wikitext.VariableSource;

import java.util.Optional;

public class PageVariableSource implements VariableSource {

  private final WikiPage page;

  public PageVariableSource(WikiPage page) {
    this.page = page;
  }

  @Override
  public Optional<String> findVariable(String key) {
    String value;
    if (key.equals("RUNNING_PAGE_NAME"))
      value = page.getName();
    else if (key.equals("RUNNING_PAGE_PATH"))
      value = page.getFullPath().parentPath().toString();
    else
      return Optional.empty();

    return Optional.of(value);
  }
}
