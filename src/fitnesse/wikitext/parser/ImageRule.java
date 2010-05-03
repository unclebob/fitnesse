package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class ImageRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Symbol current = parser.getCurrent();
        String imageProperty =
                current.getContent().endsWith("l") ? LinkRule.Left
                : current.getContent().endsWith("r") ? LinkRule.Right
                : "";

        parser.getScanner().moveNext();
        if (parser.getCurrent().getType() != SymbolType.Whitespace) return Symbol.Nothing;

        parser.getScanner().moveNext();
        if (parser.getCurrent().getType() == SymbolType.Link) {
            Maybe<Symbol> link = SymbolType.Link.getRule().parse(parser);
            if (link.isNothing()) return Symbol.Nothing;
            return makeImageLink(link.getValue(), imageProperty);
        }
        else if (parser.getCurrent().getType() == SymbolType.Text) {
            Symbol list = new Symbol(SymbolType.SymbolList).add(parser.getCurrent());
            return makeImageLink(new Symbol(SymbolType.Link).add(list), imageProperty);
        }
        else return Symbol.Nothing;
    }

    private Maybe<Symbol> makeImageLink(Symbol link, String imageProperty) {
        return new Maybe<Symbol>(link.putProperty(LinkRule.ImageProperty, imageProperty));
    }
}
