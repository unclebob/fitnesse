package fitnesse.wikitext.parser;

import util.Maybe;

public class StyleRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        String content = scanner.getCurrentContent();
        char beginner = content.charAt(content.length() - 1);
        Symbol body = Parser.makeIgnoreFirst(getPage(), scanner, SymbolType.closeType(beginner)).parse();
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.Style, content.substring(7, content.length() - 1)).add(body));
    }
}
