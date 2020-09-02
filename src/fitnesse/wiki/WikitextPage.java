package fitnesse.wiki;

import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.SyntaxTree;

/**
 * This interface denotes a class that can expose parsed wiki page content.
 */
public interface WikitextPage {
  SyntaxTree getSyntaxTree();

  ParsingPage getParsingPage();
}
