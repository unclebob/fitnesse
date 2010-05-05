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

        parser.moveNext(1);
        if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;

        parser.moveNext(1);
        if (parser.getCurrent().isType(SymbolType.Link)) {
            Maybe<Symbol> link = SymbolType.Link.getRule().parse(parser);
            if (link.isNothing()) return Symbol.nothing;
            return makeImageLink(link.getValue(), imageProperty);
        }
        else if (parser.getCurrent().isType(SymbolType.Text)) {
            Symbol list = new Symbol(SymbolType.SymbolList).add(parser.getCurrent());
            return makeImageLink(new Symbol(SymbolType.Link).add(list), imageProperty);
        }
        else return Symbol.nothing;
    }

    private Maybe<Symbol> makeImageLink(Symbol link, String imageProperty) {
        return new Maybe<Symbol>(link.putProperty(LinkRule.ImageProperty, imageProperty));
    }
}
