package fitnesse.wikitext.parser;

public class ParsedPage {
  public ParsedPage(ParsingPage parsingPage, String content) {
    this.parsingPage = parsingPage;
    this.content = content;
  }

  public ParsedPage(ParsedPage sourceParsedPage, String content) {
    parsingPage = sourceParsedPage.getParsingPage().copy();
    this.content = content;
  }

  public Symbol getSyntaxTree() {
    parseContent();
    return syntaxTree;
  }

  public ParsingPage getParsingPage() {
      parseContent();
      return parsingPage;
  }

  public void addToFront(ParsedPage addFromPage) {
    getSyntaxTree().addToFront(addFromPage.getSyntaxTree().childAt(0));
  }

  public String toHtml() {
    return new HtmlTranslator(getParsingPage().getPage(), getParsingPage()).translateTree(getSyntaxTree());
  }

  private void parseContent() {
    if (syntaxTree == null) {
        syntaxTree = Parser.make(parsingPage, content).parse();
    }
  }

  private final String content;
  private final ParsingPage parsingPage;

  private Symbol syntaxTree = null;
}
