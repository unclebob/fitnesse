package fitnesse.wiki;

import java.io.File;

import fitnesse.wikitext.parser.VariableSource;

public interface WikiPageFactory {

  /**
   * Create a new page, based on the information provided.
   *
   * @param path
   * @param pageName
   * @param parent
   * @param variableSource
   * @return a new wiki page or null if parent is not null no page exists
   */
  WikiPage makePage(File path, String pageName, WikiPage parent, VariableSource variableSource);

  boolean supports(File path);
}
