package fitnesse.wikitext.parser;

public class HtmlTranslator extends Translator {

    private ParsingPage parsingPage;

    @Override
    protected Translation getTranslation(SymbolType symbolType) {
        return symbolType.getHtmlTranslation();
    }

    public ParsingPage getParsingPage() { return parsingPage; }

    public HtmlTranslator(SourcePage currentPage, ParsingPage parsingPage) {
        super(currentPage);
        this.parsingPage = parsingPage;
    }
}
