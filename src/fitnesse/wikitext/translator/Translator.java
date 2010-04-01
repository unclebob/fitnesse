package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;
import fitnesse.wikitext.translator.HtmlBuilder;
import fitnesse.wikitext.translator.Translation;
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
        translations.put(SymbolType.Collapsible, new CollapsibleBuilder());
        translations.put(SymbolType.Define, new DefineBuilder());
        translations.put(SymbolType.Italic, new HtmlBuilder().tag("i").body(0));
        translations.put(SymbolType.Newline, new HtmlBuilder().tag("br"));
        translations.put(SymbolType.NoteLine, new HtmlBuilder().tag("span").body(0).cssClass("note"));
        translations.put(SymbolType.Contents, new ContentsBuilder());
    }

    public Translator(WikiPage currentPage) { this.currentPage = currentPage; }

    public WikiPage getPage() { return currentPage; }

    public String translateToHtml(String input) {
        Phrase list = new Parser(currentPage).parse(input);
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
            return translations.get(symbol.getType()).toHtml(this, symbol).html();
        }
        else {
            StringBuilder result = new StringBuilder(symbol.toHtml());
            for (Symbol child: symbol.getChildren()) {
                result.append(translate(child));
            }
            return result.toString();
        }
    }

    public String translate(String input) {
        return translateIgnoreFirst(new Scanner(input), SymbolType.Empty);
    }

    public String translate(Scanner scanner, SymbolType terminator) {
        return translate(scanner, new SymbolType[] {terminator});
    }

    public String translateIgnoreFirst(Scanner scanner, SymbolType terminator) {
        return translateIgnoreFirst(scanner, new SymbolType[] {terminator});
    }

    public String translate(Scanner scanner, SymbolType[] terminators) {
        return translate(scanner, terminators, new SymbolType[] {});
    }

    public String translateIgnoreFirst(Scanner scanner, SymbolType[] terminators) {
        return translate(scanner, terminators, terminators);
    }

    private String translate(Scanner scanner, SymbolType[] terminators, SymbolType[] ignoresFirst) {
        StringBuilder result = new StringBuilder();
        ArrayList<SymbolType> ignore = new ArrayList<SymbolType>();
        ignore.addAll(Arrays.asList(ignoresFirst));
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(ignore);
            if (scanner.isEnd()) break;
            Token currentToken = scanner.getCurrent();
            if (contains(terminators, currentToken.getType())) break;
            currentToken.setPage(currentPage);
            Maybe<String> translation = currentToken.render(scanner);
            if (translation.isNothing()) {
                ignore.add(currentToken.getType());
                scanner.copy(backup);
            }
            else {
                result.append(translation.getValue());
                ignore.clear();
            }
        }
        return result.toString();
    }

    private boolean contains(SymbolType[] terminators, SymbolType currentType) {
        for (SymbolType terminator: terminators)
            if (currentType == terminator) return true;
        return false;
    }
}
