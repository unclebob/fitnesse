package fitnesse.wikitext;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.ParsingPage;

import java.util.List;

public interface SyntaxTree {
  void parse(String input, ParsingPage parsingPage);

  String getHtml();
  Maybe<String> findVariable(String name);
  List<String> findPaths();
  List<String> findXrefs();
}
