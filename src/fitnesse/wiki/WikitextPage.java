package fitnesse.wiki;

import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;

/**
 * This interface denotes a class that can expose parsed wiki page content,
 */
public interface WikitextPage {
  Symbol getSyntaxTree();

  ParsingPage getParsingPage();
}
