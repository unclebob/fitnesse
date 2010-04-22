package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;

import java.util.HashMap;

public class Translator {
    private static final HashMap<SymbolType, Translation> translations;

    static {
        translations = new HashMap<SymbolType, Translation>();
        
        translations.put(SymbolType.Alias, new AliasBuilder());
        translations.put(SymbolType.AnchorName, new HtmlBuilder("a").attribute("name", 0).inline());
        translations.put(SymbolType.AnchorReference, new AnchorReferenceBuilder());
        translations.put(SymbolType.Bold, new HtmlBuilder("b").body(0).inline());
        translations.put(SymbolType.CenterLine, new HtmlBuilder("div").body(0).attribute("class", "centered"));
        translations.put(SymbolType.Collapsible, new CollapsibleBuilder());
        translations.put(SymbolType.Comment, new CommentBuilder());
        translations.put(SymbolType.Contents, new ContentsBuilder());
        translations.put(SymbolType.Define, new DefineBuilder());
        translations.put(SymbolType.EMail, new HtmlBuilder("a").bodyContent().attribute("href", -1, "mailto:").inline());
        translations.put(SymbolType.Evaluator, new EvaluatorBuilder());
        translations.put(SymbolType.HashTable, new HashTableBuilder());
        translations.put(SymbolType.HeaderLine, new HeaderLineBuilder());
        translations.put(SymbolType.HorizontalRule, new HorizontalRuleBuilder());
        translations.put(SymbolType.Include, new IncludeBuilder());
        translations.put(SymbolType.Italic, new HtmlBuilder("i").body(0).inline());
        translations.put(SymbolType.Link, new LinkBuilder());
        translations.put(SymbolType.Meta, new HtmlBuilder("span").body(0).attribute("class", "meta").inline());
        translations.put(SymbolType.Newline, new HtmlBuilder("br").inline());
        translations.put(SymbolType.NoteLine, new HtmlBuilder("span").body(0).attribute("class", "note").inline());
        translations.put(SymbolType.OrderedList, new ListBuilder("ol"));
        translations.put(SymbolType.Path, new HtmlBuilder("span").body(0, "classpath: ").attribute("class", "meta").inline());
        translations.put(SymbolType.PlainTextTable, new TableBuilder());
        translations.put(SymbolType.Preformat, new HtmlBuilder("pre").bodyContent());
        translations.put(SymbolType.See, new HtmlBuilder("b").body(0, "See: ").inline());
        translations.put(SymbolType.Strike, new HtmlBuilder("span").body(0).attribute("class", "strike").inline());
        translations.put(SymbolType.Style, new HtmlBuilder("span").body(0).attribute("class", -1).inline());
        translations.put(SymbolType.Table, new TableBuilder());
        translations.put(SymbolType.Text, new TextBuilder());
        translations.put(SymbolType.UnorderedList, new ListBuilder("ul"));
        translations.put(SymbolType.Variable, new VariableBuilder());
        translations.put(SymbolType.WikiWord, new WikiWordBuilder());
    }

    private WikiPage currentPage;
    private Symbol syntaxTree;

    public Translator(WikiPage currentPage, Symbol syntaxTree) {
        this.currentPage = currentPage;
        this.syntaxTree =  syntaxTree;
    }

    public WikiPage getPage() { return currentPage; }
    public Symbol getSyntaxTree() { return syntaxTree; }

    public String translate() {
        StringBuilder result = new StringBuilder();
        for (Symbol symbol : syntaxTree.getChildren()) {
            result.append(translate(symbol));
        }
        return result.toString();
    }

    public String translate(Symbol symbol) {
        if (translations.containsKey(symbol.getType())) {
            return translations.get(symbol.getType()).toHtml(this, symbol);
        }
        else {
            StringBuilder result = new StringBuilder(symbol.getContent());
            for (Symbol child: symbol.getChildren()) {
                result.append(translate(child));
            }
            return result.toString();
        }
    }

    public String formatError(String message) {
        return translate(new Symbol(SymbolType.Meta).add(message));
    }
}
