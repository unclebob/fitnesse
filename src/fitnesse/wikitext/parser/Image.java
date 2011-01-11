package fitnesse.wikitext.parser;

import util.Maybe;

public class Image extends SymbolType implements Rule {
    public static final Image symbolType = new Image();
    
    public Image() {
        super("Image");
        wikiMatcher(new Matcher().string("!img-l"));
        wikiMatcher(new Matcher().string("!img-r"));
        wikiMatcher(new Matcher().string("!img"));
        wikiRule(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String imageProperty =
                current.getContent().endsWith("l") ? Link.Left
                : current.getContent().endsWith("r") ? Link.Right
                : "";

        parser.moveNext(1);
        if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;

        parser.moveNext(1);
        if (parser.getCurrent().isType(Link.symbolType)) {
            Maybe<Symbol> link = Link.symbolType.getWikiRule().parse(parser.getCurrent(), parser);
            if (link.isNothing()) return Symbol.nothing;
            return makeImageLink(link.getValue(), imageProperty);
        }
        else if (parser.getCurrent().isType(SymbolType.Text)) {
            Symbol list = new Symbol(SymbolType.SymbolList).add(parser.getCurrent());
            return makeImageLink(new Symbol(Link.symbolType).add(list), imageProperty);
        }
        else return Symbol.nothing;
    }

    private Maybe<Symbol> makeImageLink(Symbol link, String imageProperty) {
        return new Maybe<Symbol>(link.putProperty(Link.ImageProperty, imageProperty));
    }
}
