package fitnesse.wikitext.parser;

public class HtmlTranslator extends Translator {

    private ParsingPage parsingPage;

    @Override
    protected Translation getTranslation(SymbolType symbolType) {
        return symbolType.getHtmlTranslation();
    }

  @Override
  protected Translation getTranslation(Symbol symbol) {
    Translation translation = super.getTranslation(symbol);
    if (translation != null) {
      symbol.getType().applyParsedSymbolDecorations(symbol, parsingPage);
    }
    return translation;
  }

  public ParsingPage getParsingPage() { return parsingPage; }

    public HtmlTranslator(SourcePage currentPage, ParsingPage parsingPage) {
        super(currentPage);
        this.parsingPage = parsingPage;
    }
}
