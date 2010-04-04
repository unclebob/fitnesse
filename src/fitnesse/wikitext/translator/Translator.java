package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;
import util.Maybe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Translator {
    private WikiPage currentPage;

    private static final HashMap<SymbolType, Translation> translations;

    static {
        translations = new HashMap<SymbolType, Translation>();
        
        translations.put(SymbolType.AnchorName, new HtmlBuilder().tag("a").attribute("name"));
        translations.put(SymbolType.AnchorReference, new AnchorReferenceBuilder());
        translations.put(SymbolType.Bold, new HtmlBuilder().tag("b").body(0));
        translations.put(SymbolType.CenterLine, new HtmlBuilder().tag("div").body(0).cssClass("centered"));
        translations.put(SymbolType.Collapsible, new CollapsibleBuilder());
        translations.put(SymbolType.Contents, new ContentsBuilder());
        translations.put(SymbolType.Define, new DefineBuilder());
        translations.put(SymbolType.Evaluator, new EvaluatorBuilder());
        translations.put(SymbolType.HashTable, new HashTableBuilder());
        translations.put(SymbolType.HeaderLine, new HeaderLineBuilder());
        translations.put(SymbolType.HorizontalRule, new HorizontalRuleBuilder());
        translations.put(SymbolType.Include, new IncludeBuilder());
        translations.put(SymbolType.Italic, new HtmlBuilder().tag("i").body(0));
        translations.put(SymbolType.List, new ListBuilder());
        translations.put(SymbolType.Newline, new HtmlBuilder().tag("br"));
        translations.put(SymbolType.NoteLine, new HtmlBuilder().tag("span").body(0).cssClass("note"));
        translations.put(SymbolType.Preformat, new HtmlBuilder().tag("pre").bodyContent());
        translations.put(SymbolType.Strike, new HtmlBuilder().tag("span").body(0).cssClass("strike"));
        translations.put(SymbolType.Style, new StyleBuilder());
        translations.put(SymbolType.Table, new TableBuilder());
        translations.put(SymbolType.Text, new TextBuilder());
        translations.put(SymbolType.Variable, new VariableBuilder());
        translations.put(SymbolType.WikiWord, new WikiWordBuilder());
    }

    public Translator(WikiPage currentPage) { this.currentPage = currentPage; }

    public WikiPage getPage() { return currentPage; }

    public String translateToHtml(String input) {
        Symbol list = new Parser(currentPage).parse(input);
        return translateToHtml(list);
    }

    public String translateToHtml(Symbol list) {
        StringBuilder result = new StringBuilder();
        for (Symbol symbol : list.getChildren()) {
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
}
