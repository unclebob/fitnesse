package fitnesse.wikitext.parser;

public class FirstTitleTranslator extends HtmlTranslator {

    private boolean capturedFirstTitle = false;
    private String firstTitle;
    
    public String setFirstTitle(String value){
        if (!capturedFirstTitle){
            firstTitle = value;
            capturedFirstTitle = true;
        }
        return value;
    }
    
    public String getFirstTitle(){
        return firstTitle;
    }
    
    @Override
    protected Translation getTranslation(SymbolType symbolType) {
        Translation result = super.getTranslation(symbolType);
        if (symbolType instanceof HeaderLine){
            result = new FirstTitleTranslation();
        }
        return result;
    }

    public FirstTitleTranslator(SourcePage currentPage, ParsingPage parsingPage) {
        super(currentPage, parsingPage);
    }

    public String translate(Symbol symbol) {
        if (capturedFirstTitle){
            return "";
        } else {
            return super.translate(symbol);
        }
    }

}
