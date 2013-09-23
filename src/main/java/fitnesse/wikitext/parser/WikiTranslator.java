package fitnesse.wikitext.parser;

import java.util.HashMap;

public class WikiTranslator extends Translator {
    private static final HashMap<SymbolType, Translation> translations;

    static {
        translations = new HashMap<SymbolType, Translation>();
        addTranslation(Alias.symbolType, new WikiBuilder().text("[[").children("][").text("]]"));
        addTranslation(Link.symbolType, new WikiBuilder().property("image", "", "!img ")
                .property("image", "left", "!img-l ").property("image", "right", "!img-r ").content().child(0));
        addTranslation(Literal.symbolType, new WikiBuilder().text("!-").content().text("-!"));
        addTranslation(Path.symbolType, new WikiBuilder().text("!path ").child(0));
        addTranslation(Preformat.symbolType, new WikiBuilder().text("{{{").child(0).text("}}}"));
        addTranslation(Variable.symbolType, new WikiBuilder().text("${").child(0).text("}"));
    }

    private static void addTranslation(SymbolType symbolType, Translation translation) {
        translations.put(symbolType, translation);
    }

    public WikiTranslator(SourcePage page) {
        super(page);
    }

    @Override
    protected Translation getTranslation(SymbolType symbolType) {
        return translations.get(symbolType);
    }
}
