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
        
        String option = "";
        String value = "";
        if (parser.getCurrent().isType(SymbolType.Text) && parser.getCurrent().getContent().startsWith("-")) {
            option = parser.getCurrent().getContent();
             parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Text)) return Symbol.nothing;
            value = parser.getCurrent().getContent();
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;
            parser.moveNext(1);
        }

        if (parser.getCurrent().isType(Link.symbolType)) {
            Maybe<Symbol> link = Link.symbolType.getWikiRule().parse(parser.getCurrent(), parser);
            if (link.isNothing()) return Symbol.nothing;
            if (option.equals("-w")) link.getValue().putProperty(Link.WidthProperty, value);
            return makeImageLink(link.getValue(), imageProperty);
        }
        else if (parser.getCurrent().isType(SymbolType.Text)) {
            Symbol list = new Symbol(SymbolType.SymbolList).add(parser.getCurrent());
            Symbol link = new Symbol(Link.symbolType).add(list);
            if (option.equals("-w")) link.putProperty(Link.WidthProperty, value);
            return makeImageLink(link, imageProperty);
        }
        else return Symbol.nothing;
    }

    private Maybe<Symbol> makeImageLink(Symbol link, String imageProperty) {
        return new Maybe<Symbol>(link.putProperty(Link.ImageProperty, imageProperty));
    }
}
