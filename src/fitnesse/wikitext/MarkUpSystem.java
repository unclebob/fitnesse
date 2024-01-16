package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.parser.MarkUpSystemV2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MarkUpSystem {
  SyntaxTree parse(ParsingPage page, String content);
  String variableValueToHtml(ParsingPage page, String variableValue);
  void findWhereUsed(SourcePage page, Consumer<String> takeWhereUsed);
  String changeReferences(SourcePage page, Function<String, Optional<String>> changeReference);

  static MarkUpSystem make() { return new MarkUpSystemV2(); }
  static MarkUpSystem make(String content) { return MarkUpSystems.STORE.make(content); }

  static Map<String, String> listVariables(WikiPage page) {
    Map<String, String> listVariables = new HashMap<>();
    ParsingPage parsingPage = new ParsingPage(new WikiSourcePage(page));
    String content = page.getData().getContent();
    MarkUpSystem.make(content).parse(parsingPage, content);
    for (String listVariable : parsingPage.listVariables()) {
      listVariables.put(listVariable, parsingPage.findVariable(listVariable).orElse(""));
    }
    return listVariables;
  }
}
