package fitnesse.wikitext;

import fitnesse.wikitext.parser.Maybe;

import java.util.function.Consumer;

public interface SyntaxTree {
  String translateToHtml();

  Maybe<String> findVariable(String name);
  void findPaths(Consumer<String> takePath);
  void findXrefs(Consumer<String> takeXref);
}
