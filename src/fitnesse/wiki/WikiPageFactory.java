package fitnesse.wiki;

import java.io.File;

import fitnesse.wikitext.parser.VariableSource;

public interface WikiPageFactory {

  WikiPage makePage(File path, String pageName, WikiPage parent, VariableSource variableSource);

  boolean supports(File path);
}
