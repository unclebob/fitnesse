package fitnesse.wikitext.parser;

import util.Maybe;

public class StyleRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String content = current.getContent();
        char beginner = content.charAt(content.length() - 1);
        Symbol body = parser.parseToIgnoreFirst(closeType(beginner));
        if (parser.atEnd()) return Symbol.nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.Style, content.substring(7, content.length() - 1)).add(body));
    }

    private static SymbolType closeType(char beginner) {
        return beginner == '[' ? SymbolType.CloseBracket
                : beginner == '{' ? SymbolType.CloseBrace
                : SymbolType.CloseParenthesis;
    }
}
