package fitnesse.wikitext.parser;

public class HtmlTranslator extends Translator {

    private ParsingPage parsingPage;
    private String testSystem;

    @Override
    protected Translation getTranslation(SymbolType symbolType) {
        if(symbolType instanceof SymbolTypeDecorator){
            SymbolType applicable = ((SymbolTypeDecorator)symbolType).isApplicable(this);
            return applicable.getHtmlTranslation();
        }
        return symbolType.getHtmlTranslation();
    }

    public ParsingPage getParsingPage() { return parsingPage; }

    public String getTestSystem() { return testSystem; }

    public HtmlTranslator(SourcePage currentPage, ParsingPage parsingPage) {
        this(currentPage, parsingPage, "fit");
    }

    public HtmlTranslator(SourcePage currentPage, ParsingPage parsingPage, String testSystem) {
        super(currentPage);
        this.parsingPage = parsingPage;
        this.testSystem = testSystem;
    }
}
