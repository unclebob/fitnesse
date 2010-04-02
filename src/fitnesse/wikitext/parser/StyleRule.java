package fitnesse.wikitext.parser;

import util.Maybe;

public class StyleRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        String content = scanner.getCurrentContent();
        char beginner = content.charAt(content.length() - 1);
        Symbol body = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.closeType(beginner));
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.Style, content).add(body));
    }
}
