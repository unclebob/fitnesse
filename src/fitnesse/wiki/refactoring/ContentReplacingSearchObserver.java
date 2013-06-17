package fitnesse.wiki.refactoring;

import java.util.regex.Pattern;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class ContentReplacingSearchObserver implements TraversalListener<WikiPage> {

  private Pattern searchPattern;

  private String replacement;

  public ContentReplacingSearchObserver(String searchPattern, String replacement) {
    this.searchPattern = Pattern.compile(searchPattern);
    this.replacement = replacement;
  }

  public void process(WikiPage page) {
    PageData pageData = page.getData();
    String replacedContent = searchPattern.matcher(pageData.getContent()).replaceAll(replacement);

    pageData.setContent(replacedContent);
    page.commit(pageData);
  }

}
