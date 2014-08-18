package fitnesse.wikitext.parser;

public class ParsedPage {
  public ParsedPage(ParsingPage parsingPage, String content) {
    this.parsingPage = parsingPage;
    this.content = content;
  }

  public Symbol getSyntaxTree() {
    if (syntaxTree == null) {
      syntaxTree = Parser.make(parsingPage, content).parse();
    }
    return syntaxTree;
  }

  public ParsingPage getParsingPage() {
    getSyntaxTree();
    return parsingPage;
  }

  public String toHtml() {
    return new HtmlTranslator(getParsingPage().getPage(), getParsingPage()).translateTree(getSyntaxTree());
  }

  private final String content;
  private final ParsingPage parsingPage;

  private Symbol syntaxTree = null;
}
