package fitnesse.wikitext.parser;

import util.Maybe;

public class StyleRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        String content = scanner.getCurrentContent();
        char beginner = content.charAt(content.length() - 1);
        Symbol body = parser.parseToIgnoreFirst(SymbolType.closeType(beginner));
        if (scanner.isEnd()) return Symbol.nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.Style, content.substring(7, content.length() - 1)).add(body));
    }
}
