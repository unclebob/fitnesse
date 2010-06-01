package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.SourcePage;
import fitnesse.wikitext.parser.SymbolType;

import java.util.HashMap;

public class WikiTranslator extends Translator {
    private static final HashMap<SymbolType, Translation> translations;

    static {
        translations = new HashMap<SymbolType, Translation>();
        addTranslation(SymbolType.Alias, new WikiBuilder().text("[[").children("][").text("]]"));
        addTranslation(SymbolType.Link, new WikiBuilder().property("image", "", "!img ")
                .property("image", "left", "!img-l ").property("image", "right", "!img-r ").content().child(0));
        addTranslation(SymbolType.Literal, new WikiBuilder().text("!-").content().text("-!"));
        addTranslation(SymbolType.Path, new WikiBuilder().text("!path ").child(0));
        addTranslation(SymbolType.Preformat, new WikiBuilder().text("{{{").content().text("}}}"));
    }

    private static void addTranslation(SymbolType symbolType, Translation translation) {
        translations.put(symbolType, translation);
    }

    public WikiTranslator(SourcePage page) {
        super(page);
    }

    @Override
    protected HashMap<SymbolType, Translation> getTranslations() {
        return translations;
    }
}
