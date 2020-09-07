package fitnesse.wikitext;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.ParsingPage;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SyntaxTree {
  void parse(String input, ParsingPage parsingPage);

  String translateToHtml();
  String translateToMarkUp();

  Maybe<String> findVariable(String name);
  void findPaths(Consumer<String> takePath);
  void findXrefs(Consumer<String> takeXref);
  void findWhereUsed(Consumer<String> takeWhereUsed);
  void findReferences(Function<String, Optional<String>> changeReference);
}
