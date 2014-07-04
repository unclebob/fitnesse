package fitnesse.wiki;

import fitnesse.wikitext.parser.ParsedPage;

/**
 * This interface denotes a class that can expose parsed wiki page content,
 */
public interface ParsablePage {
  ParsedPage getParsedPage();
}
