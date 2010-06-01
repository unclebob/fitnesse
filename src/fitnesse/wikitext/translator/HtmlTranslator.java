package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.SourcePage;
import fitnesse.wikitext.parser.SymbolType;

import java.util.HashMap;

public class HtmlTranslator extends Translator {
    private static final HashMap<SymbolType, Translation> translations;

    static {
        translations = new HashMap<SymbolType, Translation>();

        addTranslation(SymbolType.Alias, new AliasBuilder());
        addTranslation(SymbolType.AnchorName, new HtmlBuilder("a").attribute("name", 0).inline());
        addTranslation(SymbolType.AnchorReference, new AnchorReferenceBuilder());
        addTranslation(SymbolType.Bold, new HtmlBuilder("b").body(0).inline());
        addTranslation(SymbolType.CenterLine, new HtmlBuilder("div").body(0).attribute("class", "centered"));
        addTranslation(SymbolType.Collapsible, new CollapsibleBuilder());
        addTranslation(SymbolType.Comment, new CommentBuilder());
        addTranslation(SymbolType.Contents, new ContentsBuilder());
        addTranslation(SymbolType.Define, new DefineBuilder());
        addTranslation(SymbolType.EMail, new HtmlBuilder("a").bodyContent().attribute("href", -1, "mailto:").inline());
        addTranslation(SymbolType.Evaluator, new EvaluatorBuilder());
        addTranslation(SymbolType.HashTable, new HashTableBuilder());
        addTranslation(SymbolType.HeaderLine, new HeaderLineBuilder());
        addTranslation(SymbolType.HorizontalRule, new HorizontalRuleBuilder());
        addTranslation(SymbolType.Include, new IncludeBuilder());
        addTranslation(SymbolType.Italic, new HtmlBuilder("i").body(0).inline());
        addTranslation(SymbolType.LastModified, new LastModifiedBuilder());
        addTranslation(SymbolType.Link, new LinkBuilder());
        addTranslation(SymbolType.Meta, new HtmlBuilder("span").body(0).attribute("class", "meta").inline());
        addTranslation(SymbolType.Newline, new HtmlBuilder("br").inline());
        addTranslation(SymbolType.NoteLine, new HtmlBuilder("span").body(0).attribute("class", "note").inline());
        addTranslation(SymbolType.OrderedList, new ListBuilder("ol"));
        addTranslation(SymbolType.Path, new HtmlBuilder("span").body(0, "classpath: ").attribute("class", "meta").inline());
        addTranslation(SymbolType.PlainTextTable, new TableBuilder());
        addTranslation(SymbolType.Preformat, new HtmlBuilder("pre").bodyContent());
        addTranslation(SymbolType.See, new HtmlBuilder("b").body(0, "See: ").inline());
        addTranslation(SymbolType.Strike, new HtmlBuilder("span").body(0).attribute("class", "strike").inline());
        addTranslation(SymbolType.Style, new HtmlBuilder("span").body(0).attribute("class", -1).inline());
        addTranslation(SymbolType.Table, new TableBuilder());
        addTranslation(SymbolType.Today, new TodayBuilder());
        addTranslation(SymbolType.Text, new TextBuilder());
        addTranslation(SymbolType.UnorderedList, new ListBuilder("ul"));
        addTranslation(SymbolType.Variable, new VariableBuilder());
        addTranslation(SymbolType.WikiWord, new WikiWordBuilder());
    }
    
    private static void addTranslation(SymbolType symbolType, Translation translation) {
        translations.put(symbolType, translation);
    }

    @Override
    protected HashMap<SymbolType, Translation> getTranslations() {
        return translations;
    }

    public HtmlTranslator(SourcePage page) {
        super(page);
    }
}
