package fitnesse.wiki.refactoring;

import java.util.regex.Pattern;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class ContentReplacingSearchObserver implements TraversalListener<WikiPage>, ContentReplacer {

  private Pattern searchPattern;

  private String replacement;

  public ContentReplacingSearchObserver(String searchPattern, String replacement) {
    this.searchPattern = Pattern.compile(searchPattern);
    this.replacement = replacement;
  }

  @Override
  public void process(WikiPage page) {
    PageData pageData = page.getData();
    String replacedContent = replace(pageData.getContent());

    pageData.setContent(replacedContent);
    page.commit(pageData);
  }

  @Override
  public String replace(String content) {
    return searchPattern.matcher(content).replaceAll(replacement);
  }

}
