package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.translator.Translator;
import util.Maybe;
import java.util.HashMap;

public class EqualPairRule extends Rule {
    private static final HashMap<SymbolType, String> tags;
    private static final HashMap<SymbolType, String> classes;

    static {
        tags = new HashMap<SymbolType, String>();
        tags.put(SymbolType.Bold, "b");
        tags.put(SymbolType.Italic, "i");
        tags.put(SymbolType.Strike, "span");

        classes = new HashMap<SymbolType, String>();
        classes.put(SymbolType.Strike, "strike");
    }

    public Maybe<String> render(Scanner scanner) {
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, scanner.getCurrentType());
        if (scanner.isEnd()) return Maybe.noString;
        
        HtmlTag html = new HtmlTag(tags.get(scanner.getCurrentType()));
        if (classes.containsKey(scanner.getCurrentType())) html.addAttribute("class", classes.get(scanner.getCurrentType()));
        html.add(body);
        return new Maybe<String>(html.html());
    }

    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        SymbolType type = scanner.getCurrentType();
        Phrase body = new Parser(getPage()).parseIgnoreFirst(scanner, type);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Phrase(type).add(body));
    }
}
