package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.LineRule;
import fitnesse.wikitext.parser.SourcePage;
import fitnesse.wikitext.parser.SymbolType;

import java.util.HashMap;

public class WikiTranslator extends Translator {
    private static final HashMap<SymbolType, Translation> translations;

    static {
        translations = new HashMap<SymbolType, Translation>();
        translations.put(SymbolType.Alias, new WikiBuilder().text("[[").children("][").text("]]"));
        translations.put(SymbolType.Link, new WikiBuilder().property("image", "", "!img ")
                .property("image", "left", "!img-l ").property("image", "right", "!img-r ").content().child(0));
        translations.put(SymbolType.Literal, new WikiBuilder().text("!-").content().text("-!"));
        translations.put(SymbolType.Path, new WikiBuilder().text("!path ").child(0));
        translations.put(SymbolType.Preformat, new WikiBuilder().text("{{{").content().text("}}}"));
        /*translations.put(SymbolType.Alias, new WikiBuilder().text("[[").children("][").text("]]"));
        translations.put(SymbolType.AnchorName, new WikiBuilder().text("!anchor ").child(0));
        translations.put(SymbolType.AnchorReference, new WikiBuilder().text(".#").child(0));
        translations.put(SymbolType.Bold, new WikiBuilder().text("'''").child(0).text("'''"));
        translations.put(SymbolType.CenterLine, new WikiBuilder().text("!c ").child(0));
        translations.put(SymbolType.Collapsible, new WikiBuilder().text("!*").property("State", "Closed", ">")
                .property("State", "Invisible", "<").text(" ").child(0).text("\n").child(1).text("\n*!"));
        translations.put(SymbolType.Comment, new WikiBuilder().text("#").child(0));
        translations.put(SymbolType.Evaluator, new WikiBuilder().text("${=").child(0).text("=}"));
        translations.put(SymbolType.HashRow, new WikiBuilder().children(":"));
        translations.put(SymbolType.HashTable, new WikiBuilder().text("!{").children(",").text("}"));
        translations.put(SymbolType.HeaderLine, new WikiBuilder().text("!").property(LineRule.Level).text(" ").child(0));
        translations.put(SymbolType.Italic, new WikiBuilder().text("''").child(0).text("''"));
        translations.put(SymbolType.Meta, new WikiBuilder().text("!meta ").child(0));
        translations.put(SymbolType.NoteLine, new WikiBuilder().text("!note ").child(0));
        translations.put(SymbolType.Strike, new WikiBuilder().text("--").child(0).text("--"));*/
    }

    public WikiTranslator(SourcePage page) {
        super(page);
    }

    @Override
    protected HashMap<SymbolType, Translation> getTranslations() {
        return translations;
    }
}
