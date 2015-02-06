package fitnesse.wiki;

import java.io.File;

import fitnesse.wikitext.parser.VariableSource;

public interface WikiPageFactory<T extends WikiPage> {

  WikiPage makePage(File path, String pageName, T parent, VariableSource variableSource);

  boolean supports(File path);
}
