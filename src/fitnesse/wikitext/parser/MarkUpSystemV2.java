package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.SyntaxTree;
import fitnesse.wikitext.MarkUpSystem;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class MarkUpSystemV2 implements MarkUpSystem {
  @Override
  public SyntaxTree parse(ParsingPage page, String content) {
    SyntaxTreeV2 syntaxTree = new SyntaxTreeV2();
    syntaxTree.parse(content, page);
    return syntaxTree;
  }

  @Override
  public String variableValueToHtml(ParsingPage page, String variableValue) {
    SyntaxTreeV2 tree = new SyntaxTreeV2(SymbolProvider.variableDefinitionSymbolProvider);
    tree.parse(variableValue, page);
    return tree.translateToHtml();
  }

  @Override
  public void findWhereUsed(WikiPage page, Consumer<String> takeWhereUsed) {
    SyntaxTreeV2 syntaxTree = new SyntaxTreeV2(SymbolProvider.refactoringProvider);
    syntaxTree.parse(page.getData().getContent(), new ParsingPage(new WikiSourcePage(page)));
    syntaxTree.findWhereUsed(takeWhereUsed);
  }

  @Override
  public String changeReferences(WikiPage page, Function<String, Optional<String>> changeReference) {
    SyntaxTreeV2 syntaxTree = new SyntaxTreeV2(SymbolProvider.refactoringProvider);
    syntaxTree.parse(page.getData().getContent(), new ParsingPage(new WikiSourcePage(page)));
    syntaxTree.findReferences(changeReference);
    return syntaxTree.translateToMarkUp();
  }
}
