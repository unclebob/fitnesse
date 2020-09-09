package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.MarkUpSystemV2;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MarkUpSystem {
  SyntaxTree parse(ParsingPage page, String content);
  String variableValueToHtml(ParsingPage page, String variableValue);
  void findWhereUsed(WikiPage page, Consumer<String> takeWhereUsed);
  String changeReferences(WikiPage page, Function<String, Optional<String>> changeReference);

  static MarkUpSystem make() { return new MarkUpSystemV2(); } //eventually can make different kinds of markup systems
}
