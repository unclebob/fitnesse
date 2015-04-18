package fitnesse.wikitext.parser;

/**
 *
 * @author user
 */
class FirstTitleTranslation extends HeaderLine {

    public FirstTitleTranslation() {
        super();
    }

    public String toTarget(Translator translator, Symbol symbol) {
        ((FirstTitleTranslator)translator).setFirstTitle(translator.translate(symbol.childAt(0)));
        return "";
    }
}
