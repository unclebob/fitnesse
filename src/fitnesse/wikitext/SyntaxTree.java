package fitnesse.wikitext;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.ParsingPage;

import java.util.function.Consumer;

public interface SyntaxTree {
  void parse(String input, ParsingPage parsingPage);

  String translateToHtml();
  Maybe<String> findVariable(String name);
  void findPaths(Consumer<String> takePath);
  void findXrefs(Consumer<String> takeXref);
  void findWhereUsed(Consumer<String> takeWhereUsed);
}
